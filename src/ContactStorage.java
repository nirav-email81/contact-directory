import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Facade for contact persistence (SQLite) and CSV import/export. */
public final class ContactStorage {
    public static final String DEFAULT_GROUP = "Friends";
    public static final List<String> CONTACT_GROUPS =
            Arrays.asList("Work", "Family", "Friends");

    private static boolean initialized = false;

    private ContactStorage() {
    }

    public static void ensureInitialized() throws SQLException {
        if (!initialized) {
            ContactDatabase.initialize();
            initialized = true;
        }
    }

    public static List<Contact> load() throws SQLException {
        ensureInitialized();
        return ContactDatabase.findAll();
    }

    public static Contact insert(Contact contact) throws SQLException {
        ensureInitialized();
        int id = ContactDatabase.insert(contact);
        contact.setId(id);
        return contact;
    }

    public static void update(Contact contact) throws SQLException {
        ensureInitialized();
        ContactDatabase.update(contact);
    }

    public static void exportCsv(List<Contact> contacts, Path filepath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filepath, StandardCharsets.UTF_8)) {
            writer.write(ContactCsv.headerLine());
            writer.newLine();
            for (Contact c : contacts) {
                writer.write(ContactCsv.toRow(c));
                writer.newLine();
            }
        }
    }

    public static ImportResult importCsv(Path filepath) throws IOException, SQLException {
        ensureInitialized();
        List<ContactCsv.ParsedRow> rows = ContactCsv.parseFile(filepath);
        int imported = 0;
        int skipped = 0;
        List<String> errors = new ArrayList<>();

        for (ContactCsv.ParsedRow row : rows) {
            Contact contact = row.contact();
            if (contact.getName().isEmpty()) {
                skipped++;
                errors.add("Line " + row.lineNumber() + ": name is required.");
                continue;
            }

            String validationError = ContactValidator.validate(
                    contact.getPhone(), contact.getEmail(), contact.getExtension());
            if (validationError != null) {
                skipped++;
                errors.add("Line " + row.lineNumber() + ": " + validationError);
                continue;
            }

            insert(contact);
            imported++;
        }

        return new ImportResult(imported, skipped, errors);
    }

    public record ImportResult(int imported, int skipped, List<String> errors) {
        public boolean hasErrors() {
            return !errors.isEmpty();
        }
    }
}

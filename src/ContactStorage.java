import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/** Facade for contact persistence (SQLite) and CSV export. */
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
            writer.write("Name,Phone,Email,Group");
            writer.newLine();
            for (Contact c : contacts) {
                writer.write(escapeCsv(c.getName()) + ","
                        + escapeCsv(c.getPhone()) + ","
                        + escapeCsv(c.getEmail()) + ","
                        + escapeCsv(c.getGroup()));
                writer.newLine();
            }
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

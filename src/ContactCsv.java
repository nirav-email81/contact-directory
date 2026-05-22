import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Parse and format CSV rows for contact import/export. */
public final class ContactCsv {
    public static final String[] EXPORT_HEADERS = {
            "Name", "Phone", "Extension", "Email", "Department", "Organization", "Group"
    };

    private ContactCsv() {
    }

    public static String headerLine() {
        StringBuilder line = new StringBuilder();
        for (int i = 0; i < EXPORT_HEADERS.length; i++) {
            if (i > 0) {
                line.append(',');
            }
            line.append(escape(EXPORT_HEADERS[i]));
        }
        return line.toString();
    }

    public static String toRow(Contact c) {
        return escape(c.getName()) + ","
                + escape(c.getPhone()) + ","
                + escape(c.getExtension()) + ","
                + escape(c.getEmail()) + ","
                + escape(c.getDepartment()) + ","
                + escape(c.getOrganization()) + ","
                + escape(c.getGroup());
    }

    public static List<ParsedRow> parseFile(Path filepath) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(filepath, StandardCharsets.UTF_8)) {
            String line;
            int lineNumber = 0;
            Map<String, Integer> columnIndex = null;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                List<String> fields = parseLine(line);
                if (columnIndex == null && looksLikeHeader(fields)) {
                    columnIndex = mapHeader(fields);
                    continue;
                }

                if (columnIndex != null) {
                    rows.add(new ParsedRow(lineNumber, fromMappedFields(fields, columnIndex)));
                } else {
                    rows.add(new ParsedRow(lineNumber, fromLegacyOrder(fields)));
                }
            }
        }
        return rows;
    }

    private static boolean looksLikeHeader(List<String> fields) {
        if (fields.isEmpty()) {
            return false;
        }
        String first = fields.get(0).trim().toLowerCase(Locale.ROOT);
        return first.equals("name") || first.contains("name");
    }

    private static Map<String, Integer> mapHeader(List<String> fields) {
        Map<String, Integer> map = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            String key = normalizeHeader(fields.get(i));
            if (!key.isEmpty()) {
                map.put(key, i);
            }
        }
        return map;
    }

    private static String normalizeHeader(String header) {
        return header.trim().toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("_", "");
    }

    private static Contact fromMappedFields(List<String> fields, Map<String, Integer> columnIndex) {
        return new Contact(
                field(fields, columnIndex, "name"),
                field(fields, columnIndex, "phone"),
                field(fields, columnIndex, "email"),
                normalizeGroup(field(fields, columnIndex, "group")),
                field(fields, columnIndex, "department"),
                field(fields, columnIndex, "organization"),
                field(fields, columnIndex, "extension", "phoneextension", "telephoneextension", "ext"));
    }

    private static Contact fromLegacyOrder(List<String> fields) {
        if (fields.size() <= 4) {
            return new Contact(
                    get(fields, 0),
                    get(fields, 1),
                    fields.size() >= 3 ? get(fields, 2) : "",
                    normalizeGroup(fields.size() >= 4 ? get(fields, 3) : ""),
                    "", "", "");
        }
        return new Contact(
                get(fields, 0),
                get(fields, 1),
                get(fields, 3),
                normalizeGroup(get(fields, 6)),
                get(fields, 4),
                get(fields, 5),
                get(fields, 2));
    }

    private static String field(List<String> fields, Map<String, Integer> columnIndex, String... keys) {
        for (String key : keys) {
            Integer idx = columnIndex.get(key);
            if (idx != null) {
                return get(fields, idx);
            }
        }
        return "";
    }

    private static String get(List<String> fields, int index) {
        if (index < 0 || index >= fields.size()) {
            return "";
        }
        return fields.get(index).trim();
    }

    private static String normalizeGroup(String group) {
        if (group == null || group.isEmpty()) {
            return ContactStorage.DEFAULT_GROUP;
        }
        if (ContactStorage.CONTACT_GROUPS.contains(group)) {
            return group;
        }
        return ContactStorage.DEFAULT_GROUP;
    }

    public static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public record ParsedRow(int lineNumber, Contact contact) {
    }
}

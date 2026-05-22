import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** SQLite persistence for contacts. */
public final class ContactDatabase {
    public static final String DB_FILE = "contacts.db";
    public static final String LEGACY_FILE = "contacts.txt";
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE;

    private ContactDatabase() {
    }

    public static void initialize() throws SQLException {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contacts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL DEFAULT '',
                    email TEXT NOT NULL DEFAULT '',
                    group_name TEXT NOT NULL DEFAULT 'Friends'
                )
                """);
        }
        migrateFromTextFileIfNeeded();
    }

    static {
        loadSqliteDriver();
    }

    /** Load SQLite JDBC from classpath, or from lib/ / out/ in the project folder. */
    private static void loadSqliteDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
            return;
        } catch (ClassNotFoundException ignored) {
            // try loading jar from project directory (IDE / java -cp out)
        }

        String[] candidates = {"lib/sqlite-jdbc.jar", "out/sqlite-jdbc.jar"};
        for (String relative : candidates) {
            Path jar = Paths.get(relative).toAbsolutePath().normalize();
            if (!Files.isRegularFile(jar)) {
                continue;
            }
            try {
                URLClassLoader loader = new URLClassLoader(
                        new URL[]{jar.toUri().toURL()},
                        ContactDatabase.class.getClassLoader());
                Class.forName("org.sqlite.JDBC", true, loader);
                return;
            } catch (ReflectiveOperationException | java.io.IOException e) {
                // try next path
            }
        }

        throw new ExceptionInInitializerError(
                "SQLite JDBC driver not found. Open a terminal in the project folder and run compile.bat, "
                        + "then start the app with run.bat (or add lib/sqlite-jdbc.jar to your IDE classpath).");
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(JDBC_URL);
    }

    public static List<Contact> findAll() throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = "SELECT id, name, phone, email, group_name FROM contacts ORDER BY name COLLATE NOCASE";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                contacts.add(mapRow(rs));
            }
        }
        return contacts;
    }

    public static int insert(Contact contact) throws SQLException {
        String sql = "INSERT INTO contacts (name, phone, email, group_name) VALUES (?, ?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, contact.getName());
            ps.setString(2, contact.getPhone());
            ps.setString(3, contact.getEmail());
            ps.setString(4, contact.getGroup());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Insert failed: no generated key returned.");
    }

    public static void update(Contact contact) throws SQLException {
        String sql = """
            UPDATE contacts
            SET name = ?, phone = ?, email = ?, group_name = ?
            WHERE id = ?
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, contact.getName());
            ps.setString(2, contact.getPhone());
            ps.setString(3, contact.getEmail());
            ps.setString(4, contact.getGroup());
            ps.setInt(5, contact.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No contact found with id " + contact.getId());
            }
        }
    }

    private static void migrateFromTextFileIfNeeded() throws SQLException {
        Path legacy = Paths.get(LEGACY_FILE);
        if (!Files.exists(legacy)) {
            return;
        }

        if (!findAll().isEmpty()) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(legacy, StandardCharsets.UTF_8);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split("\\|", -1);
                if (parts.length >= 3) {
                    String group = parts.length >= 4 ? parts[3].trim() : ContactStorage.DEFAULT_GROUP;
                    if (!ContactStorage.CONTACT_GROUPS.contains(group)) {
                        group = ContactStorage.DEFAULT_GROUP;
                    }
                    insert(new Contact(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            group));
                }
            }
            Path backup = Paths.get(LEGACY_FILE + ".bak");
            Files.move(legacy, backup);
            System.out.println("Migrated contacts from " + LEGACY_FILE + " to " + DB_FILE
                    + " (backup: " + backup + ")");
        } catch (Exception e) {
            throw new SQLException("Failed to migrate " + LEGACY_FILE + ": " + e.getMessage(), e);
        }
    }

    private static Contact mapRow(ResultSet rs) throws SQLException {
        return new Contact(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("group_name"));
    }
}

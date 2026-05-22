import java.net.URL;
import java.net.URLClassLoader;
import java.io.IOException;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** SQLite persistence for contacts. */
public final class ContactDatabase {
    /** @deprecated use {@link #databasePath()} */
    @Deprecated
    public static final String DB_FILE = "contacts.db";

    private ContactDatabase() {
    }

    public static Path databasePath() {
        return AppEnvironment.databasePath();
    }

    public static void initialize() throws SQLException {
        ensureDatabaseDirectory();
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS contacts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL DEFAULT '',
                    email TEXT NOT NULL DEFAULT '',
                    group_name TEXT NOT NULL DEFAULT 'Friends',
                    department TEXT NOT NULL DEFAULT '',
                    organization TEXT NOT NULL DEFAULT '',
                    phone_extension TEXT NOT NULL DEFAULT ''
                )
                """);
            migrateSchema(conn);
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

    private static void ensureDatabaseDirectory() throws SQLException {
        Path dbPath = databasePath();
        Path parent = dbPath.getParent();
        if (parent != null && !Files.isDirectory(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new SQLException("Could not create database directory: " + parent, e);
            }
        }
    }

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath());
    }

    private static void migrateSchema(Connection conn) throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(contacts)")) {
            while (rs.next()) {
                columns.add(rs.getString("name").toLowerCase());
            }
        }
        if (!columns.contains("department")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE contacts ADD COLUMN department TEXT NOT NULL DEFAULT ''");
            }
        }
        if (!columns.contains("organization")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE contacts ADD COLUMN organization TEXT NOT NULL DEFAULT ''");
            }
        }
        if (!columns.contains("phone_extension")) {
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE contacts ADD COLUMN phone_extension TEXT NOT NULL DEFAULT ''");
            }
        }
    }

    public static List<Contact> findAll() throws SQLException {
        List<Contact> contacts = new ArrayList<>();
        String sql = """
            SELECT id, name, phone, email, group_name, department, organization, phone_extension
            FROM contacts ORDER BY name COLLATE NOCASE
            """;

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
        String sql = """
            INSERT INTO contacts (name, phone, email, group_name, department, organization, phone_extension)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindContact(ps, contact);
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
            SET name = ?, phone = ?, email = ?, group_name = ?,
                department = ?, organization = ?, phone_extension = ?
            WHERE id = ?
            """;

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindContact(ps, contact);
            ps.setInt(8, contact.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No contact found with id " + contact.getId());
            }
        }
    }

    private static void bindContact(PreparedStatement ps, Contact contact) throws SQLException {
        ps.setString(1, contact.getName());
        ps.setString(2, contact.getPhone());
        ps.setString(3, contact.getEmail());
        ps.setString(4, contact.getGroup());
        ps.setString(5, contact.getDepartment());
        ps.setString(6, contact.getOrganization());
        ps.setString(7, contact.getExtension());
    }

    private static void migrateFromTextFileIfNeeded() throws SQLException {
        Path legacy = AppEnvironment.legacyImportPath();
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
            Path backup = Paths.get(legacy.toString() + ".bak");
            Files.move(legacy, backup);
            System.out.println("Migrated contacts from " + legacy + " to " + databasePath()
                    + " (backup: " + backup + ")");
        } catch (Exception e) {
            throw new SQLException("Failed to migrate " + legacy + ": " + e.getMessage(), e);
        }
    }

    private static Contact mapRow(ResultSet rs) throws SQLException {
        return new Contact(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("group_name"),
                rs.getString("department"),
                rs.getString("organization"),
                rs.getString("phone_extension"));
    }
}

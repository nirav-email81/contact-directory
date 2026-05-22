import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Loads development or production settings from config files and system properties. */
public final class AppEnvironment {
    public static final String ENV_VAR = "CONTACT_ENV";
    public static final String ENV_PROPERTY = "contact.env";

    private static final Logger LOGGER = Logger.getLogger(AppEnvironment.class.getName());
    private static final AppEnvironment INSTANCE = new AppEnvironment();

    private final String name;
    private final boolean production;
    private final Path databasePath;
    private final Path legacyImportPath;
    private final String windowTitle;
    private final Level logLevel;

    private AppEnvironment() {
        name = resolveEnvironmentName();
        Properties props = loadProperties(name);
        production = "production".equalsIgnoreCase(props.getProperty("environment", name));

        databasePath = resolvePath(props.getProperty("database.file"), defaultDatabasePath(name));
        legacyImportPath = resolvePath(props.getProperty("legacy.file"), Paths.get("contacts.txt"));
        windowTitle = props.getProperty("window.title", defaultWindowTitle(production));
        logLevel = parseLogLevel(props.getProperty("logging.level", production ? "WARNING" : "INFO"));

        LOGGER.setLevel(logLevel);
        LOGGER.info(() -> "Environment: " + name + " | DB: " + databasePath);
    }

    public static AppEnvironment get() {
        return INSTANCE;
    }

    public static String currentName() {
        return INSTANCE.name;
    }

    public static boolean isProduction() {
        return INSTANCE.production;
    }

    public static Path databasePath() {
        return INSTANCE.databasePath;
    }

    public static Path legacyImportPath() {
        return INSTANCE.legacyImportPath;
    }

    public static String windowTitle() {
        return INSTANCE.windowTitle;
    }

    public static Level logLevel() {
        return INSTANCE.logLevel;
    }

    private static String resolveEnvironmentName() {
        String fromProperty = System.getProperty(ENV_PROPERTY);
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty.trim().toLowerCase();
        }
        String fromEnv = System.getenv(ENV_VAR);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim().toLowerCase();
        }
        return "dev";
    }

    private static Properties loadProperties(String envName) {
        Properties props = new Properties();
        String resource = "/config/application-" + envName + ".properties";
        try (InputStream in = AppEnvironment.class.getResourceAsStream(resource)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not load classpath config: " + resource, e);
        }

        Path fileConfig = Paths.get("config", "application-" + envName + ".properties");
        if (Files.isRegularFile(fileConfig)) {
            try (InputStream in = Files.newInputStream(fileConfig)) {
                Properties fileProps = new Properties();
                fileProps.load(in);
                props.putAll(fileProps);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Could not load file config: " + fileConfig, e);
            }
        }
        return props;
    }

    private static Path defaultDatabasePath(String envName) {
        if ("prod".equals(envName) || "production".equals(envName)) {
            String localAppData = System.getenv("LOCALAPPDATA");
            Path base = localAppData != null && !localAppData.isBlank()
                    ? Paths.get(localAppData, "ContactDirectory")
                    : Paths.get(System.getProperty("user.home"), ".contact-directory");
            return base.resolve("contacts.db");
        }
        return Paths.get("contacts-dev.db");
    }

    private static String defaultWindowTitle(boolean production) {
        return production
                ? "Personal Contact Directory"
                : "Personal Contact Directory (DEV)";
    }

    private static Path resolvePath(String configured, Path fallback) {
        if (configured == null || configured.isBlank()) {
            return fallback.toAbsolutePath().normalize();
        }
        String expanded = configured
                .replace("${user.home}", System.getProperty("user.home", ""));
        Path path = Paths.get(expanded);
        if (!path.isAbsolute()) {
            path = Paths.get("").toAbsolutePath().resolve(path);
        }
        return path.normalize();
    }

    private static Level parseLogLevel(String value) {
        try {
            return Level.parse(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return Level.INFO;
        }
    }
}

package xyz.peasfultown;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ApplicationConfig {
    public static final String SQLITE_JDBC_PREFIX = "jdbc:sqlite:";
    public static String CONNECTION_STRING;
    public static Path MAIN_PATH;

    static {
        MAIN_PATH = Path.of(ApplicationDefaults.MAIN_PATH);
    }

    /**
     * Check if in `dev` environment, if so, use test variables. Otherwise, load from config file (application.properties)
     */
    public static void ready() {
        System.out.println("Ready application config");
        setConnectionString(MAIN_PATH);
        createPathDirs(MAIN_PATH);
    }

    public static void setMainPath(String str) {
        setMainPath(Path.of(str));
    }

    public static void setMainPath(Path path) {
        MAIN_PATH = path;
        ready();
    }

    public static void setMainPathInConfigFile(String pathStr) {
        System.out.println("Set main path config");
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties().setFileName(ApplicationDefaults.PROPERTIES_FILE_NAME));
        try {
            Configuration config = builder.getConfiguration();
            config.setProperty("app.mainLocation", pathStr);
            builder.save();
            Path path = Path.of(pathStr);
            createPathDirs(path);
            setConnectionString(path);
        } catch (ConfigurationException e) {
            System.err.format("Failed to read application properties, revert to defaults: %s%n", e);
            setDefaults();
        }
    }

    private static void setConnectionString(Path path) {
        CONNECTION_STRING = SQLITE_JDBC_PREFIX + path.resolve("metadata.db");
    }

    /**
     * Create necessary directory.
     * @param dir
     */
    private static void createPathDirs(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.format("Unable to create parent directories for %s: %s%n", dir);
        }
    }

    /**
     * Read path string from `application.properties` file and set it for the application.
     */
    private static void loadPathFromConfigFile() {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<FileBasedConfiguration> builder =
                new FileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class)
                        .configure(params.properties().setFileName(ApplicationDefaults.PROPERTIES_FILE_NAME));
        try {
            Configuration config = builder.getConfiguration();
            String configPath = config.getString("app.mainLocation");
            MAIN_PATH = Path.of(configPath);
        } catch (ConfigurationException e) {
            System.err.format("Failed to read application properties, revert to defaults: %s%n", e);
            setDefaults();
        }
    }

    /**
     * Set all variables to defaults for application.
     */
    private static void setDefaults() {
        MAIN_PATH = Path.of(ApplicationDefaults.MAIN_PATH);
    }
}

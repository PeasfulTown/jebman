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
    public static String CONNECTION_STRING;
    public static Path MAIN_PATH;

    static {
        ready();
    }

    /**
     * Check if in `dev` environment, if so, use test variables. Otherwise, load from config file (application.properties)
     */
    private static void ready() {
        String env = System.getenv("env");
        if (env != null && env.equalsIgnoreCase("dev")) {
            System.out.println("Development environment detected.");
            MAIN_PATH = Path.of(ApplicationDefaults.TEMPORARY_PATH);
        } else {
            setMainPathFromConfigFile();
        }
        CONNECTION_STRING = "jdbc:sqlite:" + MAIN_PATH.resolve("metadata.db");
        readyPath(MAIN_PATH);
    }

    /**
     * Create necessary directory.
     * @param dir
     */
    private static void readyPath(Path dir) {
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.format("Unable to create parent directories for %s: %s%n", dir);
        }
    }

    /**
     * Read path string from `application.properties` file and set it for the application.
     */
    private static void setMainPathFromConfigFile() {
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

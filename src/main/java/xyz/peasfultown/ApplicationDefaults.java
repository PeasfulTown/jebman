package xyz.peasfultown;

import java.nio.file.Path;
import java.util.StringJoiner;

public class ApplicationDefaults {
    public static final String TEMPORARY_PATH;
    public static final String MAIN_PATH;
    public static final String PROPERTIES_FILE_NAME;

    static {
        TEMPORARY_PATH = Path.of(System.getProperty("java.io.tmpdir"),
                "jebman-library").toString();

        MAIN_PATH = Path.of(System.getProperty("user.home"),
                "Documents",
                "jebman-library").toString();

        PROPERTIES_FILE_NAME = "application.properties";
    }
}

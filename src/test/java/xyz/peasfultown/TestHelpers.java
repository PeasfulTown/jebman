package xyz.peasfultown;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.helpers.MetadataReaderException;
import xyz.peasfultown.helpers.TreeDeleter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

public class TestHelpers {
    private static final Logger logger = LoggerFactory.getLogger(TestHelpers.class);
    public static void insertTestBooks(MainController mc) throws DAOException, MetadataReaderException, IOException {
        mc.insertBook(Path.of(TestHelpers.class.getClassLoader().getResource("dummy.pdf").getFile()));
        mc.insertBook(Path.of(TestHelpers.class.getClassLoader().getResource("frankenstein.epub").getFile()));
        mc.insertBook(Path.of(TestHelpers.class.getClassLoader().getResource("gatsby.epub").getFile()));
        mc.insertBook(Path.of(TestHelpers.class.getClassLoader().getResource("machine-stops.pdf").getFile()));
    }

    public static void cleanupPath(Path filePath) {
        logger.info("Cleaning up path");
        try {
            TreeDeleter td = new TreeDeleter();
            Files.walkFileTree(filePath, td);
        } catch (IOException e) {
            logger.error("Cleaning up test file failed.", e);
            fail();
        }

        assertFalse(Files.exists(filePath));
    }

    public static void cleanupPath(String filePath) {
        cleanupPath(Path.of(filePath));
    }
}

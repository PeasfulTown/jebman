package xyz.peasfultown.helpers;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import xyz.peasfultown.ApplicationDefaults;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

class ThumbnailGeneratorTest {
    private static final String TEMP_PATH = ApplicationDefaults.TEMPORARY_PATH;

    @BeforeAll
    static void setup() throws Exception {
        if (Files.exists(Path.of(TEMP_PATH))) {
            TreeDeleter td = new TreeDeleter();
            Files.walkFileTree(Path.of(TEMP_PATH), td);
        }

        Files.createDirectories(Path.of(TEMP_PATH));
    }

    @Test
    void testGeneratePDFThumb() throws Exception {
        File dummyPDF = new File(getClass().getClassLoader().getResource("dummy.pdf").getFile());
        ThumbnailGenerator.generatePDFThumbnail(dummyPDF, Path.of(TEMP_PATH).resolve("dummyThumb.png"));

        assertTrue(Files.exists(Path.of(TEMP_PATH).resolve("dummyThumb.png")));
    }

    @Test
    void generateEpubThumb() throws Exception {
        File dummyEpub = new File(getClass().getClassLoader().getResource("gatsby.epub").getFile());
        ThumbnailGenerator.generateEpubThumbnail(dummyEpub, Path.of(TEMP_PATH).resolve("gatsbyThumb.png"));

        assertTrue(Files.exists(Path.of(TEMP_PATH).resolve("gatsbyThumb.png")));
    }
}
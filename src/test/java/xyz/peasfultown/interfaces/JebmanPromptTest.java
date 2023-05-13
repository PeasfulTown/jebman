package xyz.peasfultown.interfaces;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.ApplicationDefaults;
import xyz.peasfultown.MainController;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;
import static xyz.peasfultown.TestHelpers.cleanupPath;
import static xyz.peasfultown.TestHelpers.insertTestBooks;

class JebmanPromptTest {
    private static final Logger logger = LoggerFactory.getLogger(JebmanPromptTest.class);
    private static final Path MAIN_PATH = Path.of(ApplicationDefaults.TEMPORARY_PATH);

    @BeforeAll
    static void setup() {
        ApplicationConfig.setMainPath(MAIN_PATH);
    }

    @AfterAll
    static void cleanup() {
        cleanupPath(MAIN_PATH);
    }

    @Test
    void testPrintList() {
        MainController mc = null;
        try {
            mc = new MainController();
            insertTestBooks(mc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        try {
            // Backup input
            //InputStream sysIn = System.in;
            String testInp = "list\nquit";
            ByteArrayInputStream in = new ByteArrayInputStream(testInp.getBytes());
            System.setIn(in);
            Prompter prompter = new Prompter(in, System.out);
            JebmanPrompt jebman = new JebmanPrompt(prompter, mc);
            jebman.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    void testInsertBook() {
        MainController mc = null;
        try {
            mc = new MainController();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        Path ebookToAdd = Path.of(getClass().getClassLoader().getResource("dummy.pdf").getFile());
        logger.info("Test adding book \"{}\" using the jebman prompt", ebookToAdd);
        try {
            String testInp = "list\nadd " + ebookToAdd.toAbsolutePath().toString() + "\nlist\nexit";
            ByteArrayInputStream in = new ByteArrayInputStream(testInp.getBytes());
            System.setIn(in);
            Prompter prompter = new Prompter(in, System.out);
            JebmanPrompt jebman = new JebmanPrompt(prompter, mc);
            jebman.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }
}
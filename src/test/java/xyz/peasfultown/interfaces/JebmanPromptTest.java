package xyz.peasfultown.interfaces;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.Application;
import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.ApplicationDefaults;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import static xyz.peasfultown.TestHelpers.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

class JebmanPromptTest {
    private static final Logger logger = LoggerFactory.getLogger(JebmanPromptTest.class);
    private static final Path MAIN_PATH = Path.of(ApplicationDefaults.TEMPORARY_PATH);

    @AfterAll
    static void cleanup() {
        cleanupPath(MAIN_PATH);
    }

    @Test
    void testPrint() {
        ApplicationConfig.setMainPath(MAIN_PATH);
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
}
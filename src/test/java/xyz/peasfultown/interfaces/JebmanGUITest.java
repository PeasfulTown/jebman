package xyz.peasfultown.interfaces;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.ApplicationDefaults;
import xyz.peasfultown.MainController;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.peasfultown.TestHelpers.*;

class JebmanGUITest {
    private static final Logger logger = LoggerFactory.getLogger(JebmanGUITest.class);
    @BeforeAll
    static void setup() throws Exception {
        ApplicationConfig.setMainPath(ApplicationDefaults.TEMPORARY_PATH);
        cleanupPath(ApplicationConfig.MAIN_PATH);
    }

    @Test
    void testGui() {
        MainController mc = null;

        try {
            mc = new MainController();
            insertTestBooks(mc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        try {
            JebmanGUI.run(mc);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }
}
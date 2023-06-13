package xyz.peasfultown.interfaces;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.ApplicationDefaults;
import xyz.peasfultown.MainController;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Series;

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
            mc.tagBook(1, "something");
            mc.tagBook(1, "else");
            mc.tagBook(1, "tbr");
            mc.tagBook(2, "read");
            mc.tagBook(2, "science");
            mc.tagBook(3, "tbr");
            mc.tagBook(3, "something");
            mc.tagBook(4, "read");
            Book updatedBook = mc.getBookById(1);
            updatedBook.setSeries(new Series("Some Series"));
            mc.updateBook(mc.getBookById(1));
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
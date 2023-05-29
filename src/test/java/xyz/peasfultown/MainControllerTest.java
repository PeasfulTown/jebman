/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Driver for all database functions.
 */
package xyz.peasfultown;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.impl.*;
import xyz.peasfultown.domain.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static xyz.peasfultown.TestHelpers.cleanupPath;
import static xyz.peasfultown.TestHelpers.insertTestBooks;


public class MainControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(MainControllerTest.class);
    private static final Path mainPath = Path.of(System.getProperty("file.separator"),
            System.getProperty("java.io.tmpdir"),
            "jebman-library");
    private static final Path dbPath = mainPath.resolve("metadata.db");
    static MainController mc = null;

    @BeforeAll
    static void setup() throws Exception {
        cleanupPath(mainPath);
        ApplicationConfig.setMainPath(mainPath);
    }

    @BeforeEach
    void cleanup() throws Exception {
        cleanupPath(mainPath);
    }

    @Test
    void testDbFileExistsInTemp() {
        logger.info("Checking if db file will be created in temp path.");
        try {
            mc = new MainController();

            assertTrue(Files.exists(dbPath));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        } finally {
            mc = null;
        }
    }

    @Test
    void insertPDFCopiesFileToMainPath() {
        Path file = Path.of(getClass().getClassLoader().getResource("dummy.pdf").getFile());
        logger.info("Test insert file \"{}\"", file);
        assertTrue((Files.exists(file)));

        try {
            MainController mc = new MainController();
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        Path expectedPath = Path.of(mainPath.toString(), "Evangelos Vlachogiannis", "dummy (1)", "dummy.pdf");

        assertTrue(Files.exists(expectedPath), "File expected at " + expectedPath);
    }

    @Test
    void insertPDFInsertsCorrectRecords() {
        // TODO: check tags
        Path file = Path.of(getClass().getClassLoader().getResource("dummy.pdf").getFile());
        logger.info("Test insert file \"{}\"", file);

        MainController mc = null;
        try {
            mc = new MainController();
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        try {
            Book bookRecord = new JDBCBookDAO(null, null).read("dummy");
            Author authorRecord = new JDBCAuthorDAO().read("Evangelos Vlachogiannis");

            logger.info("Book record: {}", bookRecord);
            logger.info("Author record: {}", authorRecord);

            assertNotNull(bookRecord);
            assertNotNull(authorRecord);
        } catch (DAOException e) {
            logger.error("Failed to query records.", e);
            fail();
        }
    }

    @Test
    void insertEpubCopiesFileToMainPath() {
        Path file = Path.of(getClass().getClassLoader().getResource("frankenstein.epub").getFile());
        logger.info("Test insert file \"{}\"", file);

        try {
            MainController mc = new MainController();
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        Path expectedPath = Path.of(mainPath.toString(),
                "Mary Wollstonecraft Shelley",
                "Frankenstein (1)",
                "Frankenstein.epub");
        assertTrue(Files.exists(expectedPath), "File expected at " + expectedPath);
    }

    @Test
    void insertEpubInsertsCorrectRecords() {
        Path file = Path.of(getClass().getClassLoader().getResource("frankenstein.epub").getFile());
        logger.info("Test insert file \"{}\"", file);
        assertTrue((Files.exists(file)));
        MainController mc = null;

        try {
            mc = new MainController();
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e);
        }

        try {
            // TODO: check tags
            SearchableRecordSet<Publisher> publishers = (SearchableRecordSet<Publisher>) new JDBCPublisherDAO().readAll();
            SearchableRecordSet<Series> series = (SearchableRecordSet<Series>) new JDBCSeriesDAO().readAll();
            Book bookRecord = new JDBCBookDAO(series, publishers).read("Frankenstein");
            Publisher publisherRecord = new JDBCPublisherDAO().read("Oxford University Press");
            Author authorRecord = new JDBCAuthorDAO().read("Mary Wollstonecraft Shelley");
            BookAuthor bookAuthor = new JDBCBookAuthorDAO().read(1);

            logger.info("Book record: {}", bookRecord);
            logger.info("Publisher record: {}", publisherRecord);
            logger.info("Author record: {}", authorRecord);

            assertNotNull(bookRecord);
            assertNotNull(publisherRecord);
            assertNotNull(authorRecord);
            assertEquals("1,1,1", bookAuthor.toString());
        } catch (Exception e) {
            logger.error("Failed to validate \"{}\" records.", file.getFileName(), e);
            fail();
        }
    }

    @Test
    void insertBooksUpdatesLists() {
        logger.info("Check if books list is updated after inserting books");
        try {
            MainController mc = new MainController();
            insertTestBooks(mc);

            Set<Book> books = mc.getBooks();
            Set<Author> authors = mc.getAuthors();
            Set<Publisher> publishers = mc.getPublishers();

            assertEquals(4, books.size());
            assertTrue(authors.size() > 0);
            assertTrue(publishers.size() > 0);
            for (Book b : books) {
                logger.info(b.toString());
            }
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void deleteBookRemovesBookFromFileSystem() {
        logger.info("Check book removed from filepath on delete");
        try {
            MainController mc = new MainController();
            insertTestBooks(mc);

            SearchableRecordSet<Book> books = (SearchableRecordSet<Book>) mc.getBooks();

            Book frankenstein = books.getByName("Frankenstein");
            Path filePath = ApplicationConfig.MAIN_PATH.resolve(frankenstein.getPath());
            logger.info("Book Path: {}", filePath);
            assertTrue(Files.exists(filePath));
            mc.removeBook(frankenstein.getId());
            assertFalse(Files.exists(filePath));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    void deleteBookRemovesRecordFromDatabase() {
        logger.info("Check book record removed from database upon delete");
        try {
            MainController mc = new MainController();
            insertTestBooks(mc);

            SearchableRecordSet<Book> books = (SearchableRecordSet<Book>) mc.getBooks();
            int initialSize = books.size();
            Book frankenstein = books.getByName("Frankenstein");
            assertNotNull(frankenstein);

            SearchableRecordSet<Publisher> publishers = (SearchableRecordSet<Publisher>) new JDBCPublisherDAO().readAll();
            SearchableRecordSet<Series> series = (SearchableRecordSet<Series>) new JDBCSeriesDAO().readAll();

            JDBCBookDAO bookDAO = new JDBCBookDAO(series, publishers);

            assertNotNull(bookDAO.read(frankenstein.getId()));
            mc.removeBook(frankenstein);
            books = (SearchableRecordSet<Book>) mc.getBooks();
            int finalSize = books.size();
            assertNull(bookDAO.read(frankenstein.getId()));
            assertTrue(initialSize > finalSize);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    void addBookTagInsertsCorrectBookTagRecord() {
        // TODO: finish
        try {
            MainController mc = new MainController();
            insertTestBooks(mc);
            Book book = ((SearchableRecordSet<Book>) mc.getBooks()).getById(2);
            mc.tagBook(book.getId(), "testTag");
            BookTag bt = mc.readAllBookTagLinks().iterator().next();

            assertNotNull(bt);
            assertEquals(2, bt.getBookId());
            assertEquals(1, bt.getTagId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    void queryBooksOfASpecificTagGetsCorrectResults() {
        try {
            MainController mc = new MainController();
            insertTestBooks(mc);
            Book book0 = ((SearchableRecordSet<Book>) mc.getBooks()).getById(3);
            Book book1 = ((SearchableRecordSet<Book>) mc.getBooks()).getById(2);

            mc.tagBook(book0.getId(), "classic");
            mc.tagBook(book0.getId(), "tbr");
            mc.tagBook(book0.getId(), "favs");

            mc.tagBook(book1.getId(), "tbr");

            Set<Book> queriedBooks = mc.getBooksByTag("tbr");

            assertEquals(2, queriedBooks.size());
            assertEquals(book0, queriedBooks.toArray()[0]);
            assertEquals(book1, queriedBooks.toArray()[1]);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    void listTagsThatBookBelongsTo() {
        try {
            MainController mc = new MainController();
            insertTestBooks(mc);
            Book book0 = ((SearchableRecordSet<Book>) mc.getBooks()).getById(3);
            Book book1 = ((SearchableRecordSet<Book>) mc.getBooks()).getById(2);

            mc.tagBook(book0.getId(), "classic");
            mc.tagBook(book0.getId(), "tbr");
            mc.tagBook(book0.getId(), "favs");
            mc.tagBook(book1.getId(), "tbr");

            Set<Tag> bookTags = mc.getTagsOfBook(book0);
            assertNotNull(bookTags);
            assertEquals(3, bookTags.size());
            assertTrue(bookTags.contains(mc.getTagByName("classic")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e);
        }
    }

//    @Test
//    void addFormatAddsFileToBookDirectory() {
//        logger.info("Check add book format adds file to the book directory");
//        // TODO: finish
//        fail();
//    }
//
//    @Test
//    void removeFormatRemovesFileFromDirectory() {
//        logger.info("Check remove book format will delete it from the directory");
//        // TODO: finish
//        fail();
//    }
//
//    @Test
//    void removeLastBookFormatAvailableDeletesDirectory() {
//        logger.info("Check remove book format will delete it from the directory");
//        // TODO: finish
//        fail();
//    }

}

/**
 * The MIT License (MIT)
 * =====================
 * <p>
 * Copyright © 2023 PeasfulTown
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Driver for all database functions.
 */
package xyz.peasfultown;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.base.Author;
import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.Publisher;
import xyz.peasfultown.db.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class MainControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(MainControllerTest.class);
    private static final Path mainPath = Path.of(new StringBuilder(System.getProperty("java.io.tmpdir"))
            .append(System.getProperty("file.separator")).append("jebman-library").toString());
    private static final Path dbPath = mainPath.resolve("metadata.db");
    static MainController mc = null;

    @AfterEach
    void cleanupEach() {
        cleanupPath(mainPath);
    }

    @Test
    void testDbFileExistsInTemp() {
        try {
            mc = new MainController(mainPath.getParent());

            assertTrue(Files.exists(mainPath));
            cleanupPath(mainPath);
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            fail();
        } finally {
            mc = null;
        }
    }

    @Test
    void testDbFileExistsInDocuments() {
        try {
            mc = new MainController();

            Path docsDir = Path.of(new StringBuilder(System.getProperty("user.home"))
                    .append(System.getProperty("file.separator"))
                    .append("Documents")
                    .append(System.getProperty("file.separator"))
                    .append("jebman-library")
                    .append(System.getProperty("file.separator"))
                    .append("metadata.db")
                    .toString());

            assertTrue(Files.exists(docsDir));
            cleanupPath(docsDir.getParent());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            fail();
        }
    }

    @Test
    void insertPDFCopiesFileToMainPath() {
        Path file = Path.of(getClass().getClassLoader().getResource("dummy.pdf").getFile());
        logger.info("Test insert file \"{}\"", file);
        assertTrue((Files.exists(file)));

        try {
            MainController mc = new MainController(mainPath.getParent());
             mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        Path expectedPath = mainPath.resolve(new StringBuilder("Evangelos Vlachogiannis")
                        .append(System.getProperty("file.separator"))
                        .append("dummy.pdf").toString());

        assertTrue(Files.exists(expectedPath), "File expected at " + expectedPath);
    }

    @Test
    void insertPDFInsertsCorrectRecords() {
        Path file = Path.of(getClass().getClassLoader().getResource("dummy.pdf").getFile());
        logger.info("Test insert file \"{}\"", file);

        try {
            MainController mc = new MainController(mainPath.getParent());
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        try (Connection con = DbConnection.getConnection(dbPath.toString())){
            String bookRecord = BookDb.queryByTitle(con, "dummy");
            String authorRecord = AuthorDb.queryByName(con, "Evangelos Vlachogiannis");
            String bookAuthorLinkRecord = BookAuthorLinkDb.queryForBook(con, Integer.parseInt(bookRecord.split(",")[0]));

            logger.info("Book record: {}", bookRecord);
            logger.info("Author record: {}", authorRecord);
            logger.info("Link record: {}", bookAuthorLinkRecord);

            assertNotNull(bookRecord);
            assertNotNull(authorRecord);
            assertNotNull(bookAuthorLinkRecord);
            assertEquals(Integer.parseInt(authorRecord.split(",")[0]), Integer.parseInt(bookAuthorLinkRecord.split(",")[2]));
        } catch (SQLException e) {
            logger.error("Failed to query book record.", e);
            fail();
        }
    }

    @Test
    void insertEpubCopiesFileToMainPath() {
        Path file = Path.of(getClass().getClassLoader().getResource("frankenstein.epub").getFile());
        logger.info("Test insert file \"{}\"", file);

        try {
            MainController mc = new MainController(mainPath.getParent());
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail();
        }

        Path expectedPath = mainPath.resolve(new StringBuilder("Mary Wollstonecraft Shelley")
                .append(System.getProperty("file.separator"))
                .append("Frankenstein.epub").toString());
        assertTrue(Files.exists(expectedPath), "File expected at " + expectedPath);
    }

    @Test
    void insertEpubInsertsCorrectRecords() {
        Path file = Path.of(getClass().getClassLoader().getResource("frankenstein.epub").getFile());
        logger.info("Test insert file \"{}\"", file);
        assertTrue((Files.exists(file)));

        try {
            MainController mc = new MainController(mainPath.getParent());
            mc.insertBook(file);
        } catch (Exception e) {
            logger.error(e.getMessage());
            fail(e);
        }

        try (Connection con = DbConnection.getConnection(dbPath.toString())){
            String bookRecord = BookDb.queryByTitle(con, "Frankenstein");
            String publisherRecord = PublisherDb.queryByName(con, "Oxford University Press");
            String authorRecord = AuthorDb.queryByName(con, "Mary Wollstonecraft Shelley");
            String bookAuthorLinkRecord = BookAuthorLinkDb.queryForBook(con, 1);

            logger.info("Book record: {}", bookRecord);
            logger.info("Publisher record: {}", publisherRecord);
            logger.info("Link record: {}", bookAuthorLinkRecord);

            assertNotNull(bookRecord);
            assertNotNull(publisherRecord);
            assertNotNull(authorRecord);
            assertNotNull(bookAuthorLinkRecord);
            assertEquals(1, Integer.parseInt(bookRecord.split(",")[6]));
            assertEquals(1, Integer.parseInt(bookAuthorLinkRecord.split(",")[1]));
            assertEquals(1, Integer.parseInt(bookAuthorLinkRecord.split(",")[2]));
        } catch (SQLException e) {
            logger.error("Failed to validate \"{}\" records.", file.getFileName(), e);
            fail();
        }
    }

    @Test
    void insertBooksUpdatesLists() {
        try {
            MainController mc = new MainController(mainPath.getParent());
            insertTestBooks(mc);

            List<Book> books = mc.getBooks();
            List<Author> authors = mc.getAuthors();
            List<Publisher> publishers = mc.getPublishers();

            assertEquals(4, books.size());
            assertTrue(authors.size() > 0);
            assertTrue(publishers.size() > 0);
        } catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void bookListIsCorrect() {
        // TODO: FINISH
    }

    void insertTestBooks(MainController mc) throws XMLStreamException, SQLException, IOException {
        mc.insertBook(Path.of(getClass().getClassLoader().getResource("dummy.pdf").getFile()));
        mc.insertBook(Path.of(getClass().getClassLoader().getResource("frankenstein.epub").getFile()));
        mc.insertBook(Path.of(getClass().getClassLoader().getResource("gatsby.epub").getFile()));
        mc.insertBook(Path.of(getClass().getClassLoader().getResource("machine-stops.pdf").getFile()));
    }

    void cleanupPath(Path filePath) {
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
}

class TreeDeleter implements FileVisitor<Path> {
    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes basicFileAttributes) throws IOException {
        System.out.format("Deleting \"%s\"%n", path);
        Files.delete(path);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        System.err.format("File visit failed: %s%n", e);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        System.out.format("Deleting \"%s\"%n", path);
        Files.delete(path);
        return FileVisitResult.CONTINUE;
    }
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

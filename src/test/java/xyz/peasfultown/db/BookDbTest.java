/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for the BookDb class.
 */
package xyz.peasfultown.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.base.Author;
import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.Publisher;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class BookDbTest {
    private static final Logger logger = LoggerFactory.getLogger(BookDbTest.class);
    private static final Author AU_JOE_ABERCROMBIE = new Author("Joe Abercrombie");
    private static Connection con;
    private static List<Book> booklistG;
    private static Book b1, b2, b3, b4, b5;
    private static Publisher p1;
    private static Instant pubDate1, pubDate2, pubDate3;

    /**
     * Set-ups and Tear-downs
     */
    @BeforeAll
    static void setup() {
        logger.info("---SQLBookTest Setup---");

        p1 = new Publisher("Gollancz");

        pubDate1 = Book.toTimeStamp(2007, 3, 8);
        pubDate2 = Book.toTimeStamp(2007, 3, 15);
        pubDate3 = Book.toTimeStamp(2008, 3, 20);

        b1 = new Book("9780575079793",
                "",
                "The Blade Itself",
                pubDate1);
        b1.setPublisher(p1);
        b1.setAuthor(AU_JOE_ABERCROMBIE);

        b2 = new Book("9780575077881",
                "",
                "Before They Are Hanged",
                pubDate2);
        b2.setPublisher(p1);
        b2.setAuthor(AU_JOE_ABERCROMBIE);
        b2.setSeriesNumber(2.0);

        b3 = new Book("9780575077904",
                "",
                "Last Argument of Kings",
                pubDate3);
        b3.setPublisher(p1);
        b3.setAuthor(AU_JOE_ABERCROMBIE);
        b3.setSeriesNumber(3.0);

        b4 = new Book("Rebecca");
        b5 = new Book("Effective Java");

        booklistG = new ArrayList<>();
        booklistG.add(b1);
        booklistG.add(b2);
        booklistG.add(b3);
        booklistG.add(b4);
        booklistG.add(b5);

        establishConnection();

        try {
            boolean publisherTableExists = PublisherDb.tableExists(con);

            if (publisherTableExists)
                PublisherDb.dropTable(con);

            PublisherDb.createTable(con);
            PublisherDb.insert(con, p1);

            boolean bookSeriesTableExists = BookSeriesDb.tableExists(con);

            if (bookSeriesTableExists)
                BookSeriesDb.dropTable(con);

            BookSeriesDb.createTable(con);

            boolean bookTableExists = BookDb.tableExists(con);

            if (bookTableExists)
                BookDb.dropTable(con);

            BookDb.createTable(con);

            assertEquals(0, BookDb.queryAll(con).size());
        } catch (SQLException e) {
            fail(e);
        }

        closeConnection();
    }

    @BeforeEach
    void setupBeforeEach() {
        establishConnection();
    }

    @AfterEach
    void teardownAfterEach() {
        closeConnection();
    }

    @AfterAll
    static void teardown() {
        closeConnection();
    }

    /**
     * Test inserting every single book individually and
     * querying them using book id in the db
     * <p>
     * Inserting rows into sqlite table should also add
     * objects into the bookdb internal list object
     */
    @Test
    @Order(1)
    void insertSingles() {
        logger.info("Executing test for inserting single record");

        String qb1 = null;
        String qb2 = null;
        String qb3 = null;

        String qb4 = null;
        String qb5 = null;

        List<String> bookList = null;

        try {
            for (int i = 0; i < booklistG.size(); i++) {
                BookDb.insert(con, booklistG.get(i));
                BookDb.update(con, booklistG.get(i).getId(), booklistG.get(i));
            }

            qb1 = BookDb.queryById(con, 1);
            qb2 = BookDb.queryById(con, 2);
            qb3 = BookDb.queryById(con, 3);

            qb4 = BookDb.queryById(con, 4);
            qb5 = BookDb.queryById(con, 5);

            bookList = BookDb.queryAll(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        for (int i = 0; i < bookList.size(); i++) {
            Book b = booklistG.get(i);

            logger.info("Book in list:  {}", b.toString());
            logger.info("Book in db:    {}", bookList.get(i));
        }

        assertEquals(5, bookList.size());

        assertEquals(b1.getIsbn(), qb1.split(",")[1]);
        assertEquals(b2.getIsbn(), qb2.split(",")[1]);
        assertEquals(b3.getIsbn(), qb3.split(",")[1]);

        assertEquals(b4.getTitle(), qb4.split(",")[3]);
        assertEquals(b5.getTitle(), qb5.split(",")[3]);

    }

    /**
     * Test deleting singles from db using book id
     */
    @Test
    @Order(2)
    void deleteSingleById() {
        logger.info("Executing test for deleting single record");
        int rFirst = 0;
        int rAfter = 0;

        try {
            rFirst = BookDb.queryAll(con).size();
            BookDb.deleteById(con, 1);
            rAfter = BookDb.queryAll(con).size();
        } catch (SQLException e) {
            fail(e);
        }

        logger.info("Number of rows before deletion: {}, number of rows after deletion: {}", rFirst, rAfter);
        assertTrue(rFirst > rAfter);
        assertEquals(4, rAfter);
    }

    /**
     * Test deleting a batch of books from the db
     * using an array of id
     */
    @Test
    @Order(3)
    void deleteBatchById() {
        logger.info("Executing test for deleting multiple records");
        int rFirst = 0;
        int rAfter = 0;
        List<String> booklistBefore = null;

        try {
            booklistBefore = BookDb.queryAll(con);
            int[] idsToDelete = new int[booklistBefore.size()];

            // Collect IDs
            for (int i = 0; i < booklistBefore.size(); i++) {
                String[] parts = booklistBefore.get(i).split(",");
                idsToDelete[i] = Integer.valueOf(parts[0]);
            }

            logger.info("IDs to delete: {}", idsToDelete);

            rFirst = BookDb.queryAll(con).size();
            // Actual
            BookDb.deleteByIds(con, idsToDelete);

            rAfter = BookDb.queryAll(con).size();
        } catch (SQLException e) {
            fail(e);
        }

        logger.info("Rows before batch delete: {}, rows after batch delete: {}", rFirst, rAfter);
        assertTrue(rFirst > rAfter, "Number of rows before deletion should be higher than the number" +
                "of rows after deletion.");
        assertEquals(0, rAfter);
    }

    /**
     * Test inserting a batch of books by giving
     * a list of books objects
     */
    @Test
    @Order(4)
    void insertBatch() {
        logger.info("Executing test for inserting multiple records");
        List<Book> booklist = new ArrayList<>();
        booklist.add(b1);
        booklist.add(b2);
        booklist.add(b3);
        booklist.add(b4);
        booklist.add(b5);

        int rFirst = 0;
        int rAfter = 0;

        try {
            rFirst = BookDb.queryAll(con).size();
            BookDb.insert(con, booklist);
            rAfter = BookDb.queryAll(con).size();
        } catch (SQLException e) {
            fail(e);
        }

        logger.info("Number of rows before insert: {}, number of rows after insert: {}", rFirst, rAfter);
        assertTrue(rFirst < rAfter);
        assertEquals(booklist.size(), rAfter);
    }

    @Test
    @Order(5)
    void updateSingle() {
        List<String> booklistBefore = null;
        List<String> booklistAfter = null;

        Book nb1 = new Book(b1.getId(),
                b1.getIsbn(),
                b1.getUuid(),
                "Something Title",
                b1.getSeries(),
                b1.getSeriesNumber(),
                b1.getPublisher(),
                b1.getPublishDate(),
                b1.getAddedDate(),
                b1.getModifiedDate());

        Book nb2 = new Book(b1.getId(),
                b2.getIsbn(),
                b2.getUuid(),
                "Another Title",
                b2.getSeries(),
                b2.getSeriesNumber(),
                b2.getPublisher(),
                b2.getPublishDate(),
                b2.getAddedDate(),
                b2.getModifiedDate());

        Book nb3 = new Book(b1.getId(),
                b3.getIsbn(),
                b3.getUuid(),
                "Boring Title",
                b3.getSeries(),
                b3.getSeriesNumber(),
                b3.getPublisher(),
                b3.getPublishDate(),
                b3.getAddedDate(),
                b3.getModifiedDate());

        List<Book> updatedBooks = new ArrayList<>();
        updatedBooks.add(nb1);
        updatedBooks.add(nb2);
        updatedBooks.add(nb3);

        try {
            booklistBefore = BookDb.queryAll(con);

            int[] idsToUpdate = new int[3];

            for (int i = 0; i < idsToUpdate.length; i++) {
                String[] parts = booklistBefore.get(i).split(",");
                idsToUpdate[i] = Integer.valueOf(parts[0]);
            }

            logger.info("IDs to update: {}", idsToUpdate);

            for (int i = 0; i < idsToUpdate.length; i++) {
                BookDb.update(con, idsToUpdate[i], updatedBooks.get(i));
            }

            booklistAfter = BookDb.queryAll(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Book database before: \n{}", booklistBefore.get(0));
        logger.info("Book database after: \n{}", booklistAfter.get(0));
        assertEquals(booklistBefore.size(), booklistAfter.size());

        String[] parts = booklistAfter.get(0).split(",");

        assertEquals(nb1.getIsbn(), parts[1]);
        assertEquals(nb1.getUuid(), parts[2]);
        assertEquals(nb1.getTitle(), parts[3]);
        assertEquals(String.valueOf(nb1.getSeriesNumber()), parts[6]);
        assertEquals(nb1.getPublishDate().toString(), parts[9]);
        assertEquals(nb1.getAddedDate().toString(), parts[10]);
        assertEquals(nb1.getModifiedDate().toString(), parts[11]);
    }

    @Test
    void updateBatch() {
        // TODO: test for updating multiple records
    }

    /**
     * Helper methods
     */
    private static void establishConnection() {
        try {
            con = DbConnection.getTestConnection();
        } catch (SQLException e) {
            fail(e.getMessage());
        }
    }

    private static void closeConnection() {
        try {
            DbConnection.close(con);
        } catch (SQLException e) {
            fail(e.getMessage());
        }
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

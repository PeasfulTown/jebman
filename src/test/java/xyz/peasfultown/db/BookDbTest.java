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

        b1 = new Book("The Blade Itself");
        b1.setIsbn("9780575079793");
        b1.setPublisher(p1);
        b1.setPublishDate(pubDate1);
        b1.addAuthor(AU_JOE_ABERCROMBIE);

        b2 = new Book("Before They Are Hanged");
        b2.setIsbn("9780575077881");
        b2.setPublisher(p1);
        b2.setPublishDate(pubDate2);
        b2.addAuthor(AU_JOE_ABERCROMBIE);
        b2.setNumberInSeries(2.0);

        b3 = new Book("Last Argument of Kings");
        b3.setIsbn("9780575077904");
        b3.setPublisher(p1);
        b3.setPublishDate(pubDate3);
        b3.addAuthor(AU_JOE_ABERCROMBIE);
        b3.setNumberInSeries(3.0);

        b4 = new Book();
        b4.setTitle("Rebecca");

        b5 = new Book();
        b5.setTitle("Effective Java");

        booklistG = new ArrayList<>();
        booklistG.add(b1);
        booklistG.add(b2);
        booklistG.add(b3);
        booklistG.add(b4);
        booklistG.add(b5);

        establishConnection();

        try {
            boolean publisherTableExists = PublisherDb.tableExists(con);

            if (publisherTableExists) {
                PublisherDb.dropTable(con);
            }

            PublisherDb.createTable(con);
            Publisher rp1 = PublisherDb.insert(con, p1);
            p1.setId(rp1.getId());
            rp1 = null;

            boolean tableExists = BookDb.tableExists(con);

            if (tableExists) {
                BookDb.dropTable(con);
            }
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

        Book qb1 = null;
        Book qb2 = null;
        Book qb3 = null;

        Book qb4 = null;
        Book qb5 = null;

        List<Book> booklist = null;

        try {
            for (int i = 0; i < booklistG.size(); i++) {
                BookDb.insert(con, booklistG.get(i));
            }

            qb1 = BookDb.queryById(con, 1);
            qb2 = BookDb.queryById(con, 2);
            qb3 = BookDb.queryById(con, 3);

            qb4 = BookDb.queryById(con, 4);
            qb5 = BookDb.queryById(con, 5);

            booklist = BookDb.queryAll(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        for (int i = 0; i < booklist.size(); i++) {
            Book qb = booklist.get(i);
            Book b = booklistG.get(i);

            logger.info("Book in list: id {} = {}, {}, {}, {}", i, b.getIsbn(), b.getTitle(), b.getPublishDate(), b.getAddedDate());
            logger.info("Book in db:   id {} = {}, {}, {}, {}", qb.getId(), qb.getIsbn(), qb.getTitle(), qb.getPublishDate(), qb.getAddedDate());
        }

        assertEquals(5, booklist.size());

        assertEquals(b1.getIsbn(), qb1.getIsbn());
        assertEquals(b2.getIsbn(), qb2.getIsbn());
        assertEquals(b3.getIsbn(), qb3.getIsbn());

        assertEquals(b4.getTitle(), qb4.getTitle());
        assertEquals(b5.getTitle(), qb5.getTitle());

    }

    /**
     * Test deleting singles from db
     * using book id
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
        List<Book> booklistBefore = null;

        try {
            booklistBefore = BookDb.queryAll(con);
            int[] idsToDelete = new int[booklistBefore.size()];

            // Collect IDs
            for (int i = 0; i < booklistBefore.size(); i++) {
                idsToDelete[i] = booklistBefore.get(i).getId();
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
        List<Book> booklistBefore = null;
        List<Book> booklistAfter = null;

        Book nb1 = new Book(b1.getId(),
                b1.getIsbn(),
                "",
                "Something Title",
                b1.getPublisher(),
                b1.getPublishDate(),
                b1.getAddedDate(),
                b1.getModifiedDate(),
                b1.getNumberInSeries());

        Book nb2 = new Book(b1.getId(),
                b2.getIsbn(),
                "",
                "Another Title",
                b2.getPublisher(),
                b2.getPublishDate(),
                b2.getAddedDate(),
                b2.getModifiedDate(),
                b2.getNumberInSeries());

        Book nb3 = new Book(b1.getId(),
                b3.getIsbn(),
                "",
                "Boring Title",
                b3.getPublisher(),
                b3.getPublishDate(),
                b3.getAddedDate(),
                b3.getModifiedDate(),
                b3.getNumberInSeries());

        List<Book> updatedBooks = new ArrayList<>();
        updatedBooks.add(nb1);
        updatedBooks.add(nb2);
        updatedBooks.add(nb3);

        try {
            booklistBefore = BookDb.queryAll(con);

            int[] idsToUpdate = new int[3];

            for (int i = 0; i < idsToUpdate.length; i++) {
                idsToUpdate[i] = booklistBefore.get(i).getId();
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
        assertEquals(nb1.getIsbn(), booklistAfter.get(0).getIsbn());
        assertEquals(nb1.getTitle(), booklistAfter.get(0).getTitle());
        assertEquals(nb1.getNumberInSeries(), booklistAfter.get(0).getNumberInSeries());
        logger.info("Inserted publisher: {}, queried publisher: {}", nb1.getPublisher().getName(), booklistAfter.get(0).getPublisher().getName());
        assertEquals(nb1.getPublisher().getName(), booklistAfter.get(0).getPublisher().getName());
        assertEquals(nb1.getPublishDate(), booklistAfter.get(0).getPublishDate());
        assertEquals(nb1.getAddedDate(), booklistAfter.get(0).getAddedDate());
        assertEquals(nb1.getModifiedDate(), booklistAfter.get(0).getModifiedDate());
    }

    @Test
    void updateBatch() {


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
            DbConnection.closeConnection(con);
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

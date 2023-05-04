package xyz.peasfultown.db;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class BookAuthorLinkDbTest {
    private static final Logger logger = LoggerFactory.getLogger(BookAuthorLinkDbTest.class);

    @BeforeAll
    static void setup() {
        Book book1 = new Book();
        book1.setIsbn("9780199537150");
        book1.setUuid("3f2fdc96-e5f6-430f-a08a-8c6c9dc8341c");
        book1.setTitle("Frankenstein");

        Book book2 = new Book();
        book2.setTitle("The Great Gatsby");

        Publisher publisher = new Publisher("Oxford University Press");

        try (Connection con = DbConnection.getTestConnection()) {
            if (BookAuthorLinkDb.tableExists(con))
                BookAuthorLinkDb.dropTable(con);

            if (BookDb.tableExists(con))
                BookDb.dropTable(con);
            BookDb.createTable(con);

            if (PublisherDb.tableExists(con))
                PublisherDb.dropTable(con);
            PublisherDb.createTable(con);

            BookDb.insert(con, book1);
            BookDb.insert(con, book2);
            PublisherDb.insert(con, publisher);
            BookAuthorLinkDb.createTable(con);
        } catch (SQLException e) {
            logger.error("Failed to insert book or publisher record.", e);
            fail();
        }
    }

    @Test
    @Order(1)
    void insert() {
        try (Connection con = DbConnection.getTestConnection()) {
            BookAuthorLinkDb.insert(con, 1, 1);
        } catch (SQLException e) {
            logger.error("Failed to insert a Book-Publisher link record", e);
            fail();
        }
    }

    @Test
    @Order(2)
    void query() {
        try (Connection con = DbConnection.getTestConnection()) {
            String rec = BookAuthorLinkDb.queryForBook(con, 1);
            assertNotNull(rec);
        } catch (SQLException e) {
            logger.error("Failed to query book-publisher link record", e);
            fail();
        }
    }

    @Test
    @Order(3)
    void update() {
        try (Connection con = DbConnection.getTestConnection()) {
            BookAuthorLinkDb.update(con, 1, 2, 1);

            String record = BookAuthorLinkDb.queryForBook(con, 1);
            assertNull(record);

            record = BookAuthorLinkDb.queryForBook(con, 2);
            assertNotNull(record);
        } catch (SQLException e) {
            logger.error("Failed to update ", e);
            fail();
        }
    }
}
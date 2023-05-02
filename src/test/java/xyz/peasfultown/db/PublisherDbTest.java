package xyz.peasfultown.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.base.Publisher;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
public class PublisherDbTest {
    private static final Logger logger = LoggerFactory.getLogger(PublisherDbTest.class);

    static Publisher p1;
    static Publisher p2;
    static Publisher p3;

    static Connection con;

    /**
     * Set-ups and Tear-downs
     */
    @BeforeAll
    static void setup() {
        logger.info("---Starting PublisherDb Test---");

        p1 = new Publisher("Harper Collins");
        p2 = new Publisher("Penguin Random House");
        p3 = new Publisher("Gollancz");

        establishConnection();
        try {
            logger.info("Creating Publisher table");
            boolean tableExists = PublisherDb.tableExists(con);

            if (tableExists) {
                PublisherDb.dropTable(con);
            }

            PublisherDb.createTable(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    @BeforeEach
    void setupEach() {
        logger.info("Setting up before each");
        establishConnection();
    }

    @AfterEach
    void teardownEach() {
        logger.info("Tearing down each");
        closeConnection();
    }

    @AfterAll
    static void teardown() {
        logger.info("---Tearing down after all");
        establishConnection();
        try {
            assertTrue(PublisherDb.tableExists(con));
            PublisherDb.dropTable(con);
            assertFalse(PublisherDb.tableExists(con));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        closeConnection();
        logger.info("All tests done");
    }

    /**
     * Tests correctness when inserting data into the 'publishers' table
     * in SQLite and tests the getPublisherById method at the same time
     */
    @Test
    @Order(1)
    void insertPublisherToTable() {
        logger.info("Executing test for inserting publisher into table");
        Publisher insertedPub1 = null;
        Publisher insertedPub2 = null;
        Publisher insertedPub3 = null;

        String queriedPub1 = null;
        String queriedPub2 = null;
        String queriedPub3 = null;
        try {
            insertedPub1 = PublisherDb.insert(con, p1);
            insertedPub2 = PublisherDb.insert(con, p2);
            insertedPub3 = PublisherDb.insert(con, p3);

            queriedPub1 = PublisherDb.queryById(con, 1);
            queriedPub2 = PublisherDb.queryById(con, 2);
            queriedPub3 = PublisherDb.queryById(con, 3);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        assertEquals(p1, insertedPub1);
        assertEquals(p2, insertedPub2);
        assertEquals(p3, insertedPub3);
        assertEquals(p1.getName(), queriedPub1.split(",")[1], "Queried publisher should be correct");
        assertEquals(p2.getName(), queriedPub2.split(",")[1], "Queried publisher should be correct");
        assertEquals(p3.getName(), queriedPub3.split(",")[1], "Queried publisher should be correct");
    }

    /**
     * Tests correctness when querying publisher information by their name
     */
    @Test
    @Order(2)
    void getPublisherByName() {
        logger.info("Executing test for getting publisher by name");
        String queriedPub1 = null;
        String queriedPub2 = null;
        String queriedPub3 = null;
        String queriedPub4 = null;

        try {
            queriedPub1 = PublisherDb.queryByName(con, p1.getName());
            queriedPub2 = PublisherDb.queryByName(con, p2.getName());
            queriedPub3 = PublisherDb.queryByName(con, p3.getName());
            queriedPub4 = PublisherDb.queryByName(con, "Non-existent");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        logger.info("Queried publisher: {}", queriedPub1);
        assertEquals(p1.getName(), queriedPub1.split(",")[1]);
        assertEquals(p2.getName(), queriedPub2.split(",")[1]);
        assertEquals(p3.getName(), queriedPub3.split(",")[1]);
        assertNull(queriedPub4);
    }


    /**
     * Tests correctness when updating publishers from the table by their name
     */
    @Test
    @Order(3)
    void updatePublisherById() {
        logger.info("Executing test for updating publishers");

        Publisher newPub1 = new Publisher("Pearson Education");
        Publisher newPub2 = new Publisher("Wiley");

        String updatedRow1 = null;
        String updatedRow2 = null;

        try {
            PublisherDb.update(con, 1, newPub1);
            PublisherDb.update(con, 2, newPub2);

            updatedRow1 = PublisherDb.queryById(con, 1);
            updatedRow2 = PublisherDb.queryById(con, 2);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        assertEquals(newPub1.getName(), updatedRow1.split(",")[1]);
        assertEquals(newPub2.getName(), updatedRow2.split(",")[1]);
    }

    /**
     * Tests correctness when removing publishers from the table by their id
     */
    @Test
    @Order(4)
    void deletePublisherById() {
        logger.info("Executing test for removing publisher by their id");

        try {
            assertEquals(3, PublisherDb.queryAll(con).size());
            assertNotNull(PublisherDb.queryById(con, 1));
            PublisherDb.deleteById(con, 1);
            assertNull(PublisherDb.queryById(con, 1));
            assertEquals(2, PublisherDb.queryAll(con).size());
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Tests correctness when removing publishers from the table by their name
     */
    @Test
    @Order(5)
    void removePublisherByName() {
        logger.info("Executing test for removing publisher by their name");

        try {
            assertEquals(2, PublisherDb.queryAll(con).size());
            assertNotNull(PublisherDb.queryByName(con, "Wiley"));
            PublisherDb.deleteByName(con, "Wiley");
            assertEquals(1, PublisherDb.queryAll(con).size());
            assertNull(PublisherDb.queryByName(con, "Wiley"));
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Helper methods
     */
    static void establishConnection() {
        try {
            con = DbConnection.getTestConnection();
            logger.info("Connection established");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    static void closeConnection() {
        try {
            DbConnection.close(con);
            logger.info("Connection closed");
        } catch (SQLException e) {
            logger.error(e.getMessage());
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

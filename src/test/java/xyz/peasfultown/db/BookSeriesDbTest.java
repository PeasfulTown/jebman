package xyz.peasfultown.db;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.BookSeries;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

@TestMethodOrder(OrderAnnotation.class)
public class BookSeriesDbTest {
    private static final Logger logger = LoggerFactory.getLogger(BookSeriesDbTest.class);
    private static Connection con;

    private static Book b1, b2, b3;
    private static BookSeries bs1, bs2, bs3;

    /**
     * Set-ups and Tear-downs
     */
    @BeforeAll
    static void setup() {
        logger.info("---Setting up Book Series test---");

        b1 = new Book();
        b1.setTitle("The Blade Itself");

        b2 = new Book();
        b2.setTitle("Before They Are Hanged");

        b3 = new Book();
        b3.setTitle("Last Argument Of Kings");

        bs1 = new BookSeries("The First Law");
        bs2 = new BookSeries("Outlander");
        bs3 = new BookSeries("Teixcalaan");

        establishConnection();

        try {
            boolean tableExists = BookSeriesDb.tableExists(con);
            logger.info("Table exists: {}", tableExists);
            if (tableExists) {
                BookSeriesDb.dropTable(con);
            }
            logger.info("Creating table");
            BookSeriesDb.createTable(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
    @BeforeEach
    void setupEach() {
        logger.info("Setting up each");
        establishConnection();
    }

    @AfterEach
    void teardownEach() {
        logger.info("Tearing down after each");
        closeConnection();
    }

    @AfterAll
    static void teardown() {
        logger.info("---Tearing down after all---");

        establishConnection();

        try {
            logger.info("Dropping table");
            BookSeriesDb.dropTable(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        closeConnection();
    }

    /**
     * Test inserting book series into the table
     * and fetching them using their id
     */
    @Test
    @Order(1)
    void testInsert() {
        logger.info("Executing test for inserting and fetching by id");
        BookSeries qbs1 = null;
        BookSeries qbs2 = null;
        BookSeries qbs3 = null;

        try {
            BookSeriesDb.insert(con, bs1);
            BookSeriesDb.insert(con, bs2);
            BookSeriesDb.insert(con, bs3);

            qbs1 = BookSeriesDb.getBookSeriesById(con, 1);
            qbs2 = BookSeriesDb.getBookSeriesById(con, 2);
            qbs3 = BookSeriesDb.getBookSeriesById(con, 3);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        assertEquals(bs1, qbs1);
        assertEquals(bs2, qbs2);
        assertEquals(bs3, qbs3);
    }

    @Test
    @Order(2)
    void updateById() {
        logger.info("Executing test for updating book series record by id");

        List<BookSeries> bslistBefore = null;
        List<BookSeries> bslistAfter = null;

        int[] idsToUpdate = null;

        BookSeries ubs1 = new BookSeries("New Book Series");
        BookSeries ubs2 = new BookSeries("Some Other Book Series");

        List<BookSeries> ubslist = new ArrayList<>();
        ubslist.add(ubs1);
        ubslist.add(ubs2);

        List<BookSeries> ubslistRet = new ArrayList<>();

        try {
            bslistBefore = BookSeriesDb.queryAll(con);

            idsToUpdate = new int[bslistBefore.size()];

            for (int i = 0; i < bslistBefore.size(); i++) {
                idsToUpdate[i] = bslistBefore.get(i).getId();
            }

            for (int i = 0; i < ubslist.size(); i++) {
                ubslistRet.add(BookSeriesDb.update(con, idsToUpdate[i], ubslist.get(i)));
            }

            bslistAfter = BookSeriesDb.queryAll(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Record before update: {}, after update: {}", bslistBefore.get(0), bslistAfter.get(0));
        assertEquals(ubslist.get(0).getName(), bslistAfter.get(0).getName());
        assertEquals(ubslist.get(1).getName(), bslistAfter.get(1).getName());
        assertEquals(2, ubslistRet.size());
        assertEquals(bslistAfter.get(0), ubslistRet.get(0));
        assertEquals(bslistAfter.get(1), ubslistRet.get(1));
    }

    /**
     * Helper Methods
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
            if (con != null) {
                con.close();
                logger.info("Connection closed");
            }
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

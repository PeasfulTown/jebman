package xyz.peasfultown.db;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.Series;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;

@TestMethodOrder(OrderAnnotation.class)
public class SeriesDbTest {
    private static final Logger logger = LoggerFactory.getLogger(SeriesDbTest.class);
    private static Connection con;

    private static Book b1, b2, b3;
    private static Series bs1, bs2, bs3;

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

        bs1 = new Series("The First Law");
        bs2 = new Series("Outlander");
        bs3 = new Series("Teixcalaan");

        establishConnection();

        try {
            boolean tableExists = SeriesDb.tableExists(con);
            logger.info("Table exists: {}", tableExists);
            if (tableExists) {
                SeriesDb.dropTable(con);
            }
            logger.info("Creating table");
            SeriesDb.createTable(con);
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
            SeriesDb.dropTable(con);
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
        String qbs1 = null;
        String qbs2 = null;
        String qbs3 = null;

        try {
            SeriesDb.insert(con, bs1);
            SeriesDb.insert(con, bs2);
            SeriesDb.insert(con, bs3);

            qbs1 = SeriesDb.queryById(con, 1);
            qbs2 = SeriesDb.queryById(con, 2);
            qbs3 = SeriesDb.queryById(con, 3);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Object: {}", bs1);
        logger.info("Record: {}", qbs1);
        assertEquals(bs1.toString(), qbs1);
        assertEquals(bs2.toString(), qbs2);
        assertEquals(bs3.toString(), qbs3);
    }

    @Test
    @Order(2)
    void updateById() {
        logger.info("Executing test for updating book series record by id");

        List<String> bslistBefore = null;
        List<String> bslistAfter = null;

        int[] idsToUpdate = null;

        Series ubs1 = new Series("New Book Series");
        Series ubs2 = new Series("Some Other Book Series");

        List<Series> ubslist = new ArrayList<>();
        ubslist.add(ubs1);
        ubslist.add(ubs2);

        List<Series> ubslistRet = new ArrayList<>();

        try {
            bslistBefore = SeriesDb.queryAll(con);

            idsToUpdate = new int[bslistBefore.size()];

            for (int i = 0; i < bslistBefore.size(); i++) {
                idsToUpdate[i] = Integer.valueOf(bslistBefore.get(i).split(",")[0]);
            }

            for (int i = 0; i < ubslist.size(); i++) {
                ubslistRet.add(SeriesDb.update(con, idsToUpdate[i], ubslist.get(i)));
                ubslist.get(i).setId(idsToUpdate[i]);
            }

            bslistAfter = SeriesDb.queryAll(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Record before update: {}", bslistBefore.get(0));
        logger.info("Record after update: {}", bslistAfter.get(0));
        assertEquals(ubslist.get(0).getName(), bslistAfter.get(0).split(",")[1]);
        assertEquals(ubslist.get(1).getName(), bslistAfter.get(1).split(",")[1]);
        assertEquals(2, ubslistRet.size());
        assertEquals(bslistAfter.get(0).toString(), ubslistRet.get(0).toString());
        assertEquals(bslistAfter.get(1).toString(), ubslistRet.get(1).toString());
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

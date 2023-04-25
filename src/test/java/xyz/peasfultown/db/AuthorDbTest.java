package xyz.peasfultown.db;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.base.Author;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthorDbTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthorDbTest.class);
    static Connection con;

    static List<Author> authorList;
    static Author a1, a2, a3, a4, a5;

    /**
     * Set-ups and Tear-downs
     */
    @BeforeAll
    static void setup() {
        a1 = new Author("Joe Abercrombie");
        a2 = new Author("Victoria Holt");
        a3 = new Author("Brian W. Kernighan");
        a4 = new Author("Dennis M. Ritchie");
        a5 = new Author("Daphne du Maurier");

        authorList = new ArrayList<>();

        authorList.add(a1);
        authorList.add(a2);
        authorList.add(a3);
        authorList.add(a4);
        authorList.add(a5);

        establishConnection();

        try {
            boolean tableExists = AuthorDb.tableExists(con);

            if (tableExists) {
                AuthorDb.dropTable(con);
            }

            logger.info("Creating table");
            AuthorDb.createTable(con);
            assertEquals(0, AuthorDb.queryAll(con).size());
        } catch (SQLException e) {
            fail(e);
        }

        closeConnection();
    }

    @BeforeEach
    void setupEach() {
        establishConnection();
    }

    @AfterEach
    void teardownEach() {
        closeConnection();
    }

    /**
     * Test insert(Connection con, Author authorToInsert) method of AuthorDb.
     * The test tries to insert all Author objects from the List of Author(s)
     * `authorList`.
     * <p>
     * Verifies success by comparing number of rows before and after insertion,
     * and by comparing fields between the object that were inserted and the
     * objects actually contained within the database.
     */
    @Test
    @Order(1)
    void insertSinglesByObject() {
        logger.info("Executing test for inserting single record");
        int rFirst = 0;
        int rAfter = 0;
        List<Author> aulistAfter = null;

        try {
            rFirst = AuthorDb.queryAll(con).size();
            for (int i = 0; i < authorList.size(); i++) {
                AuthorDb.insert(con, authorList.get(i));
            }
            aulistAfter = AuthorDb.queryAll(con);
            rAfter = aulistAfter.size();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Number of rows before insertion: {}, number of rows after insertion: {}", rFirst, rAfter);
        logger.info("Object in query result: {}", aulistAfter.get(3));
        assertTrue(rAfter > rFirst, "Number of rows after insertion should be higher " +
                "than number of rows before insertion.");
        assertNotNull(aulistAfter, "The queried author database should not be null.");
        assertEquals(authorList.size(), rAfter);
        assertEquals(authorList.get(1).getName(), aulistAfter.get(1).getName());
        assertNotEquals(0, aulistAfter.get(3).getId());
    }

    /**
     * Test deleteById(Connection con, int idToDelete) method of AuthorDb by
     * querying all records to get all their ids, and then calling the deleteById()
     * method on each of those ids. This test should delete all records from the table.
     */
    @Test
    @Order(2)
    void deleteSinglesById() {
        logger.info("Executing test for deleting single records");
        int rFirst = 0;
        int rAfter = 0;
        List<Author> aulist = null;
        try {
            aulist = AuthorDb.queryAll(con);

            // Fetching the ids
            int[] idsToDelete = new int[aulist.size()];
            for (int i = 0; i < aulist.size(); i++) {
                idsToDelete[i] = aulist.get(i).getId();
            }

            rFirst = aulist.size();

            // The actual delete operation
            for (int i = 0; i < idsToDelete.length; i++) {
                AuthorDb.deleteById(con, idsToDelete[i]);
            }

            rAfter = AuthorDb.queryAll(con).size();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Number of rows before deletion: {}, number of rows after deletion: {}", rFirst, rAfter);
        assertTrue(rFirst > rAfter, "Number of rows before deletion should " +
                "be higher than number of rows before deletion.");
        assertEquals(0, rAfter);
    }

    /**
     * Test inserting authors.
     */
    @Test
    @Order(3)
    void insertBatchByObjects() {
        logger.info("Executing test for inserting batch of records");
        int rFirst = 0;
        int rAfter = 0;
        List<Author> aulist = null;
        List<Author> aulistAfter = null;

        try {
            aulist = AuthorDb.queryAll(con);
            rFirst = aulist.size();
            AuthorDb.insert(con, authorList);

            aulistAfter = AuthorDb.queryAll(con);
            rAfter = aulistAfter.size();
        } catch (SQLException e) {
            fail(e);
        }

        logger.info("Object in db: {}", aulistAfter.get(3));
        logger.info("Number of rows before insertion: {}, number of rows after insertion: {}", rFirst, rAfter);
        assertTrue(rFirst < rAfter, "Number of rows before insertion should be lower " +
                "than number of rows after insertion.");
        assertEquals(authorList.size(), aulistAfter.size());
    }

    /**
     * Test querying for author record by their id.
     */
    @Test
    @Order(4)
    void queryById() {
        logger.info("Executing test for getting author by id in table");
        Author auQueried = null;

        try {
            List<Author> aulist = AuthorDb.queryAll(con);
            auQueried = AuthorDb.queryById(con, aulist.get(0).getId());
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Author in db: {}", auQueried);
        assertNotNull(auQueried);
    }

    /**
     * Test for updating single records in the database by their ids.
     */
    @Test
    @Order(5)
    void updateSinglesById() {
        logger.info("Executing test for updating single records");
        List<Author> aulist = null;


        Author auNew1 = null;
        Author auNew2 = null;
        Author auNew3 = null;

        try {
            aulist = AuthorDb.queryAll(con);
            logger.info("Obj before update: {}", aulist.get(0));

            auNew1 = AuthorDb.updateById(con, aulist.get(0).getId(), "Martin Fowler");
            auNew2 = AuthorDb.updateById(con, aulist.get(1).getId(), "Harold Abelson");
            auNew3 = AuthorDb.updateById(con, aulist.get(2).getId(), "Simon Singh");

            aulist = AuthorDb.queryAll(con);
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Obj after update: {}", aulist.get(0));

        assertEquals("Martin Fowler", auNew1.getName());
        assertEquals("Harold Abelson", auNew2.getName());
        assertEquals("Simon Singh", auNew3.getName());
    }

    /**
     * Test batch deletion by id.
     */
    @Test
    @Order(6)
    void deleteBatchById() {
        logger.info("Executing test for deleting batch of records");
        int rFirst = 0;
        int rAfter = 0;
        List<Author> aulist = null;

        try {
            aulist = AuthorDb.queryAll(con);

            int[] idsToDelete = new int[aulist.size()];

            // Collect ids
            for (int i = 0; i < aulist.size(); i++) {
                idsToDelete[i] = aulist.get(i).getId();
            }

            rFirst = AuthorDb.queryAll(con).size();

            AuthorDb.deleteByIds(con, idsToDelete);

            rAfter = AuthorDb.queryAll(con).size();
        } catch (SQLException e) {
            logger.error(e.getMessage());
            fail(e);
        }

        logger.info("Number of rows before deletion: {}, number of rows after deletion: {}", rFirst, rAfter);
        assertTrue(rFirst > rAfter, "Number of rows before batch deletion should be " +
                "higher than number of rows after deletion.");
        assertEquals(0, rAfter);
    }

    static void establishConnection() {
        try {
            con = DbConnection.getTestConnection();
        } catch (SQLException e) {
            fail(e);
        }
    }

    static void closeConnection() {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            fail(e);
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

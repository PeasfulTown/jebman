/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SQLite3 queries for the `authors` table.
 */
package xyz.peasfultown.db;

import xyz.peasfultown.base.Author;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthorDb {
    private static final String SQL_SELECT_TABLE = new StringBuilder()
            .append("SELECT name FROM sqlite_master ")
            .append("WHERE type='table' AND name='authors';")
            .toString();

    private static final String SQL_CREATE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS authors (")
            .append("id INTEGER")
            .append("   CONSTRAINT pk_authors PRIMARY KEY AUTOINCREMENT,")
            .append("name TEXT")
            .append("   COLLATE NOCASE")
            .append("   CONSTRAINT uq_authors_n UNIQUE")
            .append(");")
            .toString();

    private static final String SQL_SELECT_LAST_INSERT_ID = "SELECT last_insert_rowid() AS id;";

    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS authors;";

    private static final String SQL_SELECT_ALL = "SELECT * FROM authors;";

    private static final String SQL_SELECT_BY_ID = "SELECT * FROM authors WHERE id=?;";

    private static final String SQL_INSERT_AUTHOR = "INSERT INTO authors (name) VALUES (?);";

    private static final String SQL_DELETE_BY_ID = "DELETE FROM authors WHERE id=?;";

    private static final String SQL_UPDATE_AUTHOR = "UPDATE authors SET name=? WHERE id=?;";

    /**
     * Query for all Author records on the table
     * on the provided Connection and returns an ArrayList
     * of Author object representation of said Author records.
     *
     * @param con The database connection to query all author records on.
     * @return List object of Author objects
     * @throws SQLException
     */
    public static List<Author> queryAll(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        List<Author> aulist = new ArrayList<>();

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_SELECT_ALL);

            while (rs.next()) {
                Author newAu = new Author(rs.getString("name"));
                newAu.setId(rs.getInt("id"));
                aulist.add(newAu);
            }
        } finally {
            closeResources(stmt, rs);
        }

        return aulist;
    }

    public static Author queryById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Author author = null;

        try {
            stmt = con.prepareStatement(SQL_SELECT_BY_ID);
            stmt.setInt(1, id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                author = new Author("");
                author.setId(rs.getInt("id"));
                author.setName(rs.getString("name"));
            } else {
                throw new SQLException("Querying author by id failed, no returned values.");
            }
        } finally {
            closeResources(stmt, rs);
        }

        return author;
    }

    /**
     * Insert a provided Author record into SQLite database on the provided Connection.
     *
     * @param con            SQLite database connection to insert to.
     * @param authorToInsert Author object to insert to database.
     * @throws SQLException
     */
    public static Author insert(Connection con, Author authorToInsert) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Author newAuthor = null;

        try {
            con.setAutoCommit(false);

            stmt = con.prepareStatement(SQL_INSERT_AUTHOR);
            stmt.setString(1, authorToInsert.getName());

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Inserting author failed, no rows affected.");
            }

            newAuthor = new Author("");
            newAuthor.setId(queryLastInsertId(con));
            newAuthor.setName(authorToInsert.getName());

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return newAuthor;
    }

    /**
     * Batch insert a list of authors objects into the <i>authors</i> database.
     *
     * @param con             SQLite database connection to insert to.
     * @param authorsToInsert List of author objects to insert to database.
     * @throws SQLException
     */
    public static List<Author> insert(Connection con, List<Author> authorsToInsert) throws SQLException {
        PreparedStatement stmt = null;
        Savepoint sp = null;

        List<Author> newAuthorsToReturn = null;

        try {
            con.setAutoCommit(false);
            sp = con.setSavepoint();
            stmt = con.prepareStatement(SQL_INSERT_AUTHOR);
            newAuthorsToReturn = new ArrayList<>();

            for (int i = 0; i < authorsToInsert.size(); i++) {
                stmt.setString(1, authorsToInsert.get(i).getName());

                int rows = stmt.executeUpdate();

                if (rows == 0) {
                    con.rollback(sp);
                    newAuthorsToReturn = null;
                    throw new SQLException("Batch insert failed at index: " + i + ", no rows affected.");
                }

                Author newAu = new Author("");
                newAu.setId(queryLastInsertId(con));
                newAu.setName(authorsToInsert.get(i).getName());
                newAuthorsToReturn.add(newAu);
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return newAuthorsToReturn;
    }

    /**
     * Detele author record from the database by its ID.
     *
     * @param con SQLite database connection to delete from.
     * @param id  ID of the author record in the database.
     * @throws SQLException
     */
    public static void deleteById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_DELETE_BY_ID);
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Deleting author failed, no rows affected.");
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }
    }

    public static void deleteByIds(Connection con, int[] idsToDelete) throws SQLException {
        PreparedStatement stmt = null;
        Savepoint sp = null;

        try {
            con.setAutoCommit(false);
            sp = con.setSavepoint();
            stmt = con.prepareStatement(SQL_DELETE_BY_ID);

            for (int i = 0; i < idsToDelete.length; i++) {
                stmt.setInt(1, idsToDelete[i]);
                int rows = stmt.executeUpdate();

                if (rows == 0) {
                    con.rollback(sp);
                    throw new SQLException("Batch deletion failed, no rows affected.");
                }
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }
    }

    /**
     * Update an author record on the database by their id.
     *
     * @param con     The database connection to update record in.
     * @param id      The ID of the row to update.
     * @param newName Name to update author with.
     * @return
     * @throws SQLException
     */
    public static Author updateById(Connection con, int id, String newName) throws SQLException {
        PreparedStatement stmt = null;
        Author updatedAuthor = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_UPDATE_AUTHOR);
            stmt.setString(1, newName);
            stmt.setInt(2, id);

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Updating author failed, no rows affected.");
            }

            updatedAuthor = new Author("");
            updatedAuthor.setId(queryLastInsertId(con));
            updatedAuthor.setName(newName);

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return updatedAuthor;
    }

    /**
     * Create an <i>authors</i> table on the SQLite database provided by
     * the connection param.
     *
     * @param con The database connection to create the database on.
     * @throws SQLException
     */
    public static void createTable(Connection con) throws SQLException {
        Statement stmt = null;

        try {
            con.setAutoCommit(false);

            stmt = con.createStatement();
            stmt.executeUpdate(SQL_CREATE_TABLE);

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }
    }

    /**
     * Drop the <i>authors</i> table on the provided connection param.
     *
     * @param con
     * @throws SQLException
     */
    public static void dropTable(Connection con) throws SQLException {
        Statement stmt = null;

        try {
            con.setAutoCommit(false);

            stmt = con.createStatement();
            stmt.executeUpdate(SQL_DROP_TABLE);

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }
    }

    /**
     * Checks if the <i>authors</i> table exists on the provided connection.
     *
     * @param con Connection to check for table.
     * @return true if the table exists on the connection, otherwise returns false.
     * @throws SQLException
     */
    public static boolean tableExists(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            stmt = con.createStatement();

            rs = stmt.executeQuery(SQL_SELECT_TABLE);
            if (rs.next()) {
                exists = true;
            }

        } finally {
            closeResources(stmt, rs);
        }

        return exists;
    }

    /**
     * Helper methods
     */
    private static int queryLastInsertId(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int id;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_SELECT_LAST_INSERT_ID);

            if (rs.next()) {
                id = rs.getInt("id");
            } else {
                throw new SQLException("Querying last inserted id failed.");
            }

        } finally {
            closeResources(stmt, rs);
        }

        return id;
    }

    private static void closeResources(Statement stmt) throws SQLException {
        if (stmt != null) {
            stmt.close();
        }
    }

    private static void closeResources(Statement stmt, ResultSet rs) throws SQLException {
        closeResources(stmt);

        if (rs != null) {
            rs.close();
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

/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SQLite3 queries for the `bookseries` table.
 */
package xyz.peasfultown.db;

import xyz.peasfultown.base.BookSeries;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookSeriesDb {
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS bookseries;";
    private static final String SQL_SELECT_TABLE = "SELECT name FROM sqlite_master WHERE name='bookseries';";
    private static final String SQL_CREATE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS bookseries (")
            .append("id INTEGER")
            .append("   CONSTRAINT pk_bookseries PRIMARY KEY AUTOINCREMENT,")
            .append("name TEXT")
            .append("   CONSTRAINT uq_bookseries_n UNIQUE")
            .append(");")
            .toString();

    private static final String SQL_SELECT_ALL = "SELECT * FROM bookseries;";
    private static final String SQL_SELECT_BOOK_BY_ID = "SELECT * FROM bookseries WHERE id=?;";
    private static final String SQL_INSERT_BOOK_SERIES = "INSERT INTO bookseries (name) VALUES (?);";
    private static final String SQL_UPDATE_BY_ID = "UPDATE bookseries SET name=? WHERE id=?;";

    public static List<BookSeries> queryAll(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        List<BookSeries> bslist = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_SELECT_ALL);

            bslist = new ArrayList<>();

            while (rs.next()) {
                BookSeries bs = new BookSeries(rs.getInt("id"), rs.getString("name"));
                bslist.add(bs);
            }

        } finally {
            closeResources(stmt, rs);
        }

        return bslist;
    }

    public static void insert(Connection con, BookSeries bookseries) throws SQLException {
        PreparedStatement stmt = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_INSERT_BOOK_SERIES);
            stmt.setString(1, bookseries.getName());
            stmt.executeUpdate();

            con.commit();
        } finally {
            con.setAutoCommit(true);

            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public static BookSeries update(Connection con, int id, BookSeries updatedBookSeries) throws SQLException {
        PreparedStatement stmt = null;
        BookSeries bs = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_UPDATE_BY_ID);
            stmt.setString(1, updatedBookSeries.getName());
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException ("Updating book series record failed, no rows affected.");
            }

            bs = new BookSeries(id, updatedBookSeries.getName());

        } finally {
            con.setAutoCommit(true);
        }

        return bs;
    }

    public static BookSeries getBookSeriesById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        BookSeries bs = null;

        try {
            stmt = con.prepareStatement(SQL_SELECT_BOOK_BY_ID);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                bs = new BookSeries(rs.getString("name"));
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }

            if (rs != null) {
                rs.close();
            }
        }

        return bs;
    }

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
            if (stmt != null) {
                stmt.close();
            }

            if (rs != null) {
                rs.close();
            }
        }

        return exists;
    }

    public static void createTable(Connection con) throws SQLException {
        Statement stmt = null;

        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.executeUpdate(SQL_CREATE_TABLE);
            con.commit();
        } finally {
            con.setAutoCommit(true);
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public static void dropTable(Connection con) throws SQLException {
        Statement stmt = null;

        try {
            con.setAutoCommit(false);
            stmt = con.createStatement();
            stmt.executeUpdate(SQL_DROP_TABLE);
            con.commit();
        } finally {
            con.setAutoCommit(true);

            if (stmt != null) {
                stmt.close();
            }
        }

    }

    /**
     * Helper Methods
     */
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

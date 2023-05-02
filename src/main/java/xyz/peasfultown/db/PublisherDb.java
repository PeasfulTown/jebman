/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SQLite3 queries for the `publishers` table.
 */
package xyz.peasfultown.db;

import xyz.peasfultown.base.Publisher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class PublisherDb {
    private static final String SQL_FIND_TABLE = new StringBuilder()
            .append("SELECT name FROM sqlite_master ")
            .append("WHERE type='table' AND name='publishers';")
            .toString();
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS publishers;";
    private static final String SQL_CREATE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS publishers (")
            .append("id INTEGER")
            .append("   CONSTRAINT pk_publishers PRIMARY KEY AUTOINCREMENT,")
            .append("name TEXT")
            .append("   COLLATE NOCASE")
            .append("   CONSTRAINT uq_publishers_n UNIQUE")
            .append(");")
            .toString();
    private static final String SQL_SELECT_LAST_INSERT_ID = "SELECT last_insert_rowid() AS id;";
    private static final String SQL_SELECT_ALL_PUBLISHERS = "SELECT * FROM publishers;";
    private static final String SQL_INSERT_PUBLISHER = "INSERT INTO publishers (name) VALUES (?);";
    private static final String SQL_QUERY_BY_ID = "SELECT * FROM publishers WHERE id=?;";
    private static final String SQL_QUERY_BY_NAME = "SELECT * FROM publishers WHERE lower(name)=?;";
    private static final String SQL_UPDATE_PUBLISHER_BY_ID = "UPDATE publishers SET name=? WHERE id=?;";
    private static final String SQL_DELETE_PUBLISHER_BY_ID = "DELETE FROM publishers WHERE id=?;";
    private static final String SQL_DELETE_PUBLISHER_BY_NAME = "DELETE FROM publishers WHERE lower(name)=?;";

    public static Publisher insert(Connection con, Publisher publisher) throws SQLException {
        PreparedStatement stmt = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_INSERT_PUBLISHER);
            stmt.setString(1, publisher.getName());
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("Inserting publisher record failed, no rows affected.");
            }
            publisher.setId(getLastInsertedId(con));
            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return publisher;
    }

    public static List<String> queryAll(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        List<String> publishers = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_SELECT_ALL_PUBLISHERS);

            if (publishers == null) {
                publishers = new ArrayList<>();
            }

            while (rs.next()) {
                StringJoiner sj = new StringJoiner(",");

                sj.add(String.valueOf(rs.getInt("id")))
                        .add(rs.getString("name"));

                publishers.add(sj.toString());
            }
        } finally {
            closeResources(stmt, rs);
        }

        if (publishers.size() == 0) {
            return null;
        }

        return publishers;
    }

    public static String queryById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringJoiner sj = new StringJoiner(",");

        try {
            stmt = con.prepareStatement(SQL_QUERY_BY_ID);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                sj.add(String.valueOf(rs.getInt("id")))
                        .add(rs.getString("name"));
            }
        } finally {
            closeResources(stmt, rs);
        }

        if (sj.toString().length() == 0) {
            return null;
        }

        return sj.toString();
    }

    public static String queryByName(Connection con, String name) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringJoiner sj = new StringJoiner(",");

        try {
            stmt = con.prepareStatement(SQL_QUERY_BY_NAME);
            stmt.setString(1, name.toLowerCase());
            rs = stmt.executeQuery();
            if (rs.next()) {
                sj.add(String.valueOf(rs.getInt("id")))
                        .add(rs.getString("name"));
            }
        } finally {
            closeResources(stmt, rs);
        }

        if (sj.toString().length() == 0) {
            return null;
        }

        return sj.toString();
    }

    public static void update(Connection con, int id, Publisher updatedPub) throws SQLException {
        PreparedStatement stmt = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_UPDATE_PUBLISHER_BY_ID);
            stmt.setString(1, updatedPub.getName());
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();

            if (rows == 0)
                throw new SQLException("Updating publisher record failed, no rows affected.");

            con.commit();
        } finally {
            con.setAutoCommit(true);

            closeResources(stmt);
        }
    }

    public static void deleteById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;

        int rows = 0;
        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_DELETE_PUBLISHER_BY_ID);
            stmt.setInt(1, id);
            rows = stmt.executeUpdate();

            if (rows == 0)
                throw new SQLException("Record deletion failed, no rows affected.");

            con.commit();
        } finally {
            con.setAutoCommit(true);
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    public static void deleteByName(Connection con, String name) throws SQLException {
        PreparedStatement stmt = null;
        int rows = 0;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_DELETE_PUBLISHER_BY_NAME);
            stmt.setString(1, name.toLowerCase());
            rows = stmt.executeUpdate();

            if (rows == 0)
                throw new SQLException("Record deletion failed, no rows affected.");

            con.commit();
        } finally {
            con.setAutoCommit(true);
            if (stmt != null) {
                stmt.close();
            }
        }
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

    public static boolean tableExists(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(SQL_FIND_TABLE);

        if (rs.next()) {
            stmt.close();
            rs.close();
            return true;
        }

        stmt.close();
        rs.close();

        return false;
    }

    /**
     * Helper Methods
     */
    private static int getLastInsertedId(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int id = 0;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_SELECT_LAST_INSERT_ID);

            if (rs.next()) {
                id = rs.getInt("id");
            } else {
                throw new SQLException("Fetching last inserted ID failed, no results returned.");
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

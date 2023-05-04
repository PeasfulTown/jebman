/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SQLite3 queries for the `series` table.
 */
package xyz.peasfultown.db;

import xyz.peasfultown.domain.Series;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class SeriesDb {
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS series;";
    private static final String SQL_QUERY_TABLE = "SELECT name FROM sqlite_master WHERE name='series';";
    private static final String SQL_QUERY_LAST_INSERTED_ID = "SELECT last_insert_rowid() as id;";
    private static final String SQL_CREATE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS series (")
            .append("id INTEGER")
            .append("   CONSTRAINT pk_series PRIMARY KEY AUTOINCREMENT,")
            .append("name TEXT")
            .append("   CONSTRAINT uq_series_n UNIQUE")
            .append(");")
            .toString();

    private static final String SQL_QUERY_ALL = "SELECT * FROM series;";
    private static final String SQL_QUERY_BY_ID = "SELECT * FROM series WHERE id=?;";
    private static final String SQL_INSERT_SERIES = "INSERT INTO series (name) VALUES (?);";
    private static final String SQL_UPDATE_BY_ID = "UPDATE series SET name=? WHERE id=?;";

    public static List<String> queryAll(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_QUERY_ALL)) {
            List<String> bslist = new ArrayList<>();
            while (rs.next()) {
                String record = new StringJoiner(",")
                        .add(String.valueOf(rs.getInt("id")))
                        .add(rs.getString("name"))
                        .toString();

                bslist.add(record);
            }

            return bslist;
        } catch (SQLException e) {
            throw new SQLException("Failed to query for all records.", e);
        }
    }

    public static Series insert(Connection con, Series series) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_INSERT_SERIES)) {
            con.setAutoCommit(false);
            stmt.setString(1, series.getName());
            int rows = stmt.executeUpdate();
            if (rows == 0)
                throw new SQLException("No rows inserted.");
            series.setId(getLastInsertedRowId(con));
            con.commit();
        } catch (SQLException e) {
            throw new SQLException("Failed to insert a series record.", e);
        } finally {
            con.setAutoCommit(true);
        }

        return series;
    }

    public static Series update(Connection con, int id, Series updatedSeries) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_UPDATE_BY_ID)) {
            con.setAutoCommit(false);
            stmt.setString(1, updatedSeries.getName());
            stmt.setInt(2, id);
            int rows = stmt.executeUpdate();
            if (rows == 0)
                throw new SQLException("No rows affected.");
        } catch (SQLException e) {
            throw new SQLException("Failed to update series record.", e);
        } finally {
            con.setAutoCommit(true);
        }
        return updatedSeries;
    }

    public static String queryById(Connection con, int id) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_QUERY_BY_ID)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return new StringJoiner(",")
                            .add(String.valueOf(rs.getInt("id")))
                            .add(rs.getString("name")).toString();
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to query series record by ID.", e);
        }

        return null;
    }

    public static boolean tableExists(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_QUERY_TABLE)) {
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to query for table in sqlite.", e);
        }
        return false;
    }

    public static void createTable(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            stmt.executeUpdate(SQL_CREATE_TABLE);
            con.commit();
        } finally {
            con.setAutoCommit(true);
        }
    }

    public static void dropTable(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            con.setAutoCommit(false);
            stmt.executeUpdate(SQL_DROP_TABLE);
            con.commit();
        } finally {
            con.setAutoCommit(true);
        }

    }

    /**
     * Helper Methods
     */
    private static int getLastInsertedRowId(Connection con) throws SQLException {
        int id = 0;

        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_QUERY_LAST_INSERTED_ID)) {
            if (rs.next())
                id = rs.getInt("id");
            else
                throw new SQLException("Querying last inserted id failed, no values returned.");
        }

        return id;
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

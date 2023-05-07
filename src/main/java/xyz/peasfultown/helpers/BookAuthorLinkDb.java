/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Database interaction for the `books_authors_link` table, this is a joint table to establish
 * relationship between the `books` and the `authors` records.
 */
package xyz.peasfultown.helpers;

import java.sql.*;
import java.util.StringJoiner;

public class BookAuthorLinkDb {
    private static final String SQL_FIND_TABLE = new StringBuilder()
            .append("SELECT name FROM sqlite_master ")
            .append("WHERE type='table' AND name='books_author_link';")
            .toString();
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS books_authors_link;";
    private static final String SQL_CREATE_TABLE = new StringBuilder()
            .append("CREATE TABLE books_authors_link (")
            .append("id INTEGER")
            .append("   CONSTRAINT pk_books_authors PRIMARY KEY,")
            .append("book_id INTEGER")
            .append("   CONSTRAINT fk_books_authors_b REFERENCES books (id),")
            .append("author_id INTEGER")
            .append("   CONSTRAINT fk_books_authors_p REFERENCES authors (id)")
            .append(");")
            .toString();
    private static final String SQL_INSERT = "INSERT INTO books_authors_link (book_id, author_id) VALUES (?,?);";
    private static final String SQL_UPDATE = "UPDATE books_authors_link SET book_id=?, author_id=? WHERE id=?;";
    private static final String SQL_QUERY_BOOK = "SELECT * FROM books_authors_link WHERE book_id=?;";

    public static void insert(Connection con, int bookId, int authorId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_INSERT))  {
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException ("No rows inserted.");
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to insert books-authors link record.", e);
        }
    }

    public static void update(Connection con, int rowId, int bookId, int authorId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_UPDATE)) {
            stmt.setInt(1, bookId);
            stmt.setInt(2, authorId);
            stmt.setInt(3, rowId);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new SQLException("No rows affected.");
            }
        } catch (SQLException e) {
            throw new SQLException("Failed to update books-authors link record.", e);
        }
    }

    public static String queryForBook(Connection con, int bookId) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(SQL_QUERY_BOOK)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new StringJoiner(",")
                            .add(String.valueOf(rs.getInt("id")))
                            .add(String.valueOf(rs.getInt("book_id")))
                            .add(String.valueOf(rs.getInt("author_id")))
                            .toString();
                }
            }
        } catch (SQLException e) {
            throw new SQLException ("Failed to query for bookId " + bookId + " in books-authors link table.", e);
        }

        return null;
    }

    public static void createTable(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(SQL_CREATE_TABLE);
        } catch (SQLException e) {
            throw new SQLException("Create books_authors_link table failed.", e);
        }
    }

    public static void dropTable(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            stmt.executeUpdate(SQL_DROP_TABLE);
        } catch (SQLException e) {
            throw new SQLException ("Failed to drop books_authors_link table", e);
        }
    }

    public static boolean tableExists(Connection con) throws SQLException {
        try (Statement stmt = con.createStatement()) {
            try (ResultSet rs = stmt.executeQuery(SQL_FIND_TABLE)) {
                if (rs.next()) {
                    return true;
                }
            }
        } catch (SQLException e) {
            throw new SQLException ("Failed to query for books_authors_link table in sqlite_master", e);
        }

        return false;
    }
}

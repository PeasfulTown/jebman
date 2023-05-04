/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SQLite3 queries for the `books` table.
 */
package xyz.peasfultown.db;

import xyz.peasfultown.base.Book;

import java.sql.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class BookDb {
    private static final String COL_ID = "id";
    private static final String COL_ISBN = "isbn";
    private static final String COL_UUID = "uuid";
    private static final String COL_TITLE = "title";
    private static final String COL_SERIES_ID = "series_id";
    private static final String COL_SERIES_NUMBER = "series_number";
    private static final String COL_PUBLISHER_ID = "publisher_id";
    private static final String COL_DATE_ADDED = "date_added";
    private static final String COL_DATE_MODIFIED = "date_modified";
    private static final String COL_DATE_PUBLISHED = "date_published";
    private static final String SQL_FIND_TABLE = new StringBuilder()
            .append("SELECT name FROM sqlite_master ")
            .append("WHERE type='table' AND name='books';")
            .toString();
    private static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS books;";
    private static final String SQL_CREATE_TABLE = new StringBuilder()
            .append("CREATE TABLE IF NOT EXISTS books (")
            .append("id INTEGER")
            .append("   CONSTRAINT pk_books PRIMARY KEY AUTOINCREMENT,")
            .append("isbn TEXT")
            .append("   COLLATE NOCASE")
            .append("   CONSTRAINT df_books_i DEFAULT \"\",")
            .append("uuid TEXT")
            .append("   COLLATE NOCASE")
            .append("   CONSTRAINT df_books_u DEFAULT \"\",")
            .append("title TEXT")
            .append("   NOT NULL")
            .append("   COLLATE NOCASE")
            .append("   CONSTRAINT uq_books_t UNIQUE")
            .append("   CONSTRAINT df_books_t DEFAULT \"Unknown\",")
            .append("series_id INTEGER")
            .append("   CONSTRAINT fk_books_sid REFERENCES series (id),")
            .append("series_number REAL")
            .append("   NOT NULL")
            .append("   CONSTRAINT df_books_snum DEFAULT 1.0,")
            .append("publisher_id INTEGER")
            .append("   CONSTRAINT fk_books_pid REFERENCES publishers (id),")
            .append("date_published TEXT")
            .append("   NOT NULL")
            .append("   CONSTRAINT df_books_dp DEFAULT (strftime('%Y-%m-%dT00:00:00Z', 'now')),")
            .append("date_added TEXT")
            .append("   NOT NULL")
            .append("   CONSTRAINT df_books_da DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),")
            .append("date_modified TEXT")
            .append("   NOT NULL")
            .append("   CONSTRAINT df_books_dm DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))")
            .append(");")
            .toString();

    private static final String SQL_INSERT = new StringBuilder()
            .append("INSERT INTO books ")
            .append("(isbn, uuid, title, date_published) ")
            .append("VALUES (?,?,?,?);")
            .toString();

    private static final String SQL_UPDATE_BOOK = new StringBuilder()
            .append("UPDATE books ")
            .append("   SET isbn=?,")
            .append("   uuid=?,")
            .append("   title=?,")
            .append("   series_id=?,")
            .append("   series_number=?,")
            .append("   publisher_id=?,")
            .append("   date_published=?,")
            .append("   date_added=?,")
            .append("   date_modified=?")
            .append("       WHERE id=?;")
            .toString();

    private static final String SQL_QUERY_LAST_INSERTED_ROWID = "SELECT last_insert_rowid() as id;";

    private static final String SQL_DELETE_BY_ID = "DELETE FROM books WHERE id=?;";

    private static final String SQL_QUERY_ALL = "SELECT * FROM books;";
    private static final String SQL_QUERY_ALL_JOINED = new StringBuilder()
            .append("SELECT * FROM ")
            .append("(SELECT B.id,B.isbn,B.uuid,B.title,S.id AS series_id,S.name AS series_name,B.series_number,B.date_published,B.date_added,B.date_modified,")
            .append("P.id AS publisher_id,P.name AS publisher_name ")
            .append("FROM books B ")
            .append("   LEFT JOIN publishers P ")
            .append("       ON B.publisher_id = P.id")
            .append("   LEFT JOIN series S ")
            .append("       ON B.series_id = S.id);")
            .toString();

    private static final String SQL_QUERY_BY_ID = "SELECT * FROM books WHERE id=?;";

    private static final String SQL_QUERY_BY_TITLE = "SELECT * FROM books WHERE title=?;";

    private static final String SQL_QUERY_BOOK_BY_ID_JOINED = new StringBuilder()
            .append("SELECT * FROM ")
            .append("(SELECT ")
            .append("B.id,B.isbn,B.uuid,B.title,S.id AS series_id,S.name AS series_name,B.series_number,B.date_published,")
            .append("B.date_added,B.date_modified,P.id AS publisher_id,P.name AS publisher_name ")
            .append("FROM books B ")
            .append("LEFT OUTER JOIN publishers P ")
            .append("   ON B.publisher_id = P.id ")
            .append("LEFT OUTER JOIN series S ")
            .append("   ON B.series_id = S.id ) ")
            .append("WHERE id=?;")
            .toString();

    /**
     * Query all books in the database, this method executes the query all records in the `books` table joined with the
     * `publishers` and the `series` table.
     * <p>
     * The format of each record string:
     * id,isbn,uuid,title,series_id,series_name,series_number,publisher_id,publisher_name,date_published,date_added,date_modified
     * <p>
     * The split string index will be as:
     * 0  - id
     * 1  - isbn
     * 2  - uuid
     * 3  - title
     * 4  - series_id
     * 5  - series_name
     * 6  - series_number
     * 7  - publisher_id
     * 8  - publisher_name
     * 9  - date_published
     * 10 - date_added
     * 11 - date_modified
     *
     * @param con SQL database connection.
     * @return a list of String objects, each String represents a `books` records, joined with the `publishers` records.
     * @throws SQLException any SQL Exception.
     */
    public static List<String> queryAll(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        ArrayList<String> bookList = null;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_QUERY_ALL_JOINED);

            bookList = new ArrayList<>();

            while (rs.next()) {
                String record = getStringFromRecordJoined(rs);
                bookList.add(record);
            }
        } finally {
            closeResources(stmt, rs);
        }

        return bookList;
    }

    /**
     * Query `books` record by their ID in the table, not joined with other tables. The returned object will be a
     * comma separated String with the position:
     *
     * 0  - id
     * 1  - isbn
     * 2  - uuid
     * 3  - title
     * 4  - series_id
     * 5  - series_number
     * 6  - publisher_id
     * 7  - date_published
     * 8  - date_added
     * 9  - date_modified
     *
     * @param con the SQLite connection.
     * @param id ID of the record to query.
     * @return a comma separated string representation of the record.
     * @throws SQLException any SQL exception.
     */
    public static String queryById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        String record = null;

        try {
            stmt = con.prepareStatement(SQL_QUERY_BY_ID);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                record = getStringFromRecord(rs);
            } else {
                throw new SQLException("Querying book by id failed, no returned results.");
            }
        } finally {
            closeResources(stmt, rs);
        }

        return record;
    }

    /**
     * Query `books` record by their title, to see the output format see description for {@link #queryById(Connection, int)}
     *
     * @param con the SQLite connection.
     * @param title the title of the book record to query for.
     * @return a comma separated string representation of the record.
     * @throws SQLException any SQL exception.
     */
    public static String queryByTitle(Connection con, String title) throws SQLException {
        ResultSet rs = null;
        String record = null;

        try (PreparedStatement stmt = con.prepareStatement(SQL_QUERY_BY_TITLE)) {
            stmt.setString(1, title);
            rs = stmt.executeQuery();

            if (rs.next()) {
                record = getStringFromRecord(rs);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
        }

        return record;
    }

    public static Book insert(Connection con, Book bookToInsert) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con.setAutoCommit(false);
            Instant currentTime = Instant.now().truncatedTo(ChronoUnit.DAYS);
            stmt = con.prepareStatement(SQL_INSERT);
            stmt.setString(1, bookToInsert.getIsbn());
            stmt.setString(2, bookToInsert.getUuid());
            stmt.setString(3, bookToInsert.getTitle());
            stmt.setString(4, bookToInsert.getPublishDate() != null
                    ? bookToInsert.getPublishDate().toString()
                    : currentTime.toString());

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Inserting book failed, no rows affected.");
            }

            bookToInsert.setId(getLastInsertedRowId(con));

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt, rs);
        }

        return bookToInsert;
    }

    public static List<Book> insert(Connection con, List<Book> booksToInsert) throws SQLException {
        PreparedStatement stmt = null;
        Savepoint sp = null;

        try {
            con.setAutoCommit(false);
            sp = con.setSavepoint();

            for (int i = 0; i < booksToInsert.size(); i++) {
                Book b = booksToInsert.get(i);

                Instant currentTime = Instant.now().truncatedTo(ChronoUnit.DAYS);
                stmt = con.prepareStatement(SQL_INSERT);
                stmt.setString(1, b.getIsbn());
                stmt.setString(2, b.getUuid());
                stmt.setString(3, b.getTitle());
                stmt.setString(4, b.getPublishDate() != null
                        ? b.getPublishDate().toString()
                        : currentTime.toString());

                int rows = stmt.executeUpdate();

                if (rows == 0) {
                    con.rollback(sp);
                    throw new SQLException("Inserting book failed, no rows affected.");
                }

                b.setId(getLastInsertedRowId(con));
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return booksToInsert;
    }

    public static void update(Connection con, int id, Book updatedBook) throws SQLException {
        PreparedStatement stmt = null;

        try {
            con.setAutoCommit(false);

            stmt = con.prepareStatement(SQL_UPDATE_BOOK);

            stmt.setString(1, updatedBook.getIsbn());
            stmt.setString(2, updatedBook.getUuid());
            stmt.setString(3, updatedBook.getTitle());

            if (updatedBook.getSeries() == null)
                stmt.setNull(4, Types.NULL);
            else
                stmt.setInt(4, updatedBook.getSeries().getId());

            stmt.setDouble(5, updatedBook.getSeriesNumber());

            if (updatedBook.getPublisher() == null)
                stmt.setNull(6, Types.NULL);
            else
                stmt.setInt(6, updatedBook.getPublisher().getId());

            stmt.setString(7, updatedBook.getPublishDate().toString());
            stmt.setString(8, updatedBook.getAddedDate().toString());
            stmt.setString(9, updatedBook.getModifiedDate().toString());
            stmt.setInt(10, id);

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Updating book record failed, no rows affected.");
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }
    }

    public static void deleteById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;
        Savepoint sp = null;

        try {
            con.setAutoCommit(false);

            sp = con.setSavepoint();

            stmt = con.prepareStatement(SQL_DELETE_BY_ID);
            stmt.setInt(1, id);

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                con.rollback(sp);
                throw new SQLException("Deleting book failed, no rows affected.");
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
                    throw new SQLException("Deleting book failed, no rows affected, rolled back changes.");
                }
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
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
            closeResources(stmt);
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
            closeResources(stmt);
        }
    }

    public static boolean tableExists(Connection con) throws SQLException {
        Statement stmt = con.createStatement();
        ResultSet rs = null;
        boolean exists = false;

        try {
            rs = stmt.executeQuery(SQL_FIND_TABLE);

            if (rs.next()) {
                exists = true;
            }
        } finally {
            closeResources(stmt, rs);
        }

        return exists;
    }

    /**
     * Helper methods.
     */
    private static String getStringFromRecordJoined(ResultSet rs) throws SQLException {
        return new StringJoiner(",")
                .add(String.valueOf(rs.getInt("id")))
                .add(rs.getString("isbn"))
                .add(rs.getString("uuid"))
                .add(rs.getString("title"))
                .add(String.valueOf(rs.getInt("series_id")))
                .add(rs.getString("series_name"))
                .add(String.valueOf(rs.getDouble("series_number")))
                .add(String.valueOf(rs.getInt("publisher_id")))
                .add(rs.getString("publisher_name"))
                .add(rs.getString("date_published"))
                .add(rs.getString("date_added"))
                .add(rs.getString("date_modified")).toString();

    }

    private static String getStringFromRecord(ResultSet rs) throws SQLException {
        return new StringJoiner(",")
                .add(String.valueOf(rs.getInt("id")))
                .add(rs.getString("isbn"))
                .add(rs.getString("uuid"))
                .add(rs.getString("title"))
                .add(String.valueOf(rs.getInt("series_id")))
                .add(String.valueOf(rs.getDouble("series_number")))
                .add(String.valueOf(rs.getInt("publisher_id")))
                .add(rs.getString("date_published"))
                .add(rs.getString("date_added"))
                .add(rs.getString("date_modified")).toString();
    }

    private static Book getBookFromResultSet(ResultSet rs) throws SQLException {
        Book book = new Book();

        book.setId(rs.getInt("id"));
        book.setIsbn(rs.getString("isbn"));
        book.setTitle(rs.getString("title"));
        book.setSeriesNumber(rs.getDouble("series_number"));
        book.setPublishDate(Instant.parse(rs.getString("date_published")));
        book.setAddedDate(Instant.parse(rs.getString("date_added")));
        book.setModifiedDate(Instant.parse(rs.getString("date_modified")));

        return book;
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

    private static int getLastInsertedRowId(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        int id = 0;

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_QUERY_LAST_INSERTED_ROWID);

            if (rs.next()) {
                id = rs.getInt("id");
            } else {
                throw new SQLException("Querying last inserted id failed, no values returned.");
            }
        } finally {
            closeResources(stmt);
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

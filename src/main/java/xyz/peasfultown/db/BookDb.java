/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: SQLite3 queries for the `books` table.
 */
package xyz.peasfultown.db;

import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.Publisher;

import java.sql.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class BookDb {
    private static final String COL_ID = "id";
    private static final String COL_ISBN = "isbn";
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
            .append("   CONSTRAINT df_books_t DEFAULT \"Unknown\",")
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

    private static final String SQL_INSERT_BOOK = new StringBuilder()
            .append("INSERT INTO books ")
            .append("(isbn, title, series_number, publisher_id, date_published, date_added, date_modified) ")
            .append("VALUES (?, ?, ?, ?, ?, ?, ?);")
            .toString();

    private static final String SQL_UPDATE_BOOK = new StringBuilder()
            .append("UPDATE books ")
            .append("SET isbn=?,")
            .append("title=?,")
            .append("publisher_id=?,")
            .append("date_published=?,")
            .append("date_added=?,")
            .append("date_modified=?,")
            .append("series_number=? ")
            .append("WHERE id=?;")
            .toString();

    private static final String SQL_GET_LAST_INSERTED_ROWID = "SELECT last_insert_rowid() as id;";

    private static final String SQL_DELETE_BY_ID = "DELETE FROM books WHERE id=?;";

    private static final String SQL_SELECT_ALL_BOOKS_JOINED = new StringBuilder()
            .append("SELECT * FROM ")
            .append("(SELECT B.id,B.isbn,B.title,B.date_published,B.date_published,B.date_added,B.date_modified,")
            .append("P.id AS publisher_id,P.name AS publisher_name ")
            .append("   FROM books B ")
            .append("   LEFT JOIN publishers P ")
            .append("   ON B.publisher_id = P.id")
            .append(");")
            .toString();

    private static final String SQL_SELECT_BOOK_BY_ID = "SELECT * FROM books WHERE id=?;";

    private static final String SQL_SELECT_BOOK_BY_ID_JOINED = new StringBuilder()
            .append("SELECT * FROM ")
            .append("(SELECT ")
            .append("B.id,B.isbn,B.title,B.series_number,B.date_published,B.date_added,B.date_modified,")
            .append("P.id AS publisher_id,P.name AS publisher_name ")
            .append("FROM books B ")
            .append("LEFT OUTER JOIN publishers P ")
            .append("   ON P.id = B.publisher_id )")
            .append("WHERE id=?;")
            .toString();

    public static List<Book> queryAll(Connection con) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        List<Book> booklist = new ArrayList<>();

        try {
            stmt = con.createStatement();
            rs = stmt.executeQuery(SQL_SELECT_ALL_BOOKS_JOINED);

            while (rs.next()) {
                Book newBook = new Book();
                newBook.setId(rs.getInt("id"));
                newBook.setIsbn(rs.getString("isbn"));
                newBook.setTitle(rs.getString("title"));

                Publisher pub = new Publisher();

                pub.setId(rs.getInt("publisher_id"));
                pub.setName(rs.getString("publisher_name"));
                newBook.setPublisher(pub);

                newBook.setPublishDate(Instant.parse(rs.getString("date_published")));
                newBook.setAddedDate(Instant.parse(rs.getString("date_added")));
                newBook.setModifiedDate(Instant.parse(rs.getString("date_modified")));
                booklist.add(newBook);
            }
        } finally {
            closeResources(stmt, rs);
        }

        return booklist;
    }

    public static Book queryById(Connection con, int id) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Book book = null;

        try {
            stmt = con.prepareStatement(SQL_SELECT_BOOK_BY_ID_JOINED);
            stmt.setInt(1, id);
            rs = stmt.executeQuery();

            if (rs.next()) {
                book = new Book();
                book.setId(rs.getInt("id"));
                book.setIsbn(rs.getString("isbn"));
                book.setTitle(rs.getString("title"));
                book.setNumberInSeries(rs.getDouble("series_number"));
                book.setPublisher(new Publisher(rs.getString("publisher_name")));
                book.setPublishDate(Instant.parse(rs.getString("date_published")));
                book.setAddedDate(Instant.parse(rs.getString("date_added")));
                book.setModifiedDate(Instant.parse(rs.getString("date_modified")));
            } else {
                throw new SQLException("Querying book by id failed, no returned results.");
            }
        } finally {
            closeResources(stmt, rs);
        }

        return book;
    }

    public static Book insert(Connection con, Book bookToInsert) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Book bookToReturn = null;

        try {
            con.setAutoCommit(false);
            stmt = con.prepareStatement(SQL_INSERT_BOOK);
            stmt.setString(1, bookToInsert.getIsbn());
            stmt.setString(2, bookToInsert.getTitle());
            stmt.setDouble(3, bookToInsert.getNumberInSeries());
            if (bookToInsert.getPublisher() == null) {
                stmt.setNull(4, Types.NULL);
            } else {
                stmt.setInt(4, bookToInsert.getPublisher().getId());

            }
            stmt.setString(5, bookToInsert.getPublishDate().toString());
            stmt.setString(6, bookToInsert.getAddedDate().toString());
            stmt.setString(7, bookToInsert.getModifiedDate().toString());

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Inserting book failed, no rows affected.");
            }

            bookToReturn = new Book();
            bookToReturn.setId(getLastInsertedRowId(con));
            bookToReturn.setTitle(bookToInsert.getTitle());
            bookToReturn.setNumberInSeries(bookToInsert.getNumberInSeries());
            bookToReturn.setPublisher(bookToInsert.getPublisher());
            bookToReturn.setPublishDate(bookToInsert.getPublishDate());
            bookToReturn.setAddedDate(bookToInsert.getAddedDate());
            bookToReturn.setModifiedDate(bookToInsert.getModifiedDate());

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt, rs);
        }

        return bookToReturn;
    }

    public static List<Book> insert(Connection con, List<Book> booksToInsert) throws SQLException {
        PreparedStatement stmt = null;
        Savepoint sp = null;
        List<Book> booksToReturn = null;

        try {
            con.setAutoCommit(false);
            sp = con.setSavepoint();

            booksToReturn = new ArrayList<>();
            for (int i = 0; i < booksToInsert.size(); i++) {
                Book b = booksToInsert.get(i);

                stmt = con.prepareStatement(SQL_INSERT_BOOK);
                stmt.setString(1, b.getIsbn());
                stmt.setString(2, b.getTitle());
                stmt.setDouble(3, b.getNumberInSeries());

                if (b.getPublisher() == null) {
                    stmt.setNull(4, Types.NULL);
                } else {
                    stmt.setInt(4, b.getPublisher().getId());
                }

                stmt.setString(5, b.getPublishDate().toString());
                stmt.setString(6, b.getAddedDate().toString());
                stmt.setString(7, b.getModifiedDate().toString());

                int rows = stmt.executeUpdate();

                if (rows == 0) {
                    con.rollback(sp);
                    throw new SQLException("Inserting book failed, no rows affected.");
                }

                Book br = new Book();
                br.setId(getLastInsertedRowId(con));
                br.setIsbn(b.getIsbn());
                br.setTitle(b.getTitle());
                br.setPublisher(b.getPublisher());
                br.setPublishDate(b.getPublishDate());
                br.setAddedDate(b.getAddedDate());
                br.setModifiedDate(b.getModifiedDate());

                booksToReturn.add(br);
            }

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return booksToReturn;
    }

    public static Book update(Connection con, int id, Book updatedBook) throws SQLException {
        PreparedStatement stmt = null;
        Book bookToReturn = null;

        try {
            con.setAutoCommit(false);

            stmt = con.prepareStatement(SQL_UPDATE_BOOK);

            stmt.setString(1, updatedBook.getIsbn());
            stmt.setString(2, updatedBook.getTitle());
            if (updatedBook.getPublisher() == null) {
                stmt.setNull(3, Types.NULL);
            } else {
                stmt.setInt(3, updatedBook.getPublisher().getId());
            }
            stmt.setString(4, updatedBook.getPublishDate().toString());
            stmt.setString(5, updatedBook.getAddedDate().toString());
            stmt.setString(6, updatedBook.getModifiedDate().toString());
            stmt.setDouble(7, updatedBook.getNumberInSeries());
            stmt.setInt(8, id);

            int rows = stmt.executeUpdate();

            if (rows == 0) {
                throw new SQLException("Updating book record failed, no rows affected.");
            }

            bookToReturn = new Book(
                    id,
                    updatedBook.getIsbn(),
                    updatedBook.getUuid(),
                    updatedBook.getTitle(),
                    updatedBook.getPublisher(),
                    updatedBook.getPublishDate(),
                    updatedBook.getAddedDate(),
                    updatedBook.getModifiedDate(),
                    updatedBook.getNumberInSeries()
            );

            bookToReturn.setNumberInSeries(updatedBook.getNumberInSeries());

            con.commit();
        } finally {
            con.setAutoCommit(true);
            closeResources(stmt);
        }

        return bookToReturn;
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
            rs = stmt.executeQuery(SQL_GET_LAST_INSERTED_ROWID);

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

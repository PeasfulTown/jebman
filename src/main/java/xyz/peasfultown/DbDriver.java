/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Main class for all database interactions concerning books, authors, book series, publishers, etc.
 * This is the main controller for all modules in this program.
 */
package xyz.peasfultown;

import xyz.peasfultown.base.Author;
import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.BookSeries;
import xyz.peasfultown.base.Publisher;
import xyz.peasfultown.db.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;


public class DbDriver {
    private final boolean isTest;

    private Connection con;

    private List<Book> books;
    private List<Publisher> publishers;
    private List<Author> authors;
    private List<BookSeries> bookseries;

    public DbDriver(boolean isTest) throws SQLException {
        this.isTest = isTest;

        try {
            getConnection();

            if (!PublisherDb.tableExists(con)) {
                PublisherDb.createTable(con);
            }

            if (!AuthorDb.tableExists(con)) {
                AuthorDb.createTable(con);
            }

            if (!BookSeriesDb.tableExists(con)) {
                BookSeriesDb.createTable(con);
            }

            if (!BookDb.tableExists(con)) {
                BookDb.createTable(con);
            }
        } catch (SQLException e) {
            throw new SQLException("Instantiating DbDriver failed.");
        } finally {
            closeConnection();
        }
    }

    private void closeConnection() throws SQLException {
        try {
            if (this.con != null)
                this.con.close();

        } catch (SQLException e) {
            throw new SQLException ("Failed to close connection in DbDriver.");
        }
    }

    private void getConnection() throws SQLException {
        try {
            if (this.isTest) {
                this.con = DbConnection.getTestConnection();
            } else {
                this.con = DbConnection.getConnection();
            }
        } catch (SQLException e) {
            throw new SQLException ("Instantiating database connection in DbDriver failed");
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

/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Main class for all database interactions concerning books, authors, book series, publishers, etc.
 * This is the main controller for all modules in this program.
 */
package xyz.peasfultown;

import xyz.peasfultown.base.Author;
import xyz.peasfultown.base.Book;
import xyz.peasfultown.base.Series;
import xyz.peasfultown.base.Publisher;
import xyz.peasfultown.db.*;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.HashMap;

/**
 * TODO: upon instantialization, check program's main path for the SQLite database file (metadata.db) and load it.
 */
public class MainController {
    private static final String DEFAULT_DB_PATH = new StringBuilder(System.getProperty("user.home"))
            .append(System.getProperty("file.separator")).append("Documents")
            .append(System.getProperty("file.separator")).append("jebman-library")
            .append(System.getProperty("file.separator")).append("metadata.db").toString();
    private final Path dbPath;
    private final Path mainPath;
    private List<Book> books;
    private List<Publisher> publishers;
    private List<Author> authors;
    private List<Series> series;

    /**
     * Default constructor creates a directory for the program at the user's `Documents` directory.
     *
     * @throws SQLException any SQL exception.
     */
    public MainController() throws SQLException {
        this.dbPath = Path.of(DEFAULT_DB_PATH);
        this.mainPath = dbPath.getParent();

        ready();
    }

    /**
     * Constructor where user can specify main path for the program.
     *
     * @param customPath file path for the database and all other files for the program.
     * @throws SQLException any SQL exception.
     */
    public MainController(Path customPath) throws SQLException {
        this.mainPath = customPath.resolve("jebman-library");
        this.dbPath = mainPath.resolve("metadata.db");

        ready();
    }

    private void ready() throws SQLException {
        if (!Files.exists(mainPath) || !Files.isDirectory(mainPath)) {
            try {
                Files.createDirectory(mainPath);
            } catch (IOException e) {
                System.err.format("Failed to create directory %s: %s%n", mainPath, e);
            }
        }

        createTables();
    }

    /**
     * Copy book over to main program directory, and insert a record of the book to SQLite database.
     *
     * @param file
     * @throws IOException
     * @throws XMLStreamException
     */
    public void insertBook(Path file) throws SQLException, IOException, XMLStreamException {
        // TODO: validate file format
        // TODO: insert author record if not already present
        String[] parts = file.getFileName().toString().split("\\.");

        if (parts.length < 2) {
            throw new IOException(String.format("Unable to determine %s filetype.", file.getFileName()));
        }
        HashMap<String,String> metadata;

        Book book = new Book();
        switch (parts[parts.length - 1]) {
            case "epub":
                metadata = MetaReader.getEpubMetadata(file);
                book.setIsbn(metadata.getOrDefault("isbn", ""));
                book.setUuid(metadata.getOrDefault("uuid", ""));
                book.setTitle(metadata.getOrDefault("title", parts[0]));
                if (metadata.get("publisher") != null)
                    book.setPublisher(new Publisher(metadata.get("publisher")));
                if (metadata.get("creator") != null)
                    book.setAuthor(new Author(metadata.get("creator")));
                if (metadata.get("date") != null)
                    book.setPublishDate(MetaReader.parseDate(metadata.get("date")));
                if (metadata.get("publisher") != null)
                    book.setPublisher(new Publisher(metadata.get("publisher")));
                break;
            case "pdf":
                metadata = MetaReader.getPDFMetadata(file);
                book.setTitle(metadata.getOrDefault("title", parts[0]));
                book.setAuthor(new Author(metadata.getOrDefault("author", "Unknown")));
                book.setPublishDate(MetaReader.parseDate(metadata.getOrDefault("date", Instant.now().toString())));
                break;
            default:
                throw new IOException("Unable to add ebook, possibly unsupported file type.");
        }

        Path destDir = this.mainPath
                .resolve(book.getAuthors().getName());
        if (!Files.isDirectory(destDir) || !Files.exists(destDir)) {
            Files.createDirectory(destDir);
        }
        Path target = destDir.resolve(String.format("%s.%s",
                book.getTitle(),
                parts[parts.length-1]));
        Files.copy(file, target);

        try (Connection con = DbConnection.getConnection(this.dbPath.toString())){
            // TODO: insert record in book-author joint table
            book = BookDb.insert(con, book);
            if (book.getPublisher() != null) {
                String publisherRecord = PublisherDb.queryByName(con, book.getPublisher().getName());
                if (publisherRecord == null)
                    book.setPublisher(PublisherDb.insert(con, new Publisher(metadata.get("publisher"))));
                else
                    book.setPublisher(Publisher.parse(publisherRecord));

                BookDb.update(con, book.getId(), book);
                System.out.format("Book has publisher: %s%n", book.getPublisher());
                System.out.format("Book object: %s%n", book);
            }

            if (book.getAuthors() != null) {
                String authorRecord = AuthorDb.queryByName(con, book.getAuthors().getName());
                if (authorRecord == null)
                    book.setAuthor(AuthorDb.insert(con, new Author(book.getAuthors().getName())));
                else
                    book.setAuthor(Author.parse(authorRecord));

                BookAuthorLinkDb.insert(con, book.getId(), book.getAuthors().getId());
            }
        } catch (SQLException e) {
            throw new SQLException ("Failed to insert database records for " + file.getFileName(), e);
        }
    }

    private void createTables() throws SQLException {
        try (Connection con = DbConnection.getConnection(this.dbPath.toString())){
            if (!PublisherDb.tableExists(con)) {
                PublisherDb.createTable(con);
            }

            if (!AuthorDb.tableExists(con)) {
                AuthorDb.createTable(con);
            }

            if (!SeriesDb.tableExists(con)) {
                SeriesDb.createTable(con);
            }

            if (!BookDb.tableExists(con)) {
                BookDb.createTable(con);
            }

            if (!BookAuthorLinkDb.tableExists(con)) {
                BookAuthorLinkDb.createTable(con);
            }
        } catch (SQLException e) {
            throw new SQLException ("Failed to create tables for the main controller.", e);
        }
    }

    public Path getMainPath() {
        return this.mainPath;
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

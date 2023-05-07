/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Main class for all database interactions concerning books, authors, book series, publishers, etc.
 * This is the main controller for all modules in this program.
 */
package xyz.peasfultown;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.GenericDAO;
import xyz.peasfultown.dao.impl.*;
import xyz.peasfultown.domain.*;
import xyz.peasfultown.helpers.*;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO: upon instantialization, check program's main path for the SQLite database file (metadata.db) and load it.
 */
public class MainController {
    private Path mainPath;
    private Map<Integer, Book> booksMap;
    private Map<Integer, Series> seriesMap;
    private Map<Integer, Publisher> publishersMap;
    private Map<Integer, Author> authorsMap;
    private GenericDAO<Book> bookDAO;
    private GenericDAO<Series> seriesDAO;
    private GenericDAO<Publisher> publisherDAO;
    private GenericDAO<Author> authorDAO;
    private GenericDAO<BookAuthor> bookAuthorDAO;

    /**
     * Default constructor creates a directory for the program at the user's `Documents` directory.
     *
     * @throws SQLException any SQL exception.
     */
    public MainController() throws SQLException {
        this.mainPath = ApplicationConfig.MAIN_PATH;

        this.booksMap = new HashMap<>();
        this.seriesMap = new HashMap<>();
        this.publishersMap = new HashMap<>();
        this.authorsMap = new HashMap<>();

        this.publisherDAO = new JDBCPublisherDAO();
        this.seriesDAO = new JDBCSeriesDAO();
        this.bookAuthorDAO = new JDBCBookAuthorDAO();
        this.authorDAO = new JDBCAuthorDAO();
        this.bookDAO = new JDBCBookDAO(seriesMap, publishersMap);

        ready();
    }

    private void ready() throws SQLException {
        // directory
        if (!Files.exists(mainPath) || !Files.isDirectory(mainPath)) {
            try {
                Files.createDirectory(mainPath);
            } catch (IOException e) {
                System.err.format("Failed to create directory %s: %s%n", mainPath, e);
            }
        }

        // tables
        try (Connection con = ConnectionFactory.getConnection()) {
            ScriptRunner.runScript(con, new File("database.sql"));
        } catch (SQLException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    /**
     * Copy book over to main program directory, and insert a record of the book to SQLite database.
     *
     * @param file
     * @throws IOException
     * @throws XMLStreamException
     */
    public void insertBook(Path file) throws DAOException, IOException, MetadataReaderException {
        // TODO: validate file format
        // TODO: insert author record if not already present
        String[] parts = file.getFileName().toString().split("\\.");

        if (parts.length < 2) {
            throw new IOException(String.format("Unable to determine %s filetype.", file.getFileName()));
        }

        HashMap<String, String> metadata = MetaReader.getMetadata(file);
        Book book = null;
        try {
            book = createBookFromMetadata(metadata);
        } catch (DAOException e) {
            throw new DAOException(e.getMessage(), e);
        }

        addBookToDirectory(file, metadata
                        .getOrDefault("author", metadata
                                .getOrDefault("creator", "Unknown")),
                book.getTitle(),
                book.getId(),
                metadata.get("filetype"));
        this.booksMap.put(book.getId(), book);
    }

    public Book getBookById(int id) throws DAOException {
        return bookDAO.read(id);
    }

    public Book getBookByTitle(String str) throws DAOException {
        return bookDAO.read(str);
    }

    public Publisher getPublisherById(int id) throws DAOException {
        return publisherDAO.read(id);
    }

    public Publisher getPublisherByName(String str) throws DAOException {
        return publisherDAO.read(str);
    }

    public Series getSeriesById(int id) throws DAOException {
        return seriesDAO.read(id);
    }

    public Series getSeriesByName(String str) throws DAOException {
        return seriesDAO.read(str);
    }

    public Author getAuthorById(int id) throws DAOException {
        return authorDAO.read(id);
    }

    public Author getAuthorByName(String str) throws DAOException {
        return authorDAO.read(str);
    }

    public Path getMainPath() {
        return this.mainPath;
    }

    private void addBookToDirectory(Path file, String authorName, String bookTitle, int id, String fileType) throws IOException {
        String titleDir = new StringBuilder(bookTitle).append(' ').append('(').append(id).append(')').toString();
        Path destDir = Path.of(mainPath.toString(), authorName, titleDir);

        if (!Files.isDirectory(destDir) || !Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }
        Path target = destDir.resolve(String.format("%s.%s",
                bookTitle,
                fileType));
        Files.copy(file, target);
    }

    private Book createBookFromMetadata(HashMap<String, String> meta) throws DAOException {
        Book book = new Book();
        if (meta.get("filetype").equalsIgnoreCase("epub")) {
            setEpub(book, meta);
        } else if (meta.get("filetype").equalsIgnoreCase("pdf")) {
            setPDF(book, meta);
        }

        if (getBookFromStore(book.getTitle()) != null) {
            throw new DAOException("Book already exists in record");
        }
        String authorName = meta.getOrDefault("author", meta.getOrDefault("creator", "Unknown"));
        book.setPath(authorName + System.getProperty("file.separator") + book.getTitle());

        bookDAO.create(book);
        Author author = getAuthorFromMap(authorName);
        if (author == null) {
            author = new Author(authorName);
            authorDAO.create(author);
            this.authorsMap.put(author.getId(), author);
        }
        BookAuthor ba = new BookAuthor(book.getId(), author.getId());
        try {
            bookAuthorDAO.create(ba);
        } catch (DAOException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return book;
    }

    private void setEpub(Book book, HashMap<String, String> meta) throws DAOException {
        String filename = meta.get("filename");
        book.setIsbn(meta.getOrDefault("isbn", ""));
        book.setUuid(meta.getOrDefault("uuid", ""));
        book.setTitle(meta.getOrDefault("title", filename));

        String publisherMeta = meta.get("publisher");
        if (publisherMeta != null) {
            Publisher publisher = getPublisherFromMap(meta.get("publisher"));
            if (publisher == null) {
                publisher = new Publisher(publisherMeta);
                publisherDAO.create(publisher);
                this.publishersMap.put(publisher.getId(), publisher);
            }
            book.setPublisher(publisher);
        }

        if (meta.get("date") != null)
            book.setPublishDate(MetaReader.parseDate(meta.get("date")));
    }

    private void setPDF(Book book, HashMap<String, String> meta) {
        String filename = meta.get("filename");
        book.setTitle(meta.getOrDefault("title", filename));
        if (meta.get("date") != null)
            book.setPublishDate(MetaReader.parseDate(meta.get("date")));
    }

    private Book getBookFromStore(String title) {
        for (Book b : booksMap.values()) {
            if (b.getTitle().equals(title))
                return b;
        }
        return null;
    }

    private Publisher getPublisherFromMap(String name) {
        for (Publisher p : publishersMap.values()) {
            if (p.getName().equalsIgnoreCase(name))
                return p;
        }
        return null;
    }

    private Author getAuthorFromMap(String name) {
        for (Author a : authorsMap.values()) {
            if (a.getName().equalsIgnoreCase(name))
                return a;
        }
        return null;
    }

    public Map<Integer, Book> getBooks() {
        return this.booksMap;
    }

    public Map<Integer, Series> getSeries() {
        return this.seriesMap;
    }

    public Map<Integer, Publisher> getPublishers() {
        return this.publishersMap;
    }

    public Map<Integer, Author> getAuthors() {
        return this.authorsMap;
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

/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Main class for all database interactions concerning books, authors, book series, publishers, etc.
 * This is the main controller for all modules in this program.
 */
package xyz.peasfultown;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.GenericDAO;
import xyz.peasfultown.dao.RecordAlreadyExistsException;
import xyz.peasfultown.dao.impl.*;
import xyz.peasfultown.domain.*;
import xyz.peasfultown.helpers.*;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO: upon instantialization, check program's main path for the SQLite database file (metadata.db) and load it.
 */
public class MainController {
    private final Path mainPath;
    private SearchableRecordSet<Book> booksMap;
    private SearchableRecordSet<Series> seriesMap;
    private SearchableRecordSet<Publisher> publishersMap;
    private SearchableRecordSet<Author> authorsMap;
    private SearchableRecordSet<BookAuthor> bookAuthorMap;
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

        populateCollections();
    }

    public void populateCollections() {
        try {
            this.publisherDAO = new JDBCPublisherDAO();
            this.seriesDAO = new JDBCSeriesDAO();
            this.authorDAO = new JDBCAuthorDAO();
            this.bookAuthorDAO = new JDBCBookAuthorDAO();

            this.seriesMap = (SearchableRecordSet<Series>) this.getAllSeries();
            this.publishersMap = (SearchableRecordSet<Publisher>) this.getAllPublishers();
            this.authorsMap = (SearchableRecordSet<Author>) this.getAllAuthors();
            this.bookAuthorMap = (SearchableRecordSet<BookAuthor>) this.getAllBookAuthorLinks();

            this.bookDAO = new JDBCBookDAO(seriesMap, publishersMap);
            this.booksMap = (SearchableRecordSet<Book>) this.getAllBooks();
        } catch (DAOException e) {
            System.err.println("Unable to populate collections.");
        }
    }

    public void insertBook(String filePath) throws DAOException, IOException, MetadataReaderException {
        Path file = Path.of(filePath);
        insertBook(file);
    }

    /**
     * Copy book over to main program directory, and insert a record of the book to SQLite database.
     *
     * @param file
     * @throws IOException
     * @throws XMLStreamException
     */
    public void insertBook(Path file) throws DAOException, IOException, MetadataReaderException {
        if (!Files.exists(file))
            throw new FileNotFoundException("File does not exist.");

        HashMap<String, String> metadata = MetaReader.getMetadata(file);
        Book book = null;
        book = createRecordsFromMetadata(metadata);
        addBookToDirectory(file, metadata
                        .getOrDefault("author", metadata
                                .getOrDefault("creator", "Unknown")),
                book.getTitle(),
                book.getId(),
                metadata.get("filetype"));
        this.booksMap.add(book);
    }

    public void removeBook(int id) throws DAOException, IOException {
        Book book = bookDAO.read(id);
        removeBook(book);
    }

    public void removeBook(Book book) throws DAOException, IOException {
        Path pathToRemove = Path.of(ApplicationConfig.MAIN_PATH.toString(), book.getPath());
        TreeDeleter td = new TreeDeleter();
        Files.walkFileTree(pathToRemove, td);

        bookDAO.delete(book.getId());
        booksMap.remove(book);
    }

    private Set<Publisher> getAllPublishers() throws DAOException {
        return publisherDAO.readAll();
    }

    private Set<Series> getAllSeries() throws DAOException {
        return seriesDAO.readAll();
    }

    private Set<Author> getAllAuthors() throws DAOException {
        return authorDAO.readAll();
    }

    private Set<BookAuthor> getAllBookAuthorLinks() throws DAOException {
        return bookAuthorDAO.readAll();
    }

    private Set<Book> getAllBooks() throws DAOException {
        return bookDAO.readAll();
    }

    public Author getBookAuthorByBookId(int id) {
        for(BookAuthor ba : bookAuthorMap) {
            if (ba.getBookId() == id)
                return this.authorsMap.getById(ba.getAuthorId());
        }
        System.out.println("Cannot find Author");
        return null;
    }

    public Path getMainPath() {
        return this.mainPath;
    }

    private String getRelativePathToBook(String authorName, String bookTitle, int id) {
        return new StringBuilder(authorName)
                .append(System.getProperty("file.separator"))
                .append(bookTitle)
                .append(' ').append('(').append(id).append(')').toString();
    }

    private void addBookToDirectory(Path file, String authorName, String bookTitle, int id, String fileType) throws IOException {
        Path destDir = Path.of(mainPath.toString(), getRelativePathToBook(authorName, bookTitle, id));

        if (!Files.isDirectory(destDir) || !Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }
        Path target = destDir.resolve(String.format("%s.%s",
                bookTitle,
                fileType));
        Files.copy(file, target);
    }

    private Book createRecordsFromMetadata(HashMap<String, String> meta) throws DAOException {
        Book book = new Book();
        book.setTitle(meta.getOrDefault("title", meta.get("filename")));

        if (booksMap.getByName(book.getTitle()) != null) {
            throw new RecordAlreadyExistsException("Book already exists in records.");
        }

        if (meta.get("filetype").equalsIgnoreCase("epub")) {
            setEpub(book, meta);
        }

        book.setPublishDate(MetaReader
                .parseDate(meta.getOrDefault("date", Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())));

        bookDAO.create(book);
        String authorName = meta.getOrDefault("author", meta.getOrDefault("creator", "Unknown"));
        Author author = createAuthorFromName(authorName);
        book.setPath(getRelativePathToBook(author.getName(), book.getTitle(), book.getId()));
        bookDAO.update(book);
        addBookAuthorLink(book.getId(), author.getId());
        return book;
    }

    private void setEpub(Book book, HashMap<String, String> meta) throws DAOException {
        book.setIsbn(meta.getOrDefault("isbn", ""));
        book.setUuid(meta.getOrDefault("uuid", ""));
        String publisherMeta = meta.get("publisher");
        Publisher publisher = createPublisherFromName(publisherMeta);
        book.setPublisher(publisher);
    }

    private void addBookAuthorLink(int bookId, int authorId) throws DAOException {
        BookAuthor ba = new BookAuthor(bookId, authorId);
        this.bookAuthorDAO.create(ba);
        this.bookAuthorMap.add(ba);
    }

    private Author createAuthorFromName(String name) throws DAOException {
        Author author = authorsMap.getByName(name);
        if (author == null) {
            author = new Author(name);
            authorDAO.create(author);
            this.authorsMap.add(author);
        }
        return author;
    }

    private Publisher createPublisherFromName(String publisherName) throws DAOException {
        if (publisherName != null) {
            Publisher publisher = publishersMap.getByName(publisherName);
            if (publisher == null) {
                publisher = new Publisher(publisherName);
                publisherDAO.create(publisher);
                this.publishersMap.add(publisher);
            }
            return publisher;
        }
        return null;
    }

    public Set<Series> getSeries() {
        return this.seriesMap;
    }

    public Set<Publisher> getPublishers() {
        return this.publishersMap;
    }

    public Publisher getLastInsertedPublisher() {
        Iterator<Publisher> iPublishers = this.publishersMap.iterator();
        Publisher publisher = null;
        while (iPublishers.hasNext()) {
            publisher = iPublishers.next();
        }
        return publisher;
    }

    public Set<Author> getAuthors() {
        return this.authorsMap;
    }

    public Author getLastInsertedAuthor() {
        Iterator<Author> iAuthors = this.authorsMap.iterator();
        Author author = null;
        while (iAuthors.hasNext()) {
            author = iAuthors.next();
        }
        return author;
    }

    public Set<Book> getBooks() {
        return this.booksMap;
    }

    public Book getLastInsertedBook() {
        Iterator<Book> iBooks = this.booksMap.iterator();
        Book book = null;
        while (iBooks.hasNext()) {
            book = iBooks.next();
        }
        return book;
    }

    public Set<BookAuthor> getBookAuthorLinks() {
        return this.bookAuthorMap;
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

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
    private SearchableRecordSet<Book> bookSet;
    private SearchableRecordSet<Series> seriesSet;
    private SearchableRecordSet<Publisher> publisherSet;
    private SearchableRecordSet<Author> authorSet;
    private SearchableRecordSet<BookAuthor> bookAuthorLinkSet;
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

            this.seriesSet = (SearchableRecordSet<Series>) this.readAllSeries();
            this.publisherSet = (SearchableRecordSet<Publisher>) this.readAllPublishers();
            this.authorSet = (SearchableRecordSet<Author>) this.readAllAuthors();
            this.bookAuthorLinkSet = (SearchableRecordSet<BookAuthor>) this.readAllBookAuthorLinks();

            this.bookDAO = new JDBCBookDAO(seriesSet, publisherSet);
            this.bookSet = (SearchableRecordSet<Book>) this.readAllBooks();
        } catch (DAOException e) {
            System.err.println("Unable to populate collections.");
        }
    }

    public void insertBook(String filePath) throws DAOException, IOException, MetadataReaderException {
        Path file = Path.of(filePath);
        insertBook(file);
    }

    public void insertPublisher(Publisher publisher) throws DAOException {
        this.publisherDAO.create(publisher);
        this.publisherSet.add(publisher);
    }

    public void insertAuthor(Author author) throws DAOException {
        this.authorDAO.create(author);
        this.authorSet.add(author);
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
        this.bookSet.add(book);
    }

    public void updateBook(Book bookToUpdate) throws DAOException {
        this.bookDAO.update(bookToUpdate);
        this.bookSet.forEach(book -> {
            if (book.getId() == bookToUpdate.getId()) {
                bookSet.remove(book);
            }
        });
        this.bookSet.add(bookToUpdate);
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
        bookSet.remove(book);
    }

    public Set<Publisher> readAllPublishers() throws DAOException {
        return publisherDAO.readAll();
    }

    public Publisher readPublisherById(int id) throws DAOException {
        return publisherDAO.read(id);
    }

    public Publisher readPublisherByName(String name) throws DAOException {
        return publisherDAO.read(name);
    }

    public Set<Series> readAllSeries() throws DAOException {
        return seriesDAO.readAll();
    }

    public Series readSeriesById(int id) throws DAOException {
        return seriesDAO.read(id);
    }

    public Series readSeriesByName(String name) throws DAOException {
        return seriesDAO.read(name);
    }

    public Set<Author> readAllAuthors() throws DAOException {
        return authorDAO.readAll();
    }

    public Author readAuthorById(int id) throws DAOException {
        return authorDAO.read(id);
    }

    public Author readAuthorByName(String name) throws DAOException {
        return authorDAO.read(name);
    }

    public Set<BookAuthor> readAllBookAuthorLinks() throws DAOException {
        return bookAuthorDAO.readAll();
    }

    public Set<Book> readAllBooks() throws DAOException {
        return bookDAO.readAll();
    }

    public Author getBookAuthorByBookId(int id) {
        for(BookAuthor ba : bookAuthorLinkSet) {
            if (ba.getBookId() == id)
                return this.authorSet.getById(ba.getAuthorId());
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

        if (bookSet.getByName(book.getTitle()) != null) {
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
        this.bookAuthorLinkSet.add(ba);
    }

    private Author createAuthorFromName(String name) throws DAOException {
        Author author = authorSet.getByName(name);
        if (author == null) {
            author = new Author(name);
            authorDAO.create(author);
            this.authorSet.add(author);
        }
        return author;
    }

    private Publisher createPublisherFromName(String publisherName) throws DAOException {
        if (publisherName != null) {
            Publisher publisher = publisherSet.getByName(publisherName);
            if (publisher == null) {
                publisher = new Publisher(publisherName);
                publisherDAO.create(publisher);
                this.publisherSet.add(publisher);
            }
            return publisher;
        }
        return null;
    }

    public Set<Series> getSeries() {
        return this.seriesSet;
    }

    public Set<Publisher> getPublishers() {
        return this.publisherSet;
    }

    public Publisher getLastInsertedPublisher() {
        Iterator<Publisher> iPublishers = this.publisherSet.iterator();
        Publisher publisher = null;
        while (iPublishers.hasNext()) {
            publisher = iPublishers.next();
        }
        return publisher;
    }

    public Set<Author> getAuthors() {
        return this.authorSet;
    }

    public Author getLastInsertedAuthor() {
        Iterator<Author> iAuthors = this.authorSet.iterator();
        Author author = null;
        while (iAuthors.hasNext()) {
            author = iAuthors.next();
        }
        return author;
    }

    public Set<Book> getBooks() {
        return this.bookSet;
    }

    public Book getLastInsertedBook() {
        Iterator<Book> iBooks = this.bookSet.iterator();
        Book book = null;
        while (iBooks.hasNext()) {
            book = iBooks.next();
        }
        return book;
    }

    public Set<BookAuthor> getBookAuthorLinks() {
        return this.bookAuthorLinkSet;
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

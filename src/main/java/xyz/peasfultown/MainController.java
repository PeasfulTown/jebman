/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Main class for all database interactions concerning books, authors, book series, publishers, etc.
 * This is the main controller for all modules in this program.
 */
package xyz.peasfultown;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.GenericDAO;
import xyz.peasfultown.dao.GenericJointTableDAO;
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
import java.util.*;

/**
 * TODO: upon instantialization, check program's main path for the SQLite database file (metadata.db) and load it.
 */
public class MainController {
    private final Path mainPath;
    private SearchableRecordSet<Book> bookSet;
    private SearchableRecordSet<Series> seriesSet;
    private SearchableRecordSet<Publisher> publisherSet;
    private SearchableRecordSet<Author> authorSet;
    private SearchableRecordSet<Tag> tagSet;
    private SearchableRecordSet<BookAuthor> bookAuthorLinkSet;
    private SearchableRecordSet<BookTag> bookTagLinkSet;
    private GenericDAO<Book> bookDAO;
    private GenericDAO<Series> seriesDAO;
    private GenericDAO<Publisher> publisherDAO;
    private GenericDAO<Author> authorDAO;
    private GenericDAO<Tag> tagDAO;
    private GenericDAO<BookAuthor> bookAuthorDAO;
    private GenericDAO<BookTag> bookTagDAO;

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
            this.tagDAO = new JDBCTagDAO();
            this.bookAuthorDAO = new JDBCBookAuthorDAO();
            this.bookTagDAO = new JDBCBookTagDAO();

            this.seriesSet = (SearchableRecordSet<Series>) this.readAllSeries();
            this.publisherSet = (SearchableRecordSet<Publisher>) this.readAllPublishers();
            this.authorSet = (SearchableRecordSet<Author>) this.readAllAuthors();
            this.tagSet = (SearchableRecordSet<Tag>) this.readAllTags();
            this.bookAuthorLinkSet = (SearchableRecordSet<BookAuthor>) this.readAllBookAuthorLinks();
            this.bookTagLinkSet = (SearchableRecordSet<BookTag>) this.readAllBookTagLinks();

            this.bookDAO = new JDBCBookDAO(seriesSet, publisherSet);
            this.bookSet = (SearchableRecordSet<Book>) this.readAllBooks();
        } catch (DAOException e) {
            System.err.println("Unable to populate collections.");
        }
    }

    public void insertBook(String filePath) throws Exception {
        Path file = Path.of(filePath);
        insertBook(file);
    }

    public void insertSeries(Series series) throws DAOException {
        this.seriesDAO.create(series);
        this.seriesSet.add(series);
    }

    public void insertPublisher(Publisher publisher) throws DAOException {
        this.publisherDAO.create(publisher);
        this.publisherSet.add(publisher);
    }

    public void insertAuthor(Author author) throws DAOException {
        this.authorDAO.create(author);
        this.authorSet.add(author);
    }

    public void insertTag(Tag tag) throws DAOException {
        this.tagDAO.create(tag);
        this.tagSet.add(tag);
    }

    /**
     * Copy book over to main program directory, and insert a record of the book to SQLite database.
     *
     * @param file
     * @throws IOException
     * @throws XMLStreamException
     */
    public void insertBook(Path file) throws Exception {
        if (!Files.exists(file))
            throw new FileNotFoundException("File does not exist.");

        HashMap<String, String> metadata = MetaReader.getMetadata(file);
        Book book = null;
        book = createRecordsFromMetadata(metadata);
        String authorName = metadata
                .getOrDefault("author", metadata.getOrDefault("creator", "Unknown"));

        Path targetPath = getBookTargetDirectoryPath(authorName, book.getTitle(), book.getId())
                .resolve(getBookFileName(book.getTitle(), metadata.get("filetype")));

        addBookToPath(file, targetPath);

        createThumbnail(
                targetPath.toFile(),
                targetPath.getParent().resolve("cover.png"),
                metadata.get("filetype"));

        this.bookSet.add(book);
    }

    public void updateBook(Book bookToUpdate) throws DAOException {
        bookToUpdate.setModifiedDate(Instant.now());
        Series series = bookToUpdate.getSeries();
        if (series != null) {
            if (series.getId() == 0) {
                this.seriesDAO.create(series);
                this.seriesSet.add(series);
            }
        }

        Publisher publisher = bookToUpdate.getPublisher();
        if (publisher != null) {
            if (publisher.getId() == 0) {
                this.publisherDAO.create(publisher);
                this.publisherSet.add(publisher);
            }
        }

        this.bookDAO.update(bookToUpdate);
        Book book = this.bookSet.stream().filter(b -> b.getId() == bookToUpdate.getId()).findFirst().get();
        this.bookSet.remove(book);
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

    public void tagBook(int bookId, int tagId) throws DAOException {
        Tag tag = this.getTagById(tagId);

        if (tag == null)
            throw new DAOException("No tag record with matching ID.");

        this.addBookTagLink(bookId, tag.getId());
    }

    public void tagBook(int bookId, String tagName) throws DAOException {
        Tag tag = this.getTagByName(tagName);

        if (tag == null) {
            tag = new Tag();
            tag.setName(tagName);
            this.insertTag(tag);
        }

        tag = this.getTagByName(tagName);
        this.addBookTagLink(bookId, tag.getId());
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

    public Set<Tag> readAllTags() throws DAOException {
        return tagDAO.readAll();
    }

    public Tag readTagById(int id) throws DAOException {
        return tagDAO.read(id);
    }

    public Tag readTagByName(String name) throws DAOException {
        return tagDAO.read(name);
    }

    public Set<BookAuthor> readAllBookAuthorLinks() throws DAOException {
        return bookAuthorDAO.readAll();
    }

    public Set<BookTag> readAllBookTagLinks() throws DAOException {
        return bookTagDAO.readAll();
    }

    public Set<Book> readAllBooks() throws DAOException {
        return bookDAO.readAll();
    }

    public Author getBookAuthorByBookId(int id) {
        for (BookAuthor ba : bookAuthorLinkSet) {
            if (ba.getBookId() == id)
                return this.authorSet.getById(ba.getAuthorId());
        }
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

    private void addBookToPath(Path file, Path target) throws IOException {
        Path destDir = target.getParent();

        if (!Files.isDirectory(destDir) || !Files.exists(destDir)) {
            Files.createDirectories(destDir);
        }
        Files.copy(file, target);
    }

    private Path getBookTargetDirectoryPath(String authorName, String bookTitle, int id) {
        return Path.of(mainPath.toString(), getRelativePathToBook(authorName, bookTitle, id));
    }

    private String getBookFileName(String bookTitle, String fileType) {
        return String.format("%s.%s", bookTitle, fileType);
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

    private void addBookTagLink(int bookId, int tagId) throws DAOException {
        BookTag bt = new BookTag(bookId, tagId);
        this.bookTagDAO.create(bt);
        this.bookTagLinkSet.add(bt);
    }

    public void removeBookTagLink(int bookId, int tagId) throws DAOException {
        for (BookTag bt : this.bookTagLinkSet) {
            if (bt.getTagId() == tagId && bt.getBookId() == bookId) {
                this.bookTagLinkSet.remove(bt);
                this.bookTagDAO.delete(bt.getId());
                break;
            }
        }
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

    private void createThumbnail(File file, Path targetLocation, String fileType) throws ThumbnailGeneratorException {
        switch (fileType.toLowerCase()) {
            case "pdf":
                ThumbnailGenerator.generatePDFThumbnail(file, targetLocation);
                break;
            case "epub":
                ThumbnailGenerator.generateEpubThumbnail(file, targetLocation);
                break;
            default:
                throw new ThumbnailGeneratorException(String.format("Error while trying to generate thumbnail for %s%n", file.getName()));
        }
    }

    public Set<Book> getBooks() {
        return this.bookSet;
    }

    public Book getBookById(int id) {
        for (Book b : this.getBooks()) {
            if (b.getId() == id)
                return b;
        }

        return null;
    }

    public Book getBookByTitle(String title) {
        return this.bookSet.getByName(title);
    }

    public Set<Book> getBooksByAuthor(String authorName) {
        Set<Book> authorsBooks = new HashSet<>();
        Author author = getAuthorByName(authorName);
        for (BookAuthor ba : this.getBookAuthorLinksByAuthorId(author.getId())) {
            authorsBooks.add(getBookById(ba.getBookId()));
        }

        return authorsBooks;
    }

    public Set<Book> getBooksByPublisher(String publisherName) {
        Set<Book> publishersBooks = new HashSet<>();
        Publisher publisher = getPublisherByName(publisherName);
        for (Book b : bookSet) {
            if (b.getPublisher() != null) {
                if (b.getPublisher().equals(publisher)) {
                    publishersBooks.add(b);
                }
            }
        }
        return publishersBooks;
    }

    public Set<Integer> readBookIdsByTagId(int id) throws DAOException {
        return ((GenericJointTableDAO) this.bookTagDAO).readFirstColIdsBySecondColIds(id);
    }

    public Set<Book> getBooksByTag(Tag tag) throws DAOException {
        return getBooksByTag(tag.getId());
    }

    public Set<Book> getBooksByTag(String tagName) throws DAOException {
        Tag tag = this.tagSet.getByName(tagName);
        if (tag == null)
            this.insertTag(tag);

        return getBooksByTag(tag.getId());
    }

    public Set<Book> getBooksByTag(int tagId) throws DAOException {
        Set<Integer> ids = this.readBookIdsByTagId(tagId);
        Set<Book> books = new HashSet<>();
        for (int id : ids) {
            books.add(this.bookSet.getById(id));
        }
        return books;
    }

    public Set<Book> getBooksBySeries(String seriesName) {
        Series series = getSeriesByName(seriesName);
        Set<Book> seriesBooks = new HashSet<>();
        for (Book b : bookSet) {
            if (b.getSeries() != null) {
                if (b.getSeries().equals(series)) {
                    seriesBooks.add(b);
                }
            }
        }

        return seriesBooks;
    }

    public Set<Tag> getTags() {
        return this.tagSet;
    }

    public Set<Tag> getTagsOfBook(Book book) throws DAOException {
        return getTagsOfBook(book.getId());
    }

    public Set<Tag> getTagsOfBook(int bookId) throws DAOException {
        Set<Integer> tagIds = ((GenericJointTableDAO) this.bookTagDAO).readSecondColIdsByFirstColIds(bookId);
        Set<Tag> tags = new LinkedHashSet<>();
        for (int id : tagIds) {
            tags.add(getTagById(id));
        }
        return tags;
    }

    public Set<Series> getSeries() {
        return this.seriesSet;
    }

    public Series getSeriesByName(String seriesName) {
        for (Series s : seriesSet) {
            if (s.getName().equals(seriesName)) {
                return s;
            }
        }
        return null;
    }

    public Set<Publisher> getPublishers() {
        return this.publisherSet;
    }

    public Publisher getPublisherByName(String name) {
        return this.publisherSet.getByName(name);
    }

    public Set<Author> getAuthors() {
        return this.authorSet;
    }

    public Author getAuthorByName(String name) {
        return this.authorSet.getByName(name);
    }

    public Tag getTagById(int id) {
        return this.tagSet.stream().filter(t -> t.getId() == id).findFirst().get();
    }

    public Tag getTagByName(String name) {
        return this.tagSet.getByName(name);
    }

    public Set<BookAuthor> getBookAuthorLinks() {
        return this.bookAuthorLinkSet;
    }

    public Set<BookAuthor> getBookAuthorLinksByAuthorId(int authorId) {
        Set<BookAuthor> bookAuthorLinks = new LinkedHashSet<>();
        for (BookAuthor ba : this.getBookAuthorLinks()) {
            if (ba.getAuthorId() == authorId)
                bookAuthorLinks.add(ba);
        }

        return bookAuthorLinks;
    }

    public Set<BookTag> getBookTagLinks() {
        return this.bookTagLinkSet;
    }

    public Book getLastInsertedBook() {
        Iterator<Book> iBooks = this.bookSet.iterator();
        Book book = null;
        while (iBooks.hasNext()) {
            book = iBooks.next();
        }
        return book;
    }

    public Publisher getLastInsertedPublisher() {
        Iterator<Publisher> iPublishers = this.publisherSet.iterator();
        Publisher publisher = null;
        while (iPublishers.hasNext()) {
            publisher = iPublishers.next();
        }
        return publisher;
    }

    public Author getLastInsertedAuthor() {
        Iterator<Author> iAuthors = this.authorSet.iterator();
        Author author = null;
        while (iAuthors.hasNext()) {
            author = iAuthors.next();
        }
        return author;
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

package xyz.peasfultown.library;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.library.base.Book;
import xyz.peasfultown.library.base.BookSeries;

public class BookSeriesTest {
    private static final Logger logger = LoggerFactory.getLogger(BookSeriesTest.class);

    private Book b0, b1, b2, b3, b4, b5, b6, b7, b8, b9;
    private BookSeries bookseries0;

    @BeforeEach
    void setUp() {
        logger.info("test setting up");
        b0 = new Book("0.5 Virgins");
        b1 = new Book("1 Outlander");
        b2 = new Book("2 Dragonfly in Amber");

        b0.setNumberInSeries(0.5);
        b1.setNumberInSeries(1);
        b2.setNumberInSeries(2);


        bookseries0 = new BookSeries("Outlander");
        bookseries0.addBook(b0);
        bookseries0.addBook(b1);
        bookseries0.addBook(b2);
    }

    @Test
    void testConstructor0() {
        logger.info("executing test for BookSeries constructor 0");

        assertNotNull(bookseries0, "series object shouldn't be null");
        assertNotNull(bookseries0.getName(), "series's name shouldn't be null");
        assertNotNull(bookseries0.getBooks(), "series' book array shouldn't be null");
        assertTrue(bookseries0.getNumberOfBooks() > 0, "series' book array size should not be 0");
        assertTrue(bookseries0.getNumberOfBooks() == 3, "series' book array size should equal 3");
        assertTrue(bookseries0.getBookByIndex(0).equals(b0), "book's name in series should be the same");
        assertTrue(bookseries0.getBookByIndex(1).equals(b1), "book's name in series should be the same");
        assertTrue(bookseries0.getBookByIndex(2).equals(b2), "book's name in series should be the same");
        assertTrue(bookseries0.getBookByBookNumber(0.5).equals(b0), "book at the specified book should be equal to the defined book");
        assertTrue(bookseries0.getBookByBookNumber(1).equals(b1), "book at the specified book number should be equal to the defined book");
        assertTrue(bookseries0.getBookByBookNumber(2).equals(b2), "book at the specified book number should be equal to the defined book");
    }

    @Test
    void testGetName() {
        logger.info("executing test for getting book series name");
        assertNotNull(bookseries0.getName());
        assertEquals("Outlander", bookseries0.getName());
    }

    @Test
    void testSetName() {
        logger.info("executing test for setting name of book series");
        String bookname0 = "The Era of Madness";
        bookseries0.setName(bookname0);

        assertEquals(bookname0, bookseries0.getName(), "book name should be The Era of Madness");

        String bookname1 = "The Age of Madness";
        bookseries0.setName(bookname1);

        assertEquals(bookname1, bookseries0.getName(), "book name should be The Age of Madness");
    }

    @Test
    void testGetNumberOfBooksInSeries() {
        logger.info("executing test for getting number of books in series");

        int numberOfBooks = 3;

        assertTrue(numberOfBooks == bookseries0.getNumberOfBooks(), "number of books should be 3");
    }

    @Test
    void addingBookIncrementsBookSeriesBooksArray() {
        logger.info("executing test for book increment after adding book");

        BookSeries bookseries1 = new BookSeries("Outlander");
        int numberOfBooks2 = bookseries1.getNumberOfBooks();
        assertEquals(0, numberOfBooks2, "number of books should have been 0");
        bookseries1.addBook(new Book("Outlander"));
        bookseries1.addBook(new Book("Dragonfly in Amber"));
        int numberOfBooks3 = bookseries1.getNumberOfBooks();

        assertTrue(numberOfBooks2 < numberOfBooks3, "number of books in series should have been incremented after adding book to series");
        assertEquals(2, numberOfBooks3, "number of books in series should be 2");
    }

    @Test
    void addingBookStoresCorrectBookObjectInformation() {
        logger.info("executing test for adding book correctly");
        BookSeries bookseries1 = new BookSeries("Outlander");
        Book bAlt0 = new Book("Outlander"),
            bAlt1 = new Book("Dragonfly in Amber");

        bAlt0.setNumberInSeries(1);
        bAlt1.setNumberInSeries(2);

        bookseries1.addBook(bAlt0);
        bookseries1.addBook(bAlt1);

        assertNotNull(bookseries1.getBookByBookNumber(1));
        assertNotNull(bookseries1.getBookByBookNumber(2));
        assertTrue(bookseries1.getBookByBookNumber(1).equals(bAlt0));
        assertTrue(bookseries1.getBookByBookNumber(2).equals(bAlt1));

        assertNull(bookseries1.getBookByBookNumber(3));
    }

    @AfterEach
    void tearDown() {
        this.bookseries0 = null;
    }
}
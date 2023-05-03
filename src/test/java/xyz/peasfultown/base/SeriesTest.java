/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for BookSeries class.
 */
package xyz.peasfultown.base;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.peasfultown.db.BookDb;

public class SeriesTest {
    private static final Logger logger = LoggerFactory.getLogger(SeriesTest.class);

    private Book b0, b1, b2;
    private Series bookseries0;
    BookDb bookdb;

    @BeforeEach
    void setUp() {
        logger.info("test setting up");
        b0 = new Book("0.5 Virgins");
        b1 = new Book("1 Outlander");
        b2 = new Book("2 Dragonfly in Amber");

        b0.setSeriesNumber(0.5);
        b1.setSeriesNumber(1);
        b2.setSeriesNumber(2);

        bookseries0 = new Series("Outlander");
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
        assertTrue(bookseries0.getBookByIndex(0).equals(b0), "book's name in series " +
                "should be the same");
        assertTrue(bookseries0.getBookByIndex(1).equals(b1), "book's name in series " +
                "should be the same");
        assertTrue(bookseries0.getBookByIndex(2).equals(b2), "book's name in series " +
                "should be the same");
        assertTrue(bookseries0.getBookByBookNumber(0.5).equals(b0), "book at the specified " +
                "book should be equal to the defined book");
        assertTrue(bookseries0.getBookByBookNumber(1).equals(b1), "book at the specified " +
                "book number should be equal to the defined book");
        assertTrue(bookseries0.getBookByBookNumber(2).equals(b2), "book at the specified " +
                "book number should be equal to the defined book");
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

        Series bookseries1 = new Series("Outlander");
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
        Series bookseries1 = new Series("Outlander");
        Book bAlt0 = new Book("Outlander"),
            bAlt1 = new Book("Dragonfly in Amber");

        bAlt0.setSeriesNumber(1);
        bAlt1.setSeriesNumber(2);

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
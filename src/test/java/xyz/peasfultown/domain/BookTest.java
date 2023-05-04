/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown <peasfultown@gmail.com>
 * Description: Tests for BookTest class.
 */
package xyz.peasfultown.domain;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    private static final Logger logger = LoggerFactory.getLogger(BookTest.class);
    private static Book book1, book2, book3, book4, book5, book6;
    private static Author author1, author2, author3;
    private static Publisher publisher1;

    @BeforeAll
    static void setUp() {
        logger.info("Setting up before each");
        author1 = new Author("Joe Abercrombie");

        author2 = new Author("Joshua Bloch");
        author3 = new Author("Anonymous");

        publisher1 = new Publisher("Gollancz");

        // all fields
        book1 = new Book("The Blade Itself");
        book1.setIsbn("9780575079793");
        book1.setPublishDate(Book.toTimeStamp(2007, 3, 8));
        book1.setAuthor(author1);
        book1.setPublisher(publisher1);

        book2 = new Book("Before They Are Hanged");
        book2.setIsbn("9780575077881");
        book2.setPublishDate(Book.toTimeStamp(2007, 3, 15));
        book2.setAuthor(author1);
        book2.setPublisher(publisher1);

        book3 = new Book("Last Argument of Kings");
        book3.setIsbn("9780575077904");
        book3.setPublishDate(Book.toTimeStamp(2008, 3, 20));
        book3.setAuthor(author1);
        book3.setPublisher(publisher1);

        // title only
        book4 = new Book("Beggars, Cheats and Forgers");
        book5 = new Book("Silence of the Archive");
        book6 = new Book("The pragmatic programmer");
    }

    @Test
    void testConstructor() {
        logger.info("executing test for all fields");
        // ISBN
        assertEquals("9780575079793", book1.getIsbn(), "ISBN of book incorrect");
        assertEquals("9780575077881", book2.getIsbn(), "ISBN of book incorrect");
        assertEquals("9780575077904", book3.getIsbn(), "ISBN of book incorrect");

        // Titles
        assertEquals("The Blade Itself", book1.getTitle(), "Title of book incorrect");
        assertEquals("Before They Are Hanged", book2.getTitle(), "Title of book incorrect");
        assertEquals("Last Argument of Kings", book3.getTitle(), "Title of book incorrect");

        assertEquals("Beggars, Cheats and Forgers", book4.getTitle(), "Title of book incorrect");
        assertEquals("Silence of the Archive", book5.getTitle(), "Title of book incorrect");
        assertEquals("The pragmatic programmer", book6.getTitle(), "Title of book incorrect");

    }

    @Test
    void testToTimeStamp() {
        Instant d1 = Book.toTimeStamp(2007, 3, 8);
        Instant d2 = Book.toTimeStamp(2007, 3, 15);
        Instant d3 = Book.toTimeStamp(2008, 3, 20);

        assertEquals(d1, book1.getPublishDate(), "Date field incorrect");
        assertEquals(d2, book2.getPublishDate(), "Date field incorrect");
        assertEquals(d3, book3.getPublishDate(), "Date field incorrect");
    }

    @Test
    void testDefaultNumberInSeries() {
        assertEquals(1.0, book1.getSeriesNumber());
        assertEquals(1.0, book2.getSeriesNumber());
        assertEquals(1.0, book3.getSeriesNumber());
    }

    @Test
    void testAuthorsCorrect() {
        assertEquals(author1, book1.getAuthors());
        assertEquals(author1, book2.getAuthors());
        assertEquals(author1, book3.getAuthors());
    }

    @Test
    void testPublisherCorrect() {
        assertEquals(publisher1, book1.getPublisher());
        assertEquals(publisher1, book2.getPublisher());
        assertEquals(publisher1, book3.getPublisher());
    }

    @Test
    void testEqualsCorrect() {
        logger.info("executing test for equals");

        Book newBook1 = new Book("The Blade Itself");
        newBook1.setId(1);
        newBook1.setIsbn("9780575079793");
        newBook1.setPublishDate(Book.toTimeStamp(2007, 3, 8));
        newBook1.setAuthor(author1);
        newBook1.setPublisher(publisher1);

        Book newBook2 = new Book("Before They Are Hanged");
        newBook2.setId(2);
        newBook2.setIsbn("9780575077881");
        newBook2.setPublishDate(Book.toTimeStamp(2007, 3, 15));
        newBook2.setAuthor(author1);
        newBook2.setPublisher(publisher1);

        Book newBook3 = new Book("Last Argument of Kings");
        newBook3.setId(3);
        newBook3.setIsbn("9780575077904");
        newBook3.setPublishDate(Book.toTimeStamp(2008, 3, 20));
        newBook3.setAuthor(author1);
        newBook3.setPublisher(publisher1);

        assertTrue(newBook1.equals(book1), "Objects should be equal");
        assertTrue(newBook2.equals(book2), "Objects should be equal");
        assertTrue(newBook3.equals(book3), "Objects should be equal");

        assertFalse(book1.equals(book2), "Objects should NOT be equal");
        assertFalse(book2.equals(book3), "Objects should NOT be equal");
        assertFalse(book3.equals(book1), "Objects should NOT be equal");
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

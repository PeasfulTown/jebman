package xyz.peasfultown.library.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.peasfultown.library.base.Author;
import xyz.peasfultown.library.base.Book;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;


public class AuthorTest {
    private static final Logger logger = LoggerFactory.getLogger(AuthorTest.class);
    private Author author;
    private ArrayList<Book> books;

    @BeforeEach
    public void setUp() {
        logger.info("Setting up test for Author class");
        author = new Author("Joe Abercrombie");
        books = new ArrayList<>();
        books.add(new Book(
                "9780316187169",
                "A Little Hatred",
                new Author[]{ author },
                new Calendar.Builder()
                        .setDate(2019, 9, 17)
                        .build()
                        .getTime()
        ));
        books.add(new Book(
                "9780575095915",
                "The Trouble With Peace",
                new Author[]{ author },
                new Calendar.Builder()
                        .setDate(2020, 9, 15)
                        .build()
                        .getTime()
        ));
        books.add(new Book(
                "9780575095977",
                "The Wisdom of Crowds",
                new Author[]{ author },
                new Calendar.Builder()
                        .setDate(2021, 9, 16)
                        .build()
                        .getTime()
        ));
        for (Book b : books) {
            author.addBook(b);
        }
    }

    @Test
    public void testAuthorNameCorrect() {
        logger.info("executing test for author name field");
        assertEquals("Joe Abercrombie", author.getName(), "Failure, initialized author name incorrect");
        assertNotNull(author.getName(), "Author name is null when it's not supposed to be null.");
    }

    @Test
    public void testReturnAuthoredBooks() {
        logger.info("executing test for returning authored books");
        assertNotNull(author.getAuthoredBooks());
        assertEquals(books, author.getAuthoredBooks(), "Authored books incorrect.");
    }

    @Test
    public void testAddAuthoredBooks() {
        logger.info("executing test for adding authored books");
        int numberOfBooks1 = this.author.getAuthoredBooks().size();
        this.author.addBook(new Book(
            "9780575079793",
            "The Blade Itself",
            new Author[]{ author },
            new Calendar.Builder()
                    .setDate(2007, 3, 8)
                    .build()
                    .getTime()
        ));

        int numberOfBooks2 = this.author.getAuthoredBooks().size();

        assertTrue(numberOfBooks2 > numberOfBooks1);
        assertArrayEquals(this.author.getAuthoredBooks().get(numberOfBooks2 - 1).getAuthorsAsArray(), new Author[]{ this.author });
    }

    @Test
    public void testEqualsAuthor() {
        logger.info("executing test for equals method");
        Book newBook1 = new Book("Effective Java");
        Author newAuthor = new Author("Joe Abercrombie");
        Author differentAuthor = new Author("Joshua Bloch", newBook1);

        assertTrue(author.equals(newAuthor), "Author of same name not equal to author when they should be equal.");
        assertFalse(author.equals(differentAuthor), "Author of a different name should not equal to current author.");

        for (Book b : books) {
            assertTrue(newAuthor.equals(b.getAuthorsAsArray()[0]));
        }
    }

    @Test
    public void getArrayOfAuthorsObjsFromString() {
        logger.info("executing test for getting array of authors objects from a string object");
        Author[] auArr = new Author[] {
                new Author("Joshua Bloch"),
                new Author("Joe Abercrombie"),
                new Author("Jane Austen"),
                new Author("Neil Gaiman"),
        };

        // Author names should be separated by commas
        String auStr1 = "Joshua Bloch, Joe Abercrombie, Jane Austen, Neil Gaiman";
        String auStr2 = "Joshua Bloch,Joe Abercrombie,Jane Austen,Neil Gaiman";

        assertTrue(Arrays.equals(Author.getArrayOfAuthorsObjectsFromString(auStr1), auArr));
        assertTrue(Arrays.equals(Author.getArrayOfAuthorsObjectsFromString(auStr2), auArr));
    }
}
package xyz.peasfultown.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.peasfultown.library.base.Author;
import xyz.peasfultown.library.base.Book;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

public class AuthorTest {
    private Author author;
    private ArrayList<Book> books;

    @BeforeEach
    public void setUp() {
        author = new Author("Joe Abercrombie");
        books = new ArrayList<>();
        books.add(new Book(
                "9780316187169",
                "A Little Hatred",
                new Author[]{ author },
                new Calendar.Builder().setCalendarType(Book.CALENDAR_TYPE)
                        .setDate(2019, 9, 17)
                        .build()
        ));
        books.add(new Book(
                "9780575095915",
                "The Trouble With Peace",
                new Author[]{ author },
                new Calendar.Builder().setCalendarType(Book.CALENDAR_TYPE)
                        .setDate(2020, 9, 15)
                        .build()
        ));
        books.add(new Book(
                "9780575095977",
                "The Wisdom of Crowds",
                new Author[]{ author },
                new Calendar.Builder().setCalendarType(Book.CALENDAR_TYPE)
                        .setDate(2021, 9, 16)
                        .build()
        ));
        for (Book b : books) {
            author.addBook(b);
        }
    }

    @Test
    public void testAuthorNameCorrect() {
        assertEquals("Joe Abercrombie", author.getName(), "Failure, initialized author name incorrect");
        assertNotNull(author.getName(), "Author name is null when it's not supposed to be null.");
    }

    @Test
    public void testReturnAuthoredBooks() {
        assertNotNull(author.getAuthoredBooks());
        assertEquals(books, author.getAuthoredBooks(), "Authored books incorrect.");
    }

    @Test
    public void testAddAuthoredBooks() {
        int numberOfBooks1 = this.author.getAuthoredBooks().size();
        this.author.addBook(new Book(
            "9780575079793",
            "The Blade Itself",
            new Author[]{ author },
            new Calendar.Builder().setCalendarType(Book.CALENDAR_TYPE)
                    .setDate(2007, 3, 8)
                    .build()
        ));

        int numberOfBooks2 = this.author.getAuthoredBooks().size();

        assertTrue(numberOfBooks2 > numberOfBooks1);
        assertArrayEquals(this.author.getAuthoredBooks().get(numberOfBooks2 - 1).getAuthorsAsArray(), new Author[]{ this.author });
    }

    @Test
    public void testEqualsAuthor() {
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
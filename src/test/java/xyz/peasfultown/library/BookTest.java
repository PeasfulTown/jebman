package xyz.peasfultown.library;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import xyz.peasfultown.library.base.Author;
import xyz.peasfultown.library.base.Book;
import xyz.peasfultown.library.base.BookSeries;

import java.util.Arrays;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.*;

public class BookTest {
    private Calendar.Builder cb;
    private Author author1, author2;
    private Book book1, book2, book3, book4, book5, book6, book7, book8, book9, book10;
    BookSeries bookseries0;

    @BeforeEach
    void setUp() {
        cb = new Calendar.Builder().setCalendarType(Book.CALENDAR_TYPE).setLenient(true);
        // all fields
        author1 = new Author("Joe Abercrombie");
        book1 = new Book("9780575079793", "The Blade Itself", author1, cb.setDate(2007, 3, 8).build());
        book2 = new Book("9780575077881", "Before They Are Hanged", author1, cb.setDate(2007, 3, 15).build());
        book3 = new Book("9780575077904", "Last Argument of Kings", author1, cb.setDate(2008, 3, 20).build());

        // title only
        author2 = new Author("David Thomas");
        book4 = new Book("Beggars, Cheats and Forgers");
        book5 = new Book("Silence of the Archive");
        book6 = new Book("The pragmatic programmer");
        book4.setAuthors(author2);
        book5.setAuthors(author2);
        book6.setAuthors(author2);

        // multiple authors
        book7 = new Book("9780060853983",
                "Good Omens",
                new Author[]{new Author("Neil Gaiman")
                        , new Author("Terry Pratchet")},
                cb.setDate(2006, 11, 28)
                        .build());
        book8 = new Book("9780425246849",
                "Heads You Lose",
                new Author[]{new Author("Lisa Lutz"),
                        new Author("David Hayward")},
                cb.setDate(2012, 4, 3)
                        .build());

        book9 = new Book("9781451635812",
                "Between The Lines",
                new Author[]{new Author("Jodi Picoult"),
                        new Author("Samantha van Leer"),},
                cb.setDate(2013, 6, 25)
                        .build());

        bookseries0 = new BookSeries("First Law World");
        this.book1.setSeries(bookseries0);
        this.book2.setSeries(bookseries0);
        this.book3.setSeries(bookseries0);
        bookseries0.addBook(book1);
        bookseries0.addBook(book2);
        bookseries0.addBook(book3);
    }

    @Test
    void testAllFieldsCorrect() {
        // ISBN
        assertEquals("9780575079793", book1.getISBN(), "ISBN of book incorrect");
        assertEquals("9780575077881", book2.getISBN(), "ISBN of book incorrect");
        assertEquals("9780575077904", book3.getISBN(), "ISBN of book incorrect");

        assertNull(book4.getISBN());
        assertNull(book5.getISBN());
        assertNull(book6.getISBN());

        // Titles
        assertEquals("The Blade Itself", book1.getTitle(), "Title of book incorrect");
        assertEquals("Before They Are Hanged", book2.getTitle(), "Title of book incorrect");
        assertEquals("Last Argument of Kings", book3.getTitle(), "Title of book incorrect");

        assertEquals("Beggars, Cheats and Forgers", book4.getTitle(), "Title of book incorrect");
        assertEquals("Silence of the Archive", book5.getTitle(), "Title of book incorrect");
        assertEquals("The pragmatic programmer", book6.getTitle(), "Title of book incorrect");

        // Authors
        Author[] auArr1 = new Author[]{new Author("Joe Abercrombie")};

        Author[] auArr2 = new Author[]{new Author("David Thomas")};

        assertTrue(Arrays.equals(auArr1, book1.getAuthorsAsArray()), "Authors field incorrect");
        assertTrue(Arrays.equals(auArr1, book2.getAuthorsAsArray()), "Authors field incorrect");
        assertTrue(Arrays.equals(auArr1, book3.getAuthorsAsArray()), "Authors field incorrect");

        assertTrue(Arrays.equals(auArr2, book4.getAuthorsAsArray()), "Authors field incorrect");
        assertTrue(Arrays.equals(auArr2, book5.getAuthorsAsArray()), "Authors field incorrect");
        assertTrue(Arrays.equals(auArr2, book6.getAuthorsAsArray()), "Authors field incorrect");

        // Date published
        Calendar
                d1 = new Calendar.Builder()
                .setCalendarType(Book.CALENDAR_TYPE)
                .setDate(2007, 3, 8)
                .build(),
                d2 = new Calendar.Builder()
                        .setCalendarType(Book.CALENDAR_TYPE)
                        .setDate(2007, 3, 15)
                        .build(),
                d3 = new Calendar.Builder()
                        .setCalendarType(Book.CALENDAR_TYPE)
                        .setDate(2008, 3, 20)
                        .build();


        assertEquals(d1, book1.getDatePublished(), "Date field incorrect");
        assertEquals(d2, book2.getDatePublished(), "Date field incorrect");
        assertEquals(d3, book3.getDatePublished(), "Date field incorrect");
    }

    @Test
    void testEqualsCorrect() {
        Book newBook1 = new Book("The Blade Itself");
        newBook1.setISBN("9780575079793");
        newBook1.setAuthors(new Author("Joe Abercrombie"));
        newBook1.setDatePublished(cb.setDate(2007, 3, 8).build());

        Book newBook2 = new Book("9780575077881", "Before They Are Hanged", new Author("Joe Abercrombie"), cb.setDate(2007, 3, 15).build());

        Book newBook3 = new Book("9780575077904", "Last Argument of Kings", author1, new Calendar.Builder().setCalendarType(Book.CALENDAR_TYPE).setDate(2008, 3, 20).build());

        assertTrue(newBook1.equals(book1), "Objects should be equal");
        assertTrue(newBook2.equals(book2), "Objects should be equal");
        assertTrue(newBook3.equals(book3), "Objects should be equal");

        assertFalse(book1.equals(book2), "Objects should NOT be equal");
        assertFalse(book2.equals(book3), "Objects should NOT be equal");
        assertFalse(book3.equals(book1), "Objects should NOT be equal");
    }

    @Test
    void getAuthorsAsStringCorrect() {
        assertTrue("Joe Abercrombie".equals(book1.getAuthorsAsString()), "Get author as string incorrect output");
        assertTrue("David Thomas".equals(book4.getAuthorsAsString()), "Get author as string incorrect output");
        assertTrue("Neil Gaiman, Terry Pratchet".equals(book7.getAuthorsAsString()), "Get multiple authors as string incorrect output");
        assertTrue("Lisa Lutz, David Hayward".equals(book8.getAuthorsAsString()), "Get multiple authors as string incorrect output");
    }

    @Test
    void testGetDatePublishedAsString() {
        assertTrue(book1.getDatePublishedAsString().equals("08/03/2007"));
        assertTrue(book2.getDatePublishedAsString().equals("15/03/2007"));
        assertTrue(book3.getDatePublishedAsString().equals("20/03/2008"));
    }

    @Test
    void testGetCalendarFromString() {
        Calendar
                c1 = cb.setDate(2022, 9, 26).build(),
                c2 = cb.setDate(2023, 10, 29).build(),
                c3 = cb.setDate(2021, 3, 1).build();
        assertTrue(c1.equals(Book.getCalendarFromString("26/09/2022")));
        assertTrue(c2.equals(Book.getCalendarFromString("29/10/2023")));
        assertTrue(c3.equals(Book.getCalendarFromString("1/3/2021")));

        assertFalse(c1.equals(Book.getCalendarFromString("25/09/2022")));
        assertFalse(c2.equals(Book.getCalendarFromString("34/09/2022")));
        assertFalse(c3.equals(Book.getCalendarFromString("2/09/2022")));
    }

    @Test
    void testBookSeriesField() {
        assertTrue(bookseries0.getBooks().contains(book1));
        assertTrue(bookseries0.getBooks().contains(book2));
        assertTrue(bookseries0.getBooks().contains(book3));

        this.book1.setNumberInSeries(1);
        this.book2.setNumberInSeries(2);
        this.book3.setNumberInSeries(3);

        assertTrue(bookseries0.getBookByBookNumber(1).equals(book1));
        assertTrue(bookseries0.getBookByBookNumber(2).equals(book2));
        assertTrue(bookseries0.getBookByBookNumber(3).equals(book3));
    }
}
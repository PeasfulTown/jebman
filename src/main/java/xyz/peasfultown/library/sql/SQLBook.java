package xyz.peasfultown.library.sql;

import xyz.peasfultown.library.base.Author;
import xyz.peasfultown.library.base.Book;
import xyz.peasfultown.library.base.Publisher;

import java.sql.Connection;
import java.util.Date;
import java.util.Calendar;

public class SQLBook extends Book {

    public SQLBook(String title) {
        super(title);
    }

    public SQLBook(String ISBN, String title, Author author, Date datePublished) {
        super(ISBN, title, author, datePublished);
    }

    public SQLBook(String ISBN, String title, Author[] authors, Date datePublished) {
        super(ISBN, title, authors, datePublished);
    }

    public SQLBook(String ISBN, String title, Author[] authors, Date datePublished, Publisher publisher) {
        super(ISBN, title, authors, datePublished, publisher);
    }
}
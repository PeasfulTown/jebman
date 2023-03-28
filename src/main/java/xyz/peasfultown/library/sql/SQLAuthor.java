package xyz.peasfultown.library.sql;

import xyz.peasfultown.library.base.Author;
import xyz.peasfultown.library.base.Book;

import java.util.ArrayList;

public class SQLAuthor extends Author {
    public SQLAuthor(String name) {
        super(name);
    }

    public SQLAuthor(String name, Book book) {
        super(name, book);
    }

    public SQLAuthor(String name, ArrayList<Book> books) {
        super(name, books);
    }
}

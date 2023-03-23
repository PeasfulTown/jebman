package xyz.peasfultown.library.base;

import java.util.Collection;
import java.util.Vector;

public class BookSeries {
    private String name;
    private Vector<Book> books;

    public BookSeries(String name) {
        this.name = name;
        this.books = new Vector<>(8, 1);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumberOfBooks() {
        return this.books.size();

    }

    public Collection<Book> getBooks() {
        return this.books;
    }

    public void addBook(Book bookToAdd) {
        this.books.add(bookToAdd);
    }

    public Book getBookByIndex(int index) {
        return this.books.get(index);
    }

    public Book getBookByBookNumber(double bookNumber) {
        for (Book b : books) {
            if (b.getNumberInSeries() == bookNumber) {
                return b;
            }
        }

        return null;
    }
}
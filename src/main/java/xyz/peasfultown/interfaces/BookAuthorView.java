package xyz.peasfultown.interfaces;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import xyz.peasfultown.domain.Author;
import xyz.peasfultown.domain.Book;

public class BookAuthorView {
    private final ObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObjectProperty<Author> author = new SimpleObjectProperty<>();

    public BookAuthorView(Book book, Author author) {
        this.book.set(book);
        this.author.set(author);
    }

    public Book getBook() {
        return book.get();
    }

    public ObjectProperty<Book> bookProperty() {
        return book;
    }

    public void setBook(Book book) {
        this.book.set(book);
    }

    public Author getAuthor() {
        return author.get();
    }

    public ObjectProperty<Author> authorProperty() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author.set(author);
    }
}

package xyz.peasfultown.interfaces;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import xyz.peasfultown.domain.Author;
import xyz.peasfultown.domain.Book;

public class BookAuthorView {
    private final ObjectProperty<BookView> book = new SimpleObjectProperty<>();
    private final ObjectProperty<AuthorView> author = new SimpleObjectProperty<>();

    public BookAuthorView(BookView book, AuthorView author) {
        this.book.set(book);
        this.author.set(author);
    }

    public BookView getBook() {
        return book.get();
    }

    public ObjectProperty<BookView> bookProperty() {
        return book;
    }

    public void setBook(BookView book) {
        this.book.set(book);
    }

    public AuthorView getAuthor() {
        return author.get();
    }

    public ObjectProperty<AuthorView> authorProperty() {
        return author;
    }

    public void setAuthor(AuthorView author) {
        this.author.set(author);
    }
}

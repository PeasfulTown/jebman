package xyz.peasfultown.interfaces;

import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import xyz.peasfultown.domain.Author;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Tag;

public class BookAuthorView {
    private final ObjectProperty<Book> book = new SimpleObjectProperty<>();
    private final ObjectProperty<Author> author = new SimpleObjectProperty<>();
    private final ObjectProperty<List<Tag>> tags = new SimpleObjectProperty<>();

    public BookAuthorView(Book book, Author author, List<Tag> tags) {
        this.book.set(book);
        this.author.set(author);
        this.tags.set(tags);
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

    public List<Tag> getTags() {
        return tags.get();
    }

    public ObjectProperty<List<Tag>> tagsProperty() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags.set(tags);
    }
}

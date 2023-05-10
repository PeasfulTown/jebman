package xyz.peasfultown.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

public class BookAuthor implements Record {
    private int id;
    private int bookId;
    private int authorId;

    public BookAuthor() {
    }

    public BookAuthor(int bookId, int authorId) {
        this.bookId = bookId;
        this.authorId = authorId;
    }

    public BookAuthor(int id, int bookId, int authorId) {
        this(bookId, authorId);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getName() {
        return String.valueOf(this.getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        BookAuthor that = (BookAuthor) o;

        return new EqualsBuilder().append(id, that.id).append(bookId, that.bookId).append(authorId, that.authorId).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(bookId).append(authorId).toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(",")
                .add(String.valueOf(this.getId()))
                .add(String.valueOf(this.getBookId()))
                .add(String.valueOf(this.getAuthorId()))
                .toString();
    }
}

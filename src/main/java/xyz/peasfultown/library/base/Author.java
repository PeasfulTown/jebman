package xyz.peasfultown.library.base;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;

public class Author {
    private String name;
    private ArrayList<Book> books;

    public Author(String name) {
        this.name = name;
        this.books = new ArrayList<>();
    }

    public Author(String name, Book book) {
        this(name);
        this.books.add(book);
    }

    public Author(String name, ArrayList<Book> books) {
        this(name);
        this.books = books;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addBook(Book book) {
        this.books.add(book);
    }

    public ArrayList<Book> getAuthoredBooks() {
        return this.books;
    }

    public static Author[] getArrayOfAuthorsObjectsFromString(String authors) {
        String[] strArr = authors.split(",");
        Author[] auArr = new Author[strArr.length];
        int i = 0;
        while(i < strArr.length) {
            auArr[i] = new Author(strArr[i].trim());
            i++;
        }

        return auArr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        if (obj == this) { return true; }

        Author author = (Author) obj;

        return new EqualsBuilder()
                .append(this.getName(), author.getName())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(13, 15)
                .append(this.name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return this.getName();
    }
}

package xyz.peasfultown.library.base;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Book {
    private String ISBN;
    private String title;
    private Author[] authors;
    private Date publishDate;
    private Publisher publisher;

    private BookSeries series;
    private double numberInSeries;

    public Book(String title) {
        this.title = title;
        this.ISBN = null;
        this.authors = null;
        this.publishDate = null;
    }

    public Book(String ISBN, String title, Author author, Date publishDate) {
        this(title);
        this.ISBN = ISBN;
        this.authors = new Author[]{author};
        this.publishDate = publishDate;
    }

    public Book(String ISBN, String title, Author[] authors, Date publishDate) {
        this(title);
        this.ISBN = ISBN;
        this.authors = authors;
        this.publishDate = publishDate;
    }

    public Book(String ISBN, String title, Author[] authors, Date publishDate, Publisher publisher) {
        this(ISBN, title, authors, publishDate);
        this.publisher = publisher;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author[] getAuthorsAsArray() {
        return authors;
    }

    public String getAuthorsAsString() {
        if (authors == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < authors.length) {
            sb.append(authors[i].getName());
            if (i < authors.length - 1) {
                sb.append(", ");
            }
            i++;
        }

        return sb.toString();
    }

    public void setAuthors(Author[] authors) {
        this.authors = authors;
    }

    public void setAuthors(Author author) {
        this.authors = new Author[]{author};
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Publisher getPublisher() {
        return this.publisher;
    }

    public void setSeries(BookSeries series) {
        this.series = series;
    }

    public BookSeries getSeries() {
        return this.series;
    }

    public void setNumberInSeries(double number) {
        this.numberInSeries = number;
    }

    public double getNumberInSeries() {
        return this.numberInSeries;
    }

    // ============ Static methods ============

    public static Date getDateFromString(String strOfDate) {
        Calendar.Builder cb = new Calendar.Builder().setLenient(true);

        if (strOfDate == null) {
            return null;
        }

        String[] s = strOfDate.split("/");
        if (s.length != 3) {
            return null;
        }

        Date d;
        try {
            d = cb.setDate(Integer.parseInt(s[2]),
                            Integer.parseInt(s[1]),
                            Integer.parseInt(s[0]))
                    .build()
                    .getTime();

        } catch (IllegalArgumentException e) {
            return null;
        }

        return d;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ISBN", ISBN)
                .append("title", title)
                .append("authors", authors)
                .append("publishDate", publishDate)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Book book = (Book) obj;

        return new EqualsBuilder()
                .append(ISBN, book.ISBN)
                .append(title, book.title)
                .append(authors, book.authors)
                .append(publishDate, book.publishDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(ISBN)
                .append(title)
                .append(authors)
                .append(publishDate)
                .toHashCode();
    }
}
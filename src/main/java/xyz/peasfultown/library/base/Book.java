package xyz.peasfultown.library.base;

import java.util.Calendar;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Book {
    public static final String CALENDAR_TYPE = "iso8601";

    private String ISBN;
    private String title;
    private Author[] authors;
    private Calendar datePublished;
    private Publisher publisher;

    private BookSeries series;
    private double numberInSeries;

    public Book(String title) {
        this.title = title;
        this.ISBN = null;
        this.authors = null;
        this.datePublished = null;
    }

    public Book(String ISBN, String title, Author author, Calendar datePublished) {
        this(title);
        this.ISBN = ISBN;
        this.authors = new Author[] { author };
        this.datePublished = datePublished;
    }

    public Book(String ISBN, String title, Author[] authors, Calendar datePublished) {
        this(title);
        this.ISBN = ISBN;
        this.authors = authors;
        this.datePublished = datePublished;
    }

    public Book(String ISBN, String title, Author[] authors, Calendar datePublished, Publisher publisher) {
        this(ISBN, title, authors, datePublished);
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
        this.authors = new Author[]{ author };
    }

    public Calendar getDatePublished() {
        return datePublished;
    }

    public String getDatePublishedAsString() {
        StringBuilder sb = new StringBuilder();
        Calendar datePublished = this.datePublished;

        int day = datePublished.get(Calendar.DAY_OF_MONTH);
        int month = datePublished.get(Calendar.MONTH);
        int year = datePublished.get(Calendar.YEAR);

        if (day < 10) {
            sb.append(0);
        }
        sb.append(day);
        sb.append('/');
        if (month < 10) {
            sb.append(0);
        }
        sb.append(this.datePublished.get(Calendar.MONTH));
        sb.append('/');
        sb.append(this.datePublished.get(Calendar.YEAR));

        return sb.toString();
    }

    public void setDatePublished(Calendar datePublished) {
        this.datePublished = datePublished;
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

    // Static methods
    public static Calendar getCalendarFromString(String strOfDate) {
        if (strOfDate == null) {
            return null;
        }

        String[] s = strOfDate.split("/");
        if (s.length != 3) {
            return null;
        }

        Calendar c;
        try {
            c = new Calendar.Builder().setCalendarType(CALENDAR_TYPE)
                    .setDate(Integer.parseInt(s[2]), Integer.parseInt(s[1]), Integer.parseInt(s[0]))
                    .build();
        } catch (IllegalArgumentException e) {
            return null;
        }

        return c;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ISBN", ISBN)
                .append("title", title)
                .append("authors", authors)
                .append("datePublished", datePublished)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }

        if (this.getClass() != obj.getClass()) {
            return false;
        }

        if (obj == this) { return true; }

        Book book = (Book) obj;

        return new EqualsBuilder()
                .append(ISBN, book.ISBN)
                .append(title, book.title)
                .append(authors, book.authors)
                .append(datePublished, book.datePublished)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(ISBN)
                .append(title)
                .append(authors)
                .append(datePublished)
                .toHashCode();
    }
}
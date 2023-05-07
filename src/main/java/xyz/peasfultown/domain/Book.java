/**
 * See end of file for extended copyright information.
 * Original Author(s): PeasfulTown (peasfultown@gmail.com)
 * Description: Book object representation.
 */
package xyz.peasfultown.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.StringJoiner;

public class Book {
    private int id;
    private String isbn;
    private String uuid;
    private String title;
    private Series series;
    private double seriesNumber;
    private Publisher publisher;
    private Instant publishDate;
    private Instant addedDate;
    private Instant modifiedDate;
    private String path;

    public Book() {
        this.isbn = "";
        this.uuid = "";
        this.seriesNumber = 1.0;
        this.publishDate = Instant.now().truncatedTo(ChronoUnit.DAYS);
        this.addedDate = this.modifiedDate = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    }

    public Book(String title) {
        this();
        this.title = title;
    }

    public Book(String isbn, String uuid, String title, Instant publishDate) {
        this(title);
        this.isbn = isbn;
        this.uuid = uuid;
        this.publishDate = publishDate.truncatedTo(ChronoUnit.DAYS);
    }

    public Book(int id, String isbn, String uuid, String title, Series series, Double seriesNumber, Publisher publisher,
                Instant publishDate, Instant addedDate, Instant modifiedDate) {
        this(isbn, uuid, title, publishDate);
        this.id = id;
        this.series = series;
        this.seriesNumber = seriesNumber;
        this.publisher = publisher;
        this.addedDate = addedDate.truncatedTo(ChronoUnit.SECONDS);
        this.modifiedDate = modifiedDate.truncatedTo(ChronoUnit.SECONDS);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getUuid() {
        return this.uuid;
    }

    public void  setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSeries(Series series) {
        this.series = series;
    }

    public Series getSeries() {
        return this.series;
    }

    public void setSeriesNumber(double number) {
        this.seriesNumber = number;
    }

    public double getSeriesNumber() {
        return this.seriesNumber;
    }

    public Publisher getPublisher() {
        return this.publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public Instant getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Instant publishDate) {
        this.publishDate = publishDate.truncatedTo(ChronoUnit.DAYS);
    }

    public Instant getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Instant addedDate) {
        this.addedDate = addedDate.truncatedTo(ChronoUnit.SECONDS);
    }

    public Instant getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(Instant modifiedDate) {
        this.modifiedDate = modifiedDate.truncatedTo(ChronoUnit.SECONDS);
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // ============ Static methods ============
    public static Instant toTimeStamp(int year, int month, int dayOfMonth) {
        LocalDate ld = LocalDate.of(year, month, dayOfMonth);

        return ld.atStartOfDay().toInstant(ZoneOffset.UTC);
    }

    @Override
    public String toString() {
        boolean seriesIsNull = getSeries() == null;
        boolean publisherIsNull = getPublisher() == null;

        return new StringJoiner(",")
                .add(String.valueOf(getId()))
                .add(getIsbn())
                .add(getUuid())
                .add(getTitle())
                .add(seriesIsNull ? "0" : String.valueOf(getSeries().getId()))
                .add(seriesIsNull ? "null" : getSeries().getName())
                .add(String.valueOf(getSeriesNumber()))
                .add(publisherIsNull ? "0" : String.valueOf(getPublisher().getId()))
                .add(publisherIsNull ? "null" : getPublisher().getName())
                .add(getPublishDate().toString())
                .add(getAddedDate().toString())
                .add(getModifiedDate().toString())
                .add(getPath()).toString();
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
                .append(isbn, book.isbn)
                .append(title, book.title)
                .append(publishDate, book.publishDate)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(isbn)
                .append(title)
                .append(publishDate)
                .toHashCode();
    }
}

/**
 * The MIT License (MIT)
 * =====================
 * <p>
 * Copyright © 2023 PeasfulTown
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

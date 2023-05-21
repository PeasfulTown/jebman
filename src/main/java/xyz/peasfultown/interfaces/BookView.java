package xyz.peasfultown.interfaces;

import javafx.beans.property.*;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;
import xyz.peasfultown.domain.Series;

import java.time.Instant;

public class BookView {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty isbn = new SimpleStringProperty();
    private final StringProperty uuid = new SimpleStringProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final ObjectProperty<SeriesView> series = new SimpleObjectProperty<>();
    private final DoubleProperty seriesNumber = new SimpleDoubleProperty();
    private final ObjectProperty<PublisherView> publisher = new SimpleObjectProperty<>();
    private final StringProperty datePublished = new SimpleStringProperty();
    private final StringProperty dateAdded = new SimpleStringProperty();
    private final StringProperty dateModified = new SimpleStringProperty();
    private final StringProperty path = new SimpleStringProperty();

    public BookView(int id, String isbn, String uuid, String title, SeriesView series, double seriesNumber,
                    PublisherView publisher, String datePublished, String dateAdded, String dateModified, String path) {
        this.id.set(id);
        this.isbn.set(isbn);
        this.uuid.set(uuid);
        this.title.set(title);
        this.series.set(series);
        this.seriesNumber.set(seriesNumber);
        this.publisher.set(publisher);
        this.datePublished.set(datePublished);
        this.dateAdded.set(dateAdded);
        this.dateModified.set(dateModified);
        this.path.set(path);
    }

    public int getId() {
        return id.get();
    }

    public IntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getIsbn() {
        return isbn.get();
    }

    public StringProperty isbnProperty() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn.set(isbn);
    }

    public String getUuid() {
        return uuid.get();
    }

    public StringProperty uuidProperty() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid.set(uuid);
    }

    public String getTitle() {
        return title.get();
    }

    public StringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public SeriesView getSeries() {
        return series.get();
    }

    public ObjectProperty<SeriesView> seriesProperty() {
        return series;
    }

    public void setSeries(SeriesView series) {
        this.series.set(series);
    }

    public double getSeriesNumber() {
        return seriesNumber.get();
    }

    public DoubleProperty seriesNumberProperty() {
        return seriesNumber;
    }

    public void setSeriesNumber(double seriesNumber) {
        this.seriesNumber.set(seriesNumber);
    }

    public PublisherView getPublisher() {
        return publisher.get();
    }

    public ObjectProperty<PublisherView> publisherProperty() {
        return publisher;
    }

    public void setPublisher(PublisherView publisher) {
        this.publisher.set(publisher);
    }

    public String getDatePublished() {
        return datePublished.get();
    }

    public StringProperty datePublishedProperty() {
        return datePublished;
    }

    public void setDatePublished(String datePublished) {
        this.datePublished.set(datePublished);
    }

    public String getDateAdded() {
        return dateAdded.get();
    }

    public StringProperty dateAddedProperty() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded.set(dateAdded);
    }

    public String getDateModified() {
        return dateModified.get();
    }

    public StringProperty dateModifiedProperty() {
        return dateModified;
    }

    public void setDateModified(String dateModified) {
        this.dateModified.set(dateModified);
    }

    public String getPath() {
        return path.get();
    }

    public StringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public Book getValue() {
        return new Book(this.getId(), this.getIsbn(), this.getUuid(), this.getTitle(),
                this.getSeries() != null
                        ? this.getSeries().getValue()
                        : null, this.getSeriesNumber(),
                this.getPublisher() != null
                        ? this.getPublisher().getValue()
                        : null,
                Instant.parse(this.getDatePublished()),
                Instant.parse(this.getDateAdded()),
                Instant.parse(this.getDateModified()),
                this.getPath());
    }
}

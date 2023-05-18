package xyz.peasfultown.interfaces;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.RecordAlreadyExistsException;
import xyz.peasfultown.domain.*;

import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

// TODO: handle book series edit
public class JebmanGUI extends Application {
    private static MainController mc;

    private final ObservableList<BookAuthorView> data = FXCollections.observableList(new ArrayList<>());
    private final ObservableList<AuthorView> authors = FXCollections.observableList(new ArrayList<>());
    private final ObservableList<PublisherView> publishers = FXCollections.observableList(new ArrayList<>());
    private final ObservableList<SeriesView> series = FXCollections.observableList(new ArrayList<>());

    private final Desktop desktop = Desktop.getDesktop();
    private final ObservableSet<BookView> bookView = null;
    private final ObservableSet<AuthorView> authorView = null;
    private final ObservableSet<PublisherView> publisherView = null;
    private final ObservableSet<SeriesView> seriesView = null;
    private final ObservableSet<BookAuthorView> bookAuthorView = null;

    public static void run(MainController mc) {
        JebmanGUI.mc = mc;
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Jebman - Ebooks Manager");
        stage.setMinHeight(300);
        stage.setMinWidth(600);

        AnchorPane anchor = new AnchorPane();
        GridPane layout = getLayout(stage);
        anchor.getChildren().addAll(layout);
        AnchorPane.setTopAnchor(layout, 10.0);
        AnchorPane.setBottomAnchor(layout, 10.0);
        AnchorPane.setLeftAnchor(layout, 10.0);
        AnchorPane.setRightAnchor(layout, 10.0);

        Scene scene = new Scene(anchor);
        stage.setScene(scene);
        stage.show();
    }

    private GridPane getLayout(Stage stage) {
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(10, 10, 10, 10));
        mainGrid.setGridLinesVisible(true);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPercentWidth(20);
        col2.setPercentWidth(80);
        mainGrid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row1.setPrefHeight(100);
        row2.setVgrow(Priority.ALWAYS);
        mainGrid.getRowConstraints().addAll(row1, row2);

        mainGrid.add(getTopBar(stage), 0, 0, 2, 1);
        mainGrid.add(getBookInfoPanel(), 0, 1);
        mainGrid.add(getBookTable(), 1, 1);

        return mainGrid;
    }

    private VBox getBookInfoPanel() {
        final VBox panel = new VBox();
        // TODO: add info pane
        Label placeHolder = new Label("Info goes here");
        panel.getChildren().addAll(placeHolder);
        panel.setPadding(new Insets(10, 10, 10, 10));

        return panel;
    }

    private TableView<BookAuthorView> getBookTable() {
        TableView<BookAuthorView> table = new TableView<>();
        table.setEditable(true);

        TableColumn<BookAuthorView, Integer> idCol = new TableColumn<>("ID");
        TableColumn<BookAuthorView, String> titleCol = new TableColumn<>("Title");

        TableColumn<BookAuthorView, Integer> authorIdCol = new TableColumn<>("Author ID");
        TableColumn<BookAuthorView, String> authorNameCol = new TableColumn<>("Author");

        TableColumn<BookAuthorView, Integer> seriesIdCol = new TableColumn<>("Series ID");
        TableColumn<BookAuthorView, String> seriesNameCol = new TableColumn<>("Series");
        TableColumn<BookAuthorView, Double> seriesNumberCol = new TableColumn<>("Series Index");

        TableColumn<BookAuthorView, Integer> publisherIdCol = new TableColumn<>("Publisher ID");
        TableColumn<BookAuthorView, String> publisherNameCol = new TableColumn<>("Publisher");

        TableColumn<BookAuthorView, String> datePublishedCol = new TableColumn<>("Date Published");
        TableColumn<BookAuthorView, String> dateAddedCol = new TableColumn<>("Date Added");
        TableColumn<BookAuthorView, String> dateModifiedCol = new TableColumn<>("Date Modified");

        TableColumn<BookAuthorView, String> pathCol = new TableColumn<>("Path");


        idCol.setCellValueFactory(f -> f.getValue().bookProperty().getValue().idProperty().asObject());
        titleCol.setCellValueFactory(f -> f.getValue().bookProperty().getValue().titleProperty());
        titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        titleCol.setEditable(true);
        titleCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    BookView bookView = event.getRowValue().getBook();
                    SeriesView seriesView = bookView.getSeries();
                    PublisherView publisherView = bookView.getPublisher();

                    event.getTableView().getItems().get(
                                    event.getTablePosition().getRow())
                            .getBook().titleProperty().setValue(event.getNewValue());
                    bookView.setTitle(event.getNewValue());

                    Series series = null;
                    if (seriesView != null)
                        series = new Series(seriesView.getId(), seriesView.getName());
                    Publisher publisher = null;
                    if (publisherView != null)
                        publisher = new Publisher(publisherView.getId(), publisherView.getName());

                    Book book = new Book(bookView.getId(), bookView.getIsbn(), bookView.getTitle(), bookView.getUuid(),
                            series, bookView.getSeriesNumber(), publisher,
                            Instant.parse(bookView.getDatePublished()), Instant.parse(bookView.getDateAdded()),
                            Instant.parse(bookView.getDateModified()), bookView.getPath());

                    try {
                        mc.updateBook(book);
                    } catch (DAOException e) {
                        showPopupError(e, "Jebman - Error", "Unable to update ebook record.");
                    }
                });

        authorIdCol.setCellValueFactory(f -> f.getValue().getAuthor().idProperty().asObject());
        authorNameCol.setCellValueFactory(f -> f.getValue().getAuthor().nameProperty());

        publisherIdCol.setCellValueFactory(f -> f.getValue().getBook().getPublisher() != null
                ? f.getValue().getBook().getPublisher().idProperty().asObject()
                : null);
        publisherNameCol.setCellValueFactory(f -> f.getValue()
                .bookProperty().getValue().getPublisher() != null
                ? f.getValue().bookProperty().getValue().publisherProperty().getValue().nameProperty()
                : null);
        publisherNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        publisherNameCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    Publisher pub = null;
                    try {
                        // Check if the Publisher record with the entered name already exists in database, if so, assign
                        // it, otherwise, create and then assign
                        pub = mc.readPublisherByName(event.getNewValue());
                        if (pub == null) {
                            pub = new Publisher(event.getNewValue());
                            mc.insertPublisher(pub);
                        }
                        PublisherView publisherView = new PublisherView(pub.getId(), pub.getName());
                        this.publishers.add(publisherView);
                        this.data.forEach(d -> {
                            if (d.getBook().getId() == event.getRowValue().getBook().getId()) {
                                d.getBook().setPublisher(publisherView);
                            }
                        });
                    } catch (Exception e) {
                        showPopupErrorWithException(e);
                    }
                }
        );

        seriesIdCol.setCellValueFactory(f -> f.getValue().getBook().getSeries() != null
                ? f.getValue().getBook().getSeries().idProperty().asObject()
                : null);
        seriesNameCol.setCellValueFactory(f -> f.getValue().getBook().getSeries() != null
                ? f.getValue().getBook().getSeries().nameProperty()
                : null);
        seriesNumberCol.setCellValueFactory(f -> f.getValue().getBook().seriesNumberProperty().asObject());

        datePublishedCol.setCellValueFactory(f -> f.getValue().getBook().datePublishedProperty());
        dateAddedCol.setCellValueFactory(f -> f.getValue().getBook().dateAddedProperty());
        dateModifiedCol.setCellValueFactory(f -> f.getValue().getBook().dateModifiedProperty());

        pathCol.setCellValueFactory(f -> f.getValue().getBook().pathProperty());

        table.getColumns().addAll(new ArrayList<>(
                Arrays.asList(idCol, titleCol, authorIdCol, authorNameCol,
                        publisherIdCol, publisherNameCol,
                        seriesIdCol, seriesNameCol, seriesNumberCol,
                        datePublishedCol, dateAddedCol, dateModifiedCol, pathCol)));

        data.addAll(collectBookAuthorViewItems());
        table.setItems(data);

        return table;
    }

    private HBox getTopBar(Stage stage) {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPrefHeight(100);

        final FileChooser fileChooser = new FileChooser();
        final Button btnAddBook = new Button("Add Book");

        btnAddBook.setOnAction((final ActionEvent e) -> {
            configureFileChooser(fileChooser);
            File ebookFile = fileChooser.showOpenDialog(stage);
            if (ebookFile != null) {
                try {
                    mc.insertBook(Path.of(ebookFile.getPath()));
                    addPublisherViewIfNotExists(mc.getLastInsertedPublisher(), this.publishers);
                    addAuthorViewIfNotExists(mc.getLastInsertedAuthor(), this.authors);
                    this.data.add(createBookAuthorView(mc.getLastInsertedBook(), this.series, this.publishers, mc.getBookAuthorLinks(), authors));
                    showPopupInfo("Info", ebookFile.getName() + " added to library!");
                } catch (RecordAlreadyExistsException ex) {
                    showPopupError(ex, "Jebman - Error", "Ebook already exists in Jebman library.");
                } catch (Exception ex) {
                    showPopupErrorWithException(ex);
                }
            }
        });

        btnAddBook.setPrefSize(150, 50);
        hbox.getChildren().add(btnAddBook);

        return hbox;
    }

    private static void configureFileChooser(final FileChooser fileChooser) {
        fileChooser.setTitle("Select Ebook to add to library");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ebooks", "*.epub", "*.pdf") // TODO: add .mobi
        );
    }

    private ObservableList<BookAuthorView> collectBookAuthorViewItems() {
        Set<Book> booksInDatabase = mc.getBooks();
        Set<BookAuthor> bookAuthorLinks = mc.getBookAuthorLinks();
        ObservableList<BookAuthorView> bookAuthorViewItems = FXCollections.observableList(new ArrayList<>());
        publishers.addAll(collectPublisherViewItems());
        series.addAll(collectSeriesViewItems());
        authors.addAll(collectAuthorViewItems());

        for (Book b : booksInDatabase) {
            BookAuthorView bookAuthorView = createBookAuthorView(b, series, publishers, bookAuthorLinks, authors);
            this.data.add(bookAuthorView);
        }
        return bookAuthorViewItems;
    }

    private static BookAuthorView createBookAuthorView(Book book, Collection<SeriesView> series,
                                                       Collection<PublisherView> publishers, Collection<BookAuthor> bookAuthorLinks,
                                                       Collection<AuthorView> authors) {
        SeriesView seriesItem = getBookSeries(book, series);
        PublisherView publisherItem = getBookPublisher(book, publishers);

        BookView bookView = createBookView(book, seriesItem, publisherItem);
        AuthorView authorItem = getBookAuthor(book, bookAuthorLinks, authors);

        return new BookAuthorView(bookView, authorItem);
    }

    private static BookView createBookView(Book book, SeriesView series, PublisherView publisher) {
        BookView bv = new BookView(book.getId(), book.getIsbn(), book.getUuid(), book.getTitle(), series, book.getSeriesNumber(),
                publisher, book.getPublishDate().toString(), book.getAddedDate().toString(), book.getModifiedDate().toString(), book.getPath());
        return bv;
    }

    private static AuthorView getBookAuthor(Book book, Collection<BookAuthor> bookAuthorLinks, Collection<AuthorView> authors) {
        return getBookAuthor(book.getId(), bookAuthorLinks, authors);
    }

    private static void addAuthorViewIfNotExists(Author author, Collection<AuthorView> authors) {
        for (AuthorView av : authors) {
            if (av.getName().equals(author.getName())) {
                return;
            }
        }
        authors.add(createAuthorView(author));
    }

    private static AuthorView createAuthorView(Author author) {
        return new AuthorView(author.getId(), author.getName());
    }

    private static AuthorView getBookAuthor(int bookId, Collection<BookAuthor> bookAuthorLinks, Collection<AuthorView> authors) {
        AuthorView av = null;
        for (BookAuthor bookAuthorLink : bookAuthorLinks) {
            if (bookAuthorLink.getBookId() == bookId) {
                av = authors.stream()
                        .filter(a -> a.getId() == bookAuthorLink.getAuthorId())
                        .findFirst().get();
                break;
            }
        }
        return av;
    }

    private static SeriesView getBookSeries(Book book, Collection<SeriesView> series) {
        if (book.getSeries() == null)
            return null;

        return series.stream().filter(seriesItem -> seriesItem.getId() == book.getSeries().getId()).findAny().get();
    }

    private static void addBookSeriesIfNotExists(Series series, Collection<SeriesView> seriesCollection) {
        for (SeriesView sv : seriesCollection) {
            if (sv.getName().equals(series.getName()))
                return;
        }
        seriesCollection.add(createSeriesView(series));
    }

    private static SeriesView createSeriesView(Series series) {
        return new SeriesView(series.getId(), series.getName());
    }

    private static PublisherView getBookPublisher(Book book, Collection<PublisherView> publishers) {
        if (book.getPublisher() == null)
            return null;

        return publishers.stream().filter(publisherItem -> publisherItem.getId() == book.getPublisher().getId()).findAny().get();
    }

    private static void addPublisherViewIfNotExists(Publisher publisher, Collection<PublisherView> publishers) {
        for (PublisherView pv : publishers) {
            if (pv.getName().equals(publisher.getName()))
                return;
        }
        publishers.add(createPublisherView(publisher));
    }

    private static PublisherView createPublisherView(Publisher publisher) {
        return new PublisherView(publisher.getId(), publisher.getName());
    }

    private ObservableSet<AuthorView> collectAuthorViewItems() {
        Set<Author> authors = mc.getAuthors();
        ObservableSet<AuthorView> authorViewItems = FXCollections.observableSet();
        authors.forEach(a -> {
            AuthorView av = new AuthorView(a.getId(), a.getName());
            authorViewItems.add(av);
        });
        return authorViewItems;
    }

    private ObservableSet<PublisherView> collectPublisherViewItems() {
        Set<Publisher> publishers = mc.getPublishers();
        ObservableSet<PublisherView> publisherViewItems = FXCollections.observableSet();
        publishers.forEach(p -> {
            PublisherView pv = new PublisherView(p.getId(), p.getName());
            publisherViewItems.add(pv);
        });
        return publisherViewItems;
    }

    private ObservableSet<SeriesView> collectSeriesViewItems() {
        Set<Series> series = mc.getSeries();
        ObservableSet<SeriesView> seriesViewItems = FXCollections.observableSet();
        series.forEach(s -> {
            SeriesView sv = new SeriesView(s.getId(), s.getName());
            seriesViewItems.add(sv);
        });
        return seriesViewItems;
    }

    private void showPopupInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.show();
    }

    private void showPopupError(Exception ex, String title, String headerText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(ex.getMessage());
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.show();
    }

    private void showPopupErrorWithException(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception occurred");
        alert.setHeaderText("Exception occurred while adding ebook to jebman library");
        alert.setContentText(ex.getMessage());
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        ex.printStackTrace(printWriter);
        String exceptionTrace = stringWriter.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea ta = new TextArea(exceptionTrace);
        ta.setEditable(false);
        ta.setWrapText(true);
        ta.setMaxWidth(Double.MAX_VALUE);
        ta.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(ta, Priority.ALWAYS);
        GridPane.setHgrow(ta, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(ta, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.show();
    }
}

package xyz.peasfultown.interfaces;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.RecordAlreadyExistsException;
import xyz.peasfultown.domain.Author;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;
import xyz.peasfultown.domain.Series;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static xyz.peasfultown.interfaces.GUIHelpers.*;

// TODO: handle book series edit
public class JebmanGUI extends Application {
    private Stage stage;
    private final VBox infoPanel = new VBox();
    private static MainController mc;

    private final ObservableList<BookAuthorView> data = FXCollections.observableList(new ArrayList<>());

    public static void run(MainController mc) {
        JebmanGUI.mc = mc;
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setTitle("Jebman - Ebooks Manager");
        stage.setMinHeight(300);
        stage.setMinWidth(600);

        AnchorPane anchor = new AnchorPane();
        GridPane layout = getLayout();
        anchor.getChildren().addAll(layout);
        AnchorPane.setTopAnchor(layout, 10.0);
        AnchorPane.setBottomAnchor(layout, 10.0);
        AnchorPane.setLeftAnchor(layout, 10.0);
        AnchorPane.setRightAnchor(layout, 10.0);

        Scene scene = new Scene(anchor);
        stage.setScene(scene);
        stage.show();
    }

    private GridPane getLayout() {
        GridPane mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(10, 10, 10, 10));
        mainGrid.setGridLinesVisible(true);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setPrefWidth(200);
        col2.setHgrow(Priority.ALWAYS);
        mainGrid.getColumnConstraints().addAll(col1, col2);

        RowConstraints row1 = new RowConstraints();
        RowConstraints row2 = new RowConstraints();
        row1.setPrefHeight(100);
        row2.setVgrow(Priority.ALWAYS);
        mainGrid.getRowConstraints().addAll(row1, row2);

        mainGrid.add(getTopBar(), 0, 0, 2, 1);
        mainGrid.add(getBookInfoPanel(), 0, 1);
        mainGrid.add(getBookTable(), 1, 1);

        return mainGrid;
    }

    private VBox getBookInfoPanel() {
        Label infoLabel = new Label("Info");
        Label isbnLbl = new Label("ISBN: ");
        Label titleLbl = new Label("Title: ");
        Label publisherLbl = new Label("Publisher: ");
        Label publishDateLbl = new Label("Publish Date: ");
        Label authorLbl = new Label("Author(s): ");
        Label tagsLbl = new Label("Tag(s): ");
        // TODO: use generated thumbnail if exists, use placeholder `nocover.png` thumbnail if not exist
        ImageView cover = new ImageView(new Image(getClass().getClassLoader().getResourceAsStream("nocover.png")));
        cover.setPreserveRatio(true);
        cover.setFitWidth(150);
        this.infoPanel.getChildren().addAll(infoLabel, cover, isbnLbl, titleLbl, publisherLbl, publishDateLbl, authorLbl, tagsLbl);
        this.infoPanel.setPadding(new Insets(10, 10, 10, 10));

        return infoPanel;
    }

    private TableView<BookAuthorView> getBookTable() {
        TableView<BookAuthorView> table = new TableView<>();
        table.setEditable(true);

        TableColumn<BookAuthorView, Integer> bookIdCol = this.getBookIdColumn();
        TableColumn<BookAuthorView, String> bookTitleCol = this.getBookTitleColumn();

        TableColumn<BookAuthorView, Integer> authorIdCol = this.getAuthorIdColumn();
        TableColumn<BookAuthorView, String> authorNameCol = this.getAuthorNameColumn();

        TableColumn<BookAuthorView, Integer> seriesIdCol = this.getSeriesIdColumn();
        TableColumn<BookAuthorView, String> seriesNameCol = this.getSeriesNameColumn();
        TableColumn<BookAuthorView, Double> seriesNumberCol = this.getSeriesNumberColumn();

        TableColumn<BookAuthorView, Integer> publisherIdCol = this.getPublisherIdColumn();
        TableColumn<BookAuthorView, String> publisherNameCol = this.getPublisherNameColumn();

        TableColumn<BookAuthorView, String> datePublishedCol = this.getDatePublishedColumn();
        TableColumn<BookAuthorView, String> dateAddedCol = this.getDateAddedColumn();
        TableColumn<BookAuthorView, String> dateModifiedCol = this.getDateModifiedColumn();

        TableColumn<BookAuthorView, String> pathCol = this.getPathColumn();

        table.getColumns().addAll(new ArrayList<>(
                Arrays.asList(bookIdCol, bookTitleCol, authorIdCol, authorNameCol,
                        publisherIdCol, publisherNameCol,
                        seriesIdCol, seriesNameCol, seriesNumberCol,
                        datePublishedCol, dateAddedCol, dateModifiedCol, pathCol)));

        data.addAll(collectBookAuthorViewItems());
        table.setItems(data);

        table.getFocusModel().focusedItemProperty().addListener((observableValue, bookAuthorView, t1) -> {
            System.out.println("focus changed to: " + table.getFocusModel().getFocusedCell());
            try {
                ((ImageView) infoPanel.getChildren().get(1)).setImage(
                        new Image(Files.newInputStream(ApplicationConfig.MAIN_PATH.resolve(bookAuthorView.getBook().getPath()).resolve("cover.png"))));
            } catch (Exception e) {
                showPopupErrorWithExceptionStack(e);
            }
            ((Label) infoPanel.getChildren().get(2)).setText("ISBN: " + bookAuthorView.getBook().getIsbn());
            ((Label) infoPanel.getChildren().get(3)).setText("Title: " + bookAuthorView.getBook().getTitle());
            ((Label) infoPanel.getChildren().get(4)).setText("Publisher: " + (bookAuthorView.getBook().getPublisher() != null ? bookAuthorView.getBook().getPublisher().getName() : "Unknown"));
            ((Label) infoPanel.getChildren().get(5)).setText("Publish Date: " + bookAuthorView.getBook().getPublishDate().toString());
            ((Label) infoPanel.getChildren().get(6)).setText("Author(s): " + bookAuthorView.getAuthor().getName());
            ((Label) infoPanel.getChildren().get(7)).setText("Tag(s): " + "Place holder");
        });

        return table;
    }

    private HBox getTopBar() {
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
            File ebookFile = fileChooser.showOpenDialog(this.stage);
            if (ebookFile != null) {
                try {
                    mc.insertBook(Path.of(ebookFile.getPath()));
                    this.data.add(createBookAuthorView(mc.getLastInsertedBook()));
                    showPopupInfo("Info", ebookFile.getName() + " added to library!");
                } catch (RecordAlreadyExistsException ex) {
                    showPopupError(ex, "Jebman - Error", "Ebook already exists in Jebman library.");
                } catch (Exception ex) {
                    showPopupErrorWithExceptionStack(ex, "Exception occurred while adding ebook to jebman library");
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
        ObservableList<BookAuthorView> bookAuthorViewItems = FXCollections.observableList(new ArrayList<>());
        for (Book b : booksInDatabase) {
            BookAuthorView bookAuthorView = createBookAuthorView(b);
            bookAuthorViewItems.add(bookAuthorView);
        }
        return bookAuthorViewItems;
    }

    private BookAuthorView createBookAuthorView(Book book) {
        Author author = mc.getBookAuthorByBookId(book.getId());
        return new BookAuthorView(book, author);
    }

    private TableColumn<BookAuthorView, Integer> getBookIdColumn() {
        TableColumn<BookAuthorView, Integer> bookIdCol = new TableColumn<>("ID");
        bookIdCol.setCellValueFactory(f -> new SimpleIntegerProperty(f.getValue().getBook().getId()).asObject());
        return bookIdCol;
    }

    private TableColumn<BookAuthorView, String> getBookTitleColumn() {
        TableColumn<BookAuthorView, String> bookTitleCol = new TableColumn<>("Title");
        bookTitleCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getTitle()));
        bookTitleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        bookTitleCol.setEditable(true);
        bookTitleCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    try {
                        // Update book title only if there's no other books with the same title already in the database
                        Book book = mc.getBookByTitle(event.getNewValue());
                        if (book == null) {
                            book = new Book(event.getNewValue());
                            mc.updateBook(book);
                        } else {
                            showPopupError("Error", "A book with that title already exists in the database.");
                        }
                    } catch (Exception e) {
                        showPopupErrorWithExceptionStack(e);
                    }
                });
        return bookTitleCol;
    }

    private TableColumn<BookAuthorView, Integer> getAuthorIdColumn() {
        TableColumn<BookAuthorView, Integer> authorIdCol = new TableColumn<>("Author ID");
        authorIdCol.setCellValueFactory(f -> new SimpleIntegerProperty(f.getValue().getAuthor().getId()).asObject());
        return authorIdCol;
    }

    private TableColumn<BookAuthorView, String> getAuthorNameColumn() {
        TableColumn<BookAuthorView, String> authorNameCol = new TableColumn<>("Author");
        authorNameCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getAuthor().getName()));
        authorNameCol.setEditable(true);
        authorNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        authorNameCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    try {
                        Author author = mc.getAuthorByName(event.getNewValue());
                        if (author == null) {
                            author = new Author(event.getNewValue());
                            mc.insertAuthor(author);
                        }
                        BookAuthorView bav = event.getRowValue();
                        bav.setAuthor(author);
                        event.getTableView().getItems().set(event.getTablePosition().getRow(), bav);
                    } catch (Exception ex) {
                        showPopupErrorWithExceptionStack(ex);
                    }
                });
        return authorNameCol;
    }

    private TableColumn<BookAuthorView, Integer> getPublisherIdColumn() {
        TableColumn<BookAuthorView, Integer> publisherIdCol = new TableColumn<>("Publisher ID");
        publisherIdCol.setCellValueFactory(f -> f.getValue().getBook().getPublisher() != null
                ? new SimpleIntegerProperty(f.getValue().getBook().getPublisher().getId()).asObject()
                : null);
        publisherIdCol.setEditable(false);
        return publisherIdCol;
    }

    private TableColumn<BookAuthorView, String> getPublisherNameColumn() {
        TableColumn<BookAuthorView, String> publisherNameCol = new TableColumn<>("Publisher");
        publisherNameCol.setCellValueFactory(f -> f.getValue().bookProperty().getValue().getPublisher() != null
                ? new SimpleStringProperty(f.getValue().getBook().getPublisher().getName())
                : null);
        publisherNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        publisherNameCol.setEditable(true);
        publisherNameCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    try {
                        // Check if the Publisher record with the entered name already exists in database, if so, assign
                        // it, otherwise, create and then assign
                        Publisher record = mc.getPublisherByName(event.getNewValue());
                        if (record == null) {
                            record = new Publisher();
                            record.setName(event.getNewValue());
                            mc.insertPublisher(record);
                        }
                        BookAuthorView bav = event.getRowValue();
                        bav.getBook().setPublisher(record);
                        mc.updateBook(bav.getBook());
                        event.getTableView().getItems().set(event.getTablePosition().getRow(), event.getRowValue());
                    } catch (Exception e) {
                        showPopupErrorWithExceptionStack(e, "Error while updating publisher name.");
                    }
                }
        );
        return publisherNameCol;
    }

    private TableColumn<BookAuthorView, Integer> getSeriesIdColumn() {
        TableColumn<BookAuthorView, Integer> seriesIdCol = new TableColumn<>("Series ID");
        seriesIdCol.setCellValueFactory(f -> f.getValue().getBook().getSeries() != null
                ? new SimpleIntegerProperty(f.getValue().getBook().getSeries().getId()).asObject()
                : null);
        return seriesIdCol;
    }

    private TableColumn<BookAuthorView, String> getSeriesNameColumn() {
        TableColumn<BookAuthorView, String> seriesNameCol = new TableColumn<>("Series");
        seriesNameCol.setCellValueFactory(f -> f.getValue().getBook().getSeries() != null
                ? new SimpleStringProperty(f.getValue().getBook().getSeries().getName())
                : null);
        seriesNameCol.setEditable(true);
        seriesNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        seriesNameCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    try {
                        Series series = mc.readSeriesByName(event.getNewValue());
                        if (series == null) {
                            series = new Series(event.getNewValue());
                            mc.insertSeries(series);
                        }
                        BookAuthorView bav = event.getRowValue();
                        bav.getBook().setSeries(series);
                        event.getTableView().getItems().set(event.getTablePosition().getRow(), bav);
                    } catch (Exception e) {
                        showPopupErrorWithExceptionStack(e);
                    }
                });
        return seriesNameCol;
    }

    private TableColumn<BookAuthorView, Double> getSeriesNumberColumn() {
        TableColumn<BookAuthorView, Double> seriesNumberCol = new TableColumn<>("Series #");
        seriesNumberCol.setCellValueFactory(f -> new SimpleDoubleProperty(f.getValue().getBook().getSeriesNumber()).asObject());
        seriesNumberCol.setCellFactory(TextFieldTableCell.forTableColumn(new CustomDoubleStringConverter()));
        seriesNumberCol.setEditable(true);
        seriesNumberCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, Double> event) -> {
                    try {
                        Book updatedBook = event.getRowValue().getBook();
                        updatedBook.setSeriesNumber(event.getNewValue());
                        mc.updateBook(updatedBook);
                        event.getRowValue().getBook().setSeriesNumber(event.getNewValue());
                    } catch (DAOException e) {
                        showPopupErrorWithExceptionStack(e);
                    }
                });
        return seriesNumberCol;
    }

    private TableColumn<BookAuthorView, String> getDatePublishedColumn() {
        TableColumn<BookAuthorView, String> datePublishedCol = new TableColumn<>("Date Published");

        Callback<TableColumn<BookAuthorView, String>, TableCell<BookAuthorView, String>> cellfactory
                = (TableColumn<BookAuthorView, String> param) -> new DatePickerCell();

        datePublishedCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getPublishDate().toString()));
        datePublishedCol.setEditable(true);
        datePublishedCol.setCellFactory(cellfactory);
        datePublishedCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    Book book = event.getRowValue().getBook();
                    try {
                        book.setPublishDate(Instant.parse(event.getNewValue()));
                        mc.updateBook(book);
                    } catch (Exception e) {
                        showPopupErrorWithExceptionStack(e);
                    }
                    BookAuthorView bav = event.getRowValue();
                    bav.setBook(book);
                    event.getTableView().getItems().set(event.getTablePosition().getRow(), bav);
                });
        return datePublishedCol;
    }

    private TableColumn<BookAuthorView, String> getDateAddedColumn() {
        TableColumn<BookAuthorView, String> dateAddedCol = new TableColumn<>("Date Added");

        Callback<TableColumn<BookAuthorView, String>, TableCell<BookAuthorView, String>> cellFactory
                = (TableColumn<BookAuthorView, String> param) -> new CustomDateCell();

        dateAddedCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getAddedDate().toString()));
        dateAddedCol.setCellFactory(cellFactory);
        return dateAddedCol;
    }

    private TableColumn<BookAuthorView, String> getDateModifiedColumn() {
        TableColumn<BookAuthorView, String> dateModifiedCol = new TableColumn<>("Date Modified");

        Callback<TableColumn<BookAuthorView, String>, TableCell<BookAuthorView, String>> cellFactory
                = (TableColumn<BookAuthorView, String> param) -> new CustomDateCell();

        dateModifiedCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getModifiedDate().toString()));
        dateModifiedCol.setCellFactory(cellFactory);
        return dateModifiedCol;
    }

    private TableColumn<BookAuthorView, String> getPathColumn() {
        TableColumn<BookAuthorView, String> pathCol = new TableColumn<>("Path");
        pathCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getPath()));
        return pathCol;
    }
}

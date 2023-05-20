package xyz.peasfultown.interfaces;

import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.RecordAlreadyExistsException;
import xyz.peasfultown.domain.Author;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

import static xyz.peasfultown.interfaces.GUIHelpers.*;

// TODO: handle book series edit
public class JebmanGUI extends Application {
    private static MainController mc;

    private final ObservableList<BookAuthorView> data = FXCollections.observableList(new ArrayList<>());

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


        idCol.setCellValueFactory(f -> new SimpleIntegerProperty(f.getValue().getBook().getId()).asObject());
        titleCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getTitle()));
        titleCol.setCellFactory(TextFieldTableCell.forTableColumn());
        titleCol.setEditable(true);
        titleCol.setOnEditCommit(
                (TableColumn.CellEditEvent<BookAuthorView, String> event) -> {
                    try {
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

        authorIdCol.setCellValueFactory(f -> new SimpleIntegerProperty(f.getValue().getAuthor().getId()).asObject());
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

        publisherIdCol.setCellValueFactory(f -> f.getValue().getBook().getPublisher() != null
                ? new SimpleIntegerProperty(f.getValue().getBook().getPublisher().getId()).asObject()
                : null);
        publisherIdCol.setEditable(false);

        publisherNameCol.setCellValueFactory(f -> f.getValue().bookProperty().getValue().getPublisher() != null
                ? new SimpleStringProperty(f.getValue().getBook().getPublisher().getName())
                : null);
        publisherNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
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

                        Book book = event.getRowValue().getBook();
                        book.setPublisher(record);

                        mc.updateBook(book);

                        Book bv = this.data.stream().filter(d -> d.getBook().getId() == event.getRowValue().getBook().getId()).findFirst().get().getBook();
                        bv.setPublisher(record);
                        event.getTableView().getItems().set(event.getTablePosition().getRow(), event.getRowValue());
                    } catch (Exception e) {
                        showPopupErrorWithExceptionStack(e, "Error while updating publisher name.");
                    }
                }
        );

        seriesIdCol.setCellValueFactory(f -> f.getValue().getBook().getSeries() != null
                ? new SimpleIntegerProperty(f.getValue().getBook().getSeries().getId()).asObject()
                : null);
        seriesNameCol.setCellValueFactory(f -> f.getValue().getBook().getSeries() != null
                ? new SimpleStringProperty(f.getValue().getBook().getSeries().getName())
                : null);
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
                }
        );

        datePublishedCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getPublishDate().toString()));
        dateAddedCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getAddedDate().toString()));
        dateModifiedCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getModifiedDate().toString()));

        pathCol.setCellValueFactory(f -> new SimpleStringProperty(f.getValue().getBook().getPath()));

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

}

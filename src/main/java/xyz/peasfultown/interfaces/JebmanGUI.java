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
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import xyz.peasfultown.ApplicationConfig;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.RecordAlreadyExistsException;
import xyz.peasfultown.domain.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.List;

import static xyz.peasfultown.interfaces.GUIHelpers.*;

// TODO: handle book series edit
public class JebmanGUI extends Application {
    private Stage stage;
    private final GridPane mainGrid = new GridPane();
    private final GridPane infoPanel = new GridPane();
    private final Font font = new Font("Arial", 20);
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

        GridPane layout = getLayout();
        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.show();
    }

    private GridPane getLayout() {
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        mainGrid.setPadding(new Insets(10, 10, 10, 10));
        mainGrid.setGridLinesVisible(true);

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setMaxWidth(350);
        col1.setPrefWidth(350);
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

    private GridPane getBookInfoPanel() {
        Image defaultCover = new Image(Objects.requireNonNull(JebmanGUI.class.getClassLoader().getResourceAsStream("nocover.png")));
        ImageView cover = new ImageView();
        cover.setCache(true);
        cover.setImage(defaultCover);
        cover.setPreserveRatio(true);
        cover.setFitWidth(300);

        Label isbnLbl = new Label("ISBN:");
        GridPane.setHalignment(isbnLbl, HPos.LEFT);
        GridPane.setValignment(isbnLbl, VPos.TOP);
        isbnLbl.setFont(font);
        Label isbnTxt = new Label();
        isbnTxt.setFont(font);
        isbnTxt.setWrapText(true);

        Label titleLbl = new Label("Title:");
        GridPane.setHalignment(titleLbl, HPos.LEFT);
        GridPane.setValignment(titleLbl, VPos.TOP);
        titleLbl.setFont(font);
        Label titleTxt = new Label();
        titleTxt.setWrapText(true);
        titleTxt.setFont(font);

        Label publisherLbl = new Label("Publisher:");
        GridPane.setHalignment(publisherLbl, HPos.LEFT);
        GridPane.setValignment(publisherLbl, VPos.TOP);
        publisherLbl.setFont(font);
        Label publisherTxt = new Label();
        publisherTxt.setWrapText(true);
        publisherTxt.setFont(font);

        Label publishDateLbl = new Label("Publish Date:");
        GridPane.setHalignment(publishDateLbl, HPos.LEFT);
        GridPane.setValignment(publishDateLbl, VPos.TOP);
        publishDateLbl.setFont(font);
        Label publishDateTxt = new Label();
        publishDateTxt.setWrapText(true);
        publishDateTxt.setFont(font);

        Label authorLbl = new Label("Author(s):");
        GridPane.setHalignment(authorLbl, HPos.LEFT);
        GridPane.setValignment(authorLbl, VPos.TOP);
        authorLbl.setFont(font);
        Label authorTxt = new Label();
        authorTxt.setWrapText(true);
        authorTxt.setFont(font);

        Label tagsLbl = new Label("Tag(s):");
        GridPane.setHalignment(tagsLbl, HPos.LEFT);
        GridPane.setValignment(tagsLbl, VPos.TOP);
        tagsLbl.setFont(font);

        FlowPane tagsFlowPane = new FlowPane();

        ColumnConstraints col1 = new ColumnConstraints();
        ColumnConstraints col2 = new ColumnConstraints();
        col1.setHgrow(Priority.SOMETIMES);
        col2.setHgrow(Priority.ALWAYS);
        infoPanel.getColumnConstraints().addAll(col1, col2);

        infoPanel.add(cover, 0, 0, 2, 1);
        infoPanel.add(isbnLbl, 0, 1);
        infoPanel.add(isbnTxt, 1, 1);
        infoPanel.add(titleLbl, 0, 2);
        infoPanel.add(titleTxt, 1, 2);
        infoPanel.add(publisherLbl, 0, 3);
        infoPanel.add(publisherTxt, 1, 3);
        infoPanel.add(publishDateLbl, 0, 4);
        infoPanel.add(publishDateTxt, 1, 4);
        infoPanel.add(authorLbl, 0, 5);
        infoPanel.add(authorTxt, 1, 5);
        infoPanel.add(tagsLbl, 0, 6);
        infoPanel.add(tagsFlowPane, 1, 6);

        infoPanel.setPadding(new Insets(5));
        infoPanel.setGridLinesVisible(true);

        VBox.setVgrow(infoPanel, Priority.NEVER);
        HBox.setHgrow(infoPanel, Priority.NEVER);

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
        table.getFocusModel().focus(1);

        table.getFocusModel().focusedItemProperty().addListener((observableValue, bookAuthorViewOld, bookAuthorViewNew) -> {
//            System.out.println("focus changed to: " + table.getFocusModel().getFocusedCell());
//            System.out.println("observable value null: " + (observableValue.getValue() == null));
//            System.out.println("bookAuthorViewNew is null: " + ((bookAuthorViewNew) == null));
            boolean observableValueIsNull = observableValue.getValue() == null;
//            boolean observableValueIsNull = false;

            Image cover = !observableValueIsNull
                    ? new Image(ApplicationConfig.MAIN_PATH.resolve(observableValue.getValue().getBook().getPath()).resolve("cover.png").toUri().toString())
                    : new Image(Objects.requireNonNull(JebmanGUI.class.getClassLoader().getResourceAsStream("nocover.png")));
            ((ImageView) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 0, 0))).setImage(cover);

            if (!observableValueIsNull) {
                ((Label) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 1))).setText(observableValue.getValue().getBook().getIsbn());
                ((Label) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 2))).setText(observableValue.getValue().getBook().getTitle());
                ((Label) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 3))).setText((observableValue.getValue().getBook().getPublisher() != null
                        ? observableValue.getValue().getBook().getPublisher().getName() : "Unknown"));
                ((Label) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 4))).setText(observableValue.getValue().getBook().getPublishDate().toString());
                ((Label) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 5))).setText(observableValue.getValue().getAuthor().getName());
//                StringJoiner tagsJoiner = new StringJoiner(", ");
//                for (Tag t : observableValue.getValue().getTags()) {
//                    tagsJoiner.add(t.getName());
//                }
//                // TODO: make each tags clickable
//                ((Label) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 6))).setText(tagsJoiner.toString());
                ((FlowPane) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 6))).getChildren().clear();
                List<Tag> tagList = observableValue.getValue().getTags();
                if (tagList.size() > 0) {
                    for (int i = 0; i < tagList.size(); i++) {
                        Label lbl = new Label(tagList.get(i).getName());
                        EventHandler<MouseEvent> hoverHandler = new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                // TODO: FINISH
                                System.out.println("Mouse entered tag: " + lbl.getText());
                                lbl.setTextFill(Color.web("#5fccff"));
                                stage.getScene().setCursor(Cursor.CLOSED_HAND);
                            }
                        };

                        EventHandler<MouseEvent> exitHandler = new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                lbl.setTextFill(Color.web("#4daaff"));
                                stage.getScene().setCursor(Cursor.DEFAULT);
                            }
                        };

                        EventHandler<MouseEvent> clickHandler = new EventHandler<MouseEvent>() {
                            @Override
                            public void handle(MouseEvent mouseEvent) {
                                if (mouseEvent.getButton().name().equals("PRIMARY")) {
                                    System.out.println("Clicked on label: " + lbl.getText());
                                }
                            }
                        };

                        lbl.setFont(font);
                        lbl.setOnMouseEntered(hoverHandler);
                        lbl.setOnMouseExited(exitHandler);
                        lbl.setTextFill(Color.web("#4daaff"));
                        lbl.setOnMouseClicked(clickHandler);
                        ((FlowPane) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 6))).getChildren().add(lbl);
                        if (i != (tagList.size() - 1))
                            ((FlowPane) Objects.requireNonNull(this.getTableNodeByColAndRow(infoPanel, 1, 6))).getChildren().add(new Text(", "));
                    }
                }
            }
        });

        return table;
    }

    private Node getTableNodeByColAndRow(GridPane gridPane, int colInd, int rowInd) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == colInd && GridPane.getRowIndex(node) == rowInd) {
                return node;
            }
        }
        return null;
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
        try {
            List<Tag> tags = new ArrayList<>(mc.getTagsOfBook(book));
            return new BookAuthorView(book, author, tags);
        } catch (DAOException e) {
            showPopupErrorWithExceptionStack(e);
        }
        return null;
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
                            book = event.getRowValue().getBook();
                            book.setTitle(event.getNewValue());
                            mc.updateBook(book);
                        } else {
                            BookAuthorView bav = event.getRowValue();
                            bav.getBook().setTitle(event.getOldValue());
                            event.getTableView().getItems().set(event.getTablePosition().getRow(), bav);
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

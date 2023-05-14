package xyz.peasfultown.interfaces;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import xyz.peasfultown.MainController;
import xyz.peasfultown.domain.*;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class JebmanGUI extends Application {
    private static MainController mc;
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
        col1.setPercentWidth(20);
        col2.setPercentWidth(80);
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
        final VBox panel = new VBox();
        // TODO: add info pane
        Label placeHolder = new Label("Info goes here");
        panel.getChildren().addAll(placeHolder);
        panel.setPadding(new Insets(10, 10, 10, 10));

        return panel;
    }

    private TableView<BookAuthorView> getBookTable() {
        TableView<BookAuthorView> table = new TableView<BookAuthorView>();
        table.setEditable(true);
        TableColumn<BookAuthorView, Integer> idCol = new TableColumn<>("ID");
        TableColumn<BookAuthorView, String> titleCol = new TableColumn<>("Title");
        TableColumn<BookAuthorView, String> publisherCol = new TableColumn<>("Publisher");
        TableColumn<BookAuthorView, String> authorCol = new TableColumn<>("Author");
        ObservableSet<BookAuthorView> bav = collectBookAuthorViewItems();
        ObservableList<BookAuthorView> bal = FXCollections.observableList(new ArrayList<>(bav));

        idCol.setCellValueFactory(f -> f.getValue().bookProperty().getValue().idProperty().asObject());
        titleCol.setCellValueFactory(f -> f.getValue().bookProperty().getValue().titleProperty());
        publisherCol.setCellValueFactory(f -> f.getValue()
                .bookProperty().getValue().getPublisher() != null
                ? f.getValue().bookProperty().getValue().publisherProperty().getValue().nameProperty()
                : null);
        authorCol.setCellValueFactory(f -> f.getValue().authorProperty().getValue().nameProperty());

        table.getColumns().addAll(idCol, titleCol, publisherCol, authorCol);
        table.setItems(bal);

        return table;
    }

    private HBox getTopBar() {
        HBox hbox = new HBox();
        hbox.setPadding(new Insets(15, 12, 15, 12));
        hbox.setSpacing(10);
        hbox.setStyle("-fx-background-color: #336699");
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setPrefHeight(100);

        Button btnAddBook = new Button("Add Book");
        btnAddBook.setPrefSize(150, 50);
        hbox.getChildren().add(btnAddBook);

        return hbox;
    }

    private ObservableSet<BookAuthorView> collectBookAuthorViewItems() {
        Set<Book> books = mc.getBooks();
        Set<BookAuthor> bookAuthorLink = mc.getBookAuthorLinks();
        ObservableSet<BookAuthorView> bookAuthorViewItems = FXCollections.observableSet();
        ObservableSet<AuthorView> authorViewItems = collectAuthorViewItems();
        ObservableSet<PublisherView> publisherViewItems = collectPublisherViewItems();
        ObservableSet<SeriesView> seriesViewItems = collectSeriesViewItems();

        for (Book b : books) {
            SeriesView sv = null;
            PublisherView pv = null;
            AuthorView av = null;

            for (BookAuthor ba : bookAuthorLink) {
                if (ba.getBookId() == b.getId()) {
                    av = authorViewItems.stream()
                            .filter(avi -> avi.getId() == ba.getAuthorId())
                            .findFirst().get();
                    break;
                }
            }

            if (b.getSeries() != null) {
                sv = seriesViewItems.stream().filter(s -> s.getId() == b.getSeries().getId()).findAny().get();
            }

            if (b.getPublisher() != null) {
                pv = publisherViewItems.stream().filter(p -> p.getId() == b.getPublisher().getId()).findAny().get();
            }

            BookView bv  = new BookView(b.getId(), b.getIsbn(), b.getUuid(), b.getTitle(), sv, b.getSeriesNumber(),
                    pv, b.getPublishDate().toString(), b.getAddedDate().toString(), b.getModifiedDate().toString());

            BookAuthorView bav = new BookAuthorView(bv, av);
            bookAuthorViewItems.add(bav);
        }
        return bookAuthorViewItems;
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
}

package xyz.peasfultown.interfaces;

import javafx.scene.control.TableCell;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class CustomDateCell extends TableCell<BookAuthorView, String> {
    public CustomDateCell() {
        super();
    }

    @Override
    public void startEdit() {
        super.startEdit();
    }

    @Override
    protected void updateItem(String s, boolean empty) {
        super.updateItem(s, empty);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneOffset.UTC);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
            } else {
                setText(dtf.format(Instant.parse(s)));
                setGraphic(null);
            }
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
    }
}

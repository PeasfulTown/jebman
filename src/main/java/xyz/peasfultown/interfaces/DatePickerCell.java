package xyz.peasfultown.interfaces;

import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;

public class DatePickerCell extends TableCell<BookAuthorView, String> {
    private DatePicker datePicker;
    public DatePickerCell() {
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createDatePicker();
            setText(null);
            setGraphic(datePicker);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(LocalDate.ofInstant(getDate(), ZoneOffset.UTC).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (datePicker != null) {
                    datePicker.setValue(LocalDate.ofInstant(getDate(), ZoneId.systemDefault()));
                }
                setText(null);
                setGraphic(datePicker);
            } else {
                setText(LocalDate.ofInstant(getDate(), ZoneOffset.UTC).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)));
                setGraphic(null);
            }
        }
    }

    private void createDatePicker() {
        datePicker = new DatePicker(LocalDate.ofInstant(getDate(), ZoneId.systemDefault()));
        datePicker.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        datePicker.setOnAction((e) -> {
            System.out.println("Committed: " + datePicker.getValue().toString());
            commitEdit(datePicker.getValue().atStartOfDay().toInstant(ZoneOffset.UTC).toString());
        });
    }

    private Instant getDate() {
        return getItem() == null ? Instant.now() : Instant.parse(getItem());
    }
}

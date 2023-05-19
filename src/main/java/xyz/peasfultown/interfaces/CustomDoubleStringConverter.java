package xyz.peasfultown.interfaces;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.converter.DoubleStringConverter;

import java.io.PrintWriter;
import java.io.StringWriter;

public class CustomDoubleStringConverter extends DoubleStringConverter {
    private final DoubleStringConverter converter = new DoubleStringConverter();

    @Override
    public String toString(Double aDouble) {
        try {
            return converter.toString(aDouble);
        } catch (NumberFormatException e) {
            showAlert(e);
        }
        return null;
    }

    @Override
    public Double fromString(String s) {
        try {
            return converter.fromString(s);
        } catch (NumberFormatException e) {
            showAlert(e);
        }
        return 1.0;
    }

    private void showAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Exception occurred");
        alert.setHeaderText("Exception occurred while converting String into Double");
        alert.setContentText(e.getMessage());
        alert.setResizable(true);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
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

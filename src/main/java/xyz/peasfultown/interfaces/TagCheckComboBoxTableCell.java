package xyz.peasfultown.interfaces;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableCell;
import org.controlsfx.control.CheckComboBox;
import xyz.peasfultown.MainController;
import xyz.peasfultown.domain.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class TagCheckComboBoxTableCell extends TableCell<BookAuthorView, Set<Tag>> {
    private CheckComboBox<String> tagSelection;
    private MainController mc;
    private final Set<Tag> allTags;
    public TagCheckComboBoxTableCell(MainController mc) {
        this.mc = mc;
        this.allTags = mc.getTags();
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createCheckComboBox();
            setText(null);
            setGraphic(this.tagSelection);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getTagsAsCommaDelimitedString(getItem()));
        setGraphic(null);
    }

    @Override
    protected void updateItem(Set<Tag> tags, boolean empty) {
        super.updateItem(tags, empty);

         if (empty) {
             setText(null);
             setGraphic(null);
         } else {
             if (isEditing()) {
                 setText(null);
                 setGraphic(tagSelection);
             } else {
                 setText(getTagsAsCommaDelimitedString(getItem()));
                 setGraphic(null);
             }
         }
    }

    private void createCheckComboBox() {
        this.tagSelection = new CheckComboBox<>();
        this.tagSelection.getItems().addAll(getTagsAsListOfStrings(this.allTags));
        for (String item : tagSelection.getItems()) {
            if (getTagsAsListOfStrings(getItem()).contains(item)) {
                tagSelection.getCheckModel().check(item);
            }
        }
        this.tagSelection.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                while(c.next()) {
                    // do something with changes here
                    System.out.println("while changed: " + c);
                }
                System.out.println("tag selector on changed after while: " + tagSelection.getCheckModel().getCheckedItems());
            }
        });
    }

    private String getTagsAsCommaDelimitedString(Set<Tag> tags) {
        List<String> strings = getTagsAsListOfStrings(tags);
        StringJoiner sj = new StringJoiner(",");
        for (String s : strings) {
            sj.add(s);
        }
        return sj.toString();
    }

    private List<String> getTagsAsListOfStrings(Set<Tag> tags) {
        List<String> tagStr = new ArrayList<>();
        for (Tag t : tags) {
            tagStr.add(t.getName());
        }
        return tagStr;
    }
}

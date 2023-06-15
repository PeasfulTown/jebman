package xyz.peasfultown.interfaces;

import java.util.Set;
import java.util.StringJoiner;

import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import xyz.peasfultown.domain.Tag;

public class TagPickerCell extends TableCell<BookAuthorView, Set<Tag>> {
    private ComboBox<CheckBox> combobox;
    private Set<Tag> allTags;
    public TagPickerCell(Set<Tag> allTags) {
        this.allTags = allTags;
    }

    @Override
    public void startEdit() {
        super.startEdit();
        createComboBox();
        setText(null);
        setGraphic(combobox);
    }

    @Override
    public void commitEdit(Set<Tag> tags) {
        super.commitEdit(tags);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getTagsAsString());
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
                if (combobox != null) {
                }
                setText(null);
                setGraphic(this.combobox);
            } else {
                setText(getTagsAsString());
                setGraphic(null);
            }
        }
    }

    private void createComboBox() {
        this.combobox = new ComboBox<>();
        this.combobox.setEditable(true);
        this.combobox.setPromptText("New Tag");
        // TODO: add new tag based on combo text field input,
        // show Alert popup if tag already exists, do not allow
        // same tag names whether they be upper-case/lower-case,
        // insert new Tag CheckBox object into combobox upon commit
        for (Tag t : allTags) {
            CheckBox cb = new CheckBox(t.getName());
            if (bookHasTag(t))
                cb.setSelected(true);
            this.combobox.getItems().add(cb);
        }
    }

    private boolean bookHasTag(Tag t) {
        for (Tag bt : getItem()) {
            if (bt.equals(t))
                return true;
        }

        return false;
    }

    private String getTagsAsString() {
        StringJoiner sj = new StringJoiner(", ");

        for (Tag t : getItem()) {
            sj.add(t.getName());
        }

        return sj.toString();
    }
}

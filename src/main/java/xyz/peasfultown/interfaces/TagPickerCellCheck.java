package xyz.peasfultown.interfaces;

import java.util.Set;
import java.util.StringJoiner;

import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Tag;

public class TagPickerCellCheck extends TableCell<BookAuthorView, Set<Tag>> {
    private ComboBox<CheckBox> combobox;
    private MainController mc;
    private Set<Tag> allTags;

    public TagPickerCellCheck(MainController mc) {
        this.mc = mc;
        this.allTags = mc.getTags();
    }

    @Override
    public void startEdit() {
        super.startEdit();
        System.out.println("Started editing book: " + getTableRow().getItem().getBook().getTitle());
        StringJoiner sj = new StringJoiner(",");
        for (Tag t : getItem()) {
            sj.add(t.getName());
        }
        System.out.println("Current book tags: " + sj.toString());
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
        this.combobox.setMaxWidth(this.widthProperty().get());
        this.combobox.setEditable(true);
        this.combobox.setPromptText("New Tag");
        // TODO: add new tag based on combo text field input,
        // show Alert popup if tag already exists, do not allow
        // same tag names whether they be upper-case/lower-case,
        // insert new Tag CheckBox object into ComboBox upon commit
        for (Tag t : allTags) {
            CheckBox cb = new CheckBox(t.getName());
            if (bookHasTag(t))
                cb.setSelected(true);
            this.combobox.getItems().add(cb);
            System.out.println("Created " + cb.getText() + " checkbox");
        }

        // When new tag is added via ComboBox's text field, add it to ComboBox
        // (as a *checked* CheckBox object) and database.
        this.combobox.setOnAction((ActionEvent e) -> {
            if (this.combobox.getValue() != null && !((Object) this.combobox.getSelectionModel().getSelectedItem()).toString().isEmpty()) {
                String comboBoxTextInput = ((Object) this.combobox.getSelectionModel().getSelectedItem()).toString();
                System.out.println("New tag input from ComboBox text field: " + comboBoxTextInput);
                System.out.println("New tag already exists in db (true/false): " + tagListAlreadyContainsValue(comboBoxTextInput));
                if (!tagListAlreadyContainsValue(comboBoxTextInput)) {
                    Tag newTag = new Tag(comboBoxTextInput);
                    CheckBox newTagCheckBox = new CheckBox(newTag.getName());
                    newTagCheckBox.setSelected(true);
                    try {
                        mc.insertTag(newTag);
                        System.out.println("new tag object: " + newTag);
                        mc.tagBook(this.getTableRow().getItem().getBook().getId(), newTag.getId());
                        this.getTableRow().getItem().getTags().add(newTag);
                    } catch (DAOException ex) {
                        System.out.println("DAO Exception encountered when trying to create new Tag record");
                    }
                    this.combobox.getItems().add(newTagCheckBox);
                    this.getItem().add(newTag);
                }
                this.combobox.setValue(null);
                this.combobox.cancelEdit();
                commitEdit(this.getItem());
            } else if (this.combobox.getSelectionModel().getSelectedItem() != null) {
                System.out.println("clicked checkbox item: " + this.combobox.getSelectionModel().getSelectedItem());
            }
        });

    }

    private boolean bookHasTag(Tag t) {
        for (Tag bt : getItem()) {
            if (bt.equals(t))
                return true;
        }

        return false;
    }

    private CheckBox getTagAsCheckBox(Tag tag) {
        return new CheckBox(tag.getName());
    }

    private String getTagsAsString() {
        StringJoiner sj = new StringJoiner(", ");

        for (Tag t : getItem()) {
            sj.add(t.getName());
        }

        return sj.toString();
    }

    private boolean tagListAlreadyContainsValue(String newTag) {
        for (Tag t : allTags) {
            if (t.getName().equalsIgnoreCase(newTag))
                return true;
        }
        return false;
    }
}

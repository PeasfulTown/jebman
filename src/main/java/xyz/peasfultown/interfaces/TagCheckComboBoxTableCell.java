package xyz.peasfultown.interfaces;

import javafx.collections.ListChangeListener;
import javafx.scene.control.TableCell;
import org.controlsfx.control.CheckComboBox;
import xyz.peasfultown.MainController;
import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Tag;

import javax.swing.event.ChangeListener;
import java.util.*;

public class TagCheckComboBoxTableCell extends TableCell<BookAuthorView, Set<Tag>> {
    private CheckComboBox<String> tagSelector;
    private MainController mc;
    private final Set<Tag> selectedTags;
    private final Set<Tag> allTags;

    public TagCheckComboBoxTableCell(MainController mc) {
        this.mc = mc;
        this.selectedTags = new LinkedHashSet<>();
        this.allTags = mc.getTags();

    }

    @Override
    public void startEdit() {
        super.startEdit();
        setFocused(false);
        createCheckComboBox();
        setText(null);
        setGraphic(this.tagSelector);
    }

    @Override
    public void cancelEdit() {
        setText(getTagsAsCommaDelimitedString(selectedTags));
        setGraphic(null);
    }

    @Override
    public void commitEdit(Set<Tag> tags) {
        super.commitEdit(tags);
        setFocused(false);
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
                setGraphic(tagSelector);
            } else {
                fetchSelectedTags();
                setText(getTagsAsCommaDelimitedString(selectedTags));
                setGraphic(null);
            }
        }
    }

    private void createCheckComboBox() {
        this.tagSelector = new CheckComboBox<>();
        this.tagSelector.getItems().addAll(getTagsAsListOfStrings(this.allTags));
        for (String item : tagSelector.getItems()) {
            if (getTagsAsListOfStrings(selectedTags).contains(item)) {
                tagSelector.getCheckModel().check(item);
            }
        }

        this.tagSelector.getCheckModel().getCheckedItems().addListener(new ListChangeListener<String>() {
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                while (c.next()) {
                    // do something with changes here
//                    System.out.println("while: changed: " + c);
                    if (c.wasAdded()) {
                        try {
                            System.out.println("tag " + c.getAddedSubList().get(0) + " added");
                            mc.tagBook(getTableRow().getItem().getBook().getId(), getTagByName(c.getAddedSubList().get(0), (AbstractCollection<Tag>) allTags).getId());
                            selectedTags.add(getTagByName(c.getAddedSubList().get(0), (AbstractCollection<Tag>) allTags));
                        } catch (DAOException e) {
                            System.err.println("Exception while tagging book: " + e.getMessage());
                        }
                    } else {
                        try {
                            System.out.println("tag " + c.getRemoved().get(0) + " removed");
                            mc.untagBook(getTableRow().getItem().getBook().getId(), getTagByName(c.getRemoved().get(0), (AbstractCollection<Tag>) allTags).getId());
                        } catch (DAOException e) {
                            System.err.println("Exception while tagging book: " + e.getMessage());
                        }
                    }
                }
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

    private Tag getTagByName(String tagName, AbstractCollection<Tag> source) {
        for (Tag t : source) {
            if (t.getName().equalsIgnoreCase(tagName)) {
                return t;
            }
        }
        return null;
    }

    private void fetchSelectedTags() {
        if (getItem() != null) {
            this.selectedTags.clear();
            this.selectedTags.addAll(getItem());
            System.out.println("selected tags assigned");
        }
    }
}

package xyz.peasfultown.domain;

public class BookTag implements Record {
    private int id;
    private int bookId;
    private int tagId;

    public BookTag() {
    }

    public BookTag(int bookId, int tagId) {
        this.bookId = bookId;
        this.tagId = tagId;
    }

    public BookTag(int id, int bookId, int tagId) {
        this.id = id;
        this.bookId = bookId;
        this.tagId = tagId;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBookId() {
        return bookId;
    }

    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    public int getTagId() {
        return tagId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }
}

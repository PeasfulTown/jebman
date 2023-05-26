package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.BookTag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JDBCBookTagDAO extends JDBCAbstractDAO<BookTag> {
    @Override
    protected void assignObjectId(BookTag object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getLastInsertedRowQuery() {
        return "SELECT * FROM books_tags_link WHERE id=(SELECT MAX(id) FROM books_tags_link);";
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO books_tags_link (book_id, tag_id) VALUES (?,?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE books_tags_link SET book_id=?, tag_id=? WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT * FROM books_tags_link;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT * FROM books_tags_link WHERE id=?;";
    }

    @Override
    protected String getReadByNameQuery() {
        return null;
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM books_tags_link WHERE id=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(id) FROM books_tags_link AS count;";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, BookTag object) throws DAOException {
        try {
            stmt.setInt(1, object.getBookId());
            stmt.setInt(2, object.getTagId());
            if (object.getId() != 0)
                stmt.setInt(3, object.getId());
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected BookTag getObjectFromResultSet(ResultSet rs) throws DAOException {
        BookTag link = new BookTag();
        try {
            link.setId(rs.getInt("id"));
            link.setBookId(rs.getInt("book_id"));
            link.setTagId(rs.getInt("tag_id"));
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        return null;
    }
}

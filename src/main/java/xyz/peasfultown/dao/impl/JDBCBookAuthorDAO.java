package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.BookAuthor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCBookAuthorDAO extends JDBCAbstractDAO<BookAuthor> {
    @Override
    protected void assignObjectId(BookAuthor object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO books_authors_link (book_id, author_id) VALUES (?,?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE books_authors_link SET book_id=?, author_id=? WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT * FROM books_authors_link;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT * FROM books_authors_link WHERE id=?;";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM books_authors_link WHERE id=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(*) FROM books_authors_link AS count;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, BookAuthor object) throws DAOException {
        try {
            stmt.setInt(1, object.getBookId());
            stmt.setInt(2, object.getAuthorId());
            if (object.getId() > 0)
                stmt.setInt(3, object.getId());
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected BookAuthor getObjectFromResultSet(ResultSet rs) throws DAOException {
        BookAuthor link = new BookAuthor();
        try {
            link.setId(rs.getInt("id"));
            link.setBookId(rs.getInt("book_id"));
            link.setAuthorId(rs.getInt("author_id"));
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return link;
    }

    @Override
    public BookAuthor read(String str) throws DAOException {
        return null;
    }
}

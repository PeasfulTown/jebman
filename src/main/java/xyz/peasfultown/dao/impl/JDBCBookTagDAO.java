package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.GenericJointTableDAO;
import xyz.peasfultown.domain.BookTag;
import xyz.peasfultown.helpers.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class JDBCBookTagDAO extends JDBCAbstractDAO<BookTag> implements GenericJointTableDAO {
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

    protected String getReadBookIdsByTagIdQuery() {
        return "SELECT * FROM books_tags_link WHERE tag_id=?;";
    }

    protected String getReadTagIdsByBookIdQuery() {
        return "SELECT * FROM books_tags_link WHERE book_id=?;";
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
        return link;
    }

    public Set<Integer> readBookIdsByTagId(int tagId) throws DAOException {
        String readQuery = getReadBookIdsByTagIdQuery();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(readQuery)) {
            setStatementId(stmt, tagId);
            try (ResultSet rs = stmt.executeQuery()) {
                Set<Integer> mainObjIds = new LinkedHashSet<>();
                while (rs.next()) {
                    mainObjIds.add(rs.getInt("book_id"));
                }
                return mainObjIds;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public Set<Integer> readTagIdsByBookId(int bookId) throws DAOException {
        String readQuery = getReadTagIdsByBookIdQuery();

        try (Connection con = ConnectionFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(readQuery)) {
            stmt.setInt(1, bookId);
            try (ResultSet rs = stmt.executeQuery()) {
                Set<Integer> ids = new LinkedHashSet<>();
                while (rs.next()) {
                    ids.add(rs.getInt("tag_id"));
                }
                return ids;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public Set<Integer> readFirstColIdsBySecondColIds(int id) throws DAOException {
        return readBookIdsByTagId(id);
    }

    public Set<Integer> readSecondColIdsByFirstColIds(int id) throws DAOException {
        return readTagIdsByBookId(id);
    }
}

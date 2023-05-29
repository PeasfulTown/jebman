package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Tag;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JDBCTagDAO extends JDBCAbstractDAO<Tag>{

    @Override
    protected void assignObjectId(Tag object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getLastInsertedRowQuery() {
        return "SELECT * FROM tags WHERE id=(SELECT MAX(id) FROM tags);";
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO tags (name) VALUES (?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE tags SET name=? WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT * FROM tags;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT * FROM tags WHERE id=?;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "SELECT * FROM tags WHERE name=?;";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM tags WHERE id=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(*) FROM tags AS count;";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, Tag object) throws DAOException {
        try {
            if (object.getId() > 0) {
                stmt.setInt(2, object.getId());
            }
            stmt.setString(1, object.getName());
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected Tag getObjectFromResultSet(ResultSet rs) throws DAOException {
        Tag tag = new Tag();
        try {
            tag.setId(rs.getInt("id"));
            tag.setName(rs.getString("name"));
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        return tag;
    }
}

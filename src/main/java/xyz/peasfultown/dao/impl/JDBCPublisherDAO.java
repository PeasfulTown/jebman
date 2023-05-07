package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Publisher;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCPublisherDAO extends JDBCAbstractDAO<Publisher> {

    @Override
    protected void assignObjectId(Publisher object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO publishers (name) VALUES (?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE publishers SET name=? WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT * FROM publishers;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT * FROM publishers WHERE id=?;";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM publishers WHERE id=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(*) FROM publishers AS count;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "SELECT * FROM publishers WHERE name=?;";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, Publisher object) throws DAOException {
        try {
            stmt.setString(1, object.getName());
            if (object.getId() > 0) {
                stmt.setInt(2, object.getId());
            }
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected Publisher getObjectFromResultSet(ResultSet rs) throws DAOException {
        Publisher publisher = new Publisher();
        try {
            publisher.setId(rs.getInt("id"));
            publisher.setName(rs.getString("name"));
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }

        return publisher;
    }
}

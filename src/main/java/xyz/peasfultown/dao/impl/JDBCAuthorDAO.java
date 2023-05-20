package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Author;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCAuthorDAO extends JDBCAbstractDAO<Author> {
    @Override
    protected void assignObjectId(Author object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getLastInsertedRowQuery() {
        return "SELECT * FROM authors WHERE id=(SELECT MAX(id) FROM authors);";
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO authors (name) VALUES (?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE authors SET name=? WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT * FROM authors;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT * FROM authors WHERE id=?;";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM authors WHERE id=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(*) FROM authors AS count;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "SELECT * FROM authors WHERE name=?;";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, Author object) throws DAOException {
        try {
            stmt.setString(1, object.getName());
            if (object.getId() > 0)
                stmt.setInt(2, object.getId());
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected Author getObjectFromResultSet(ResultSet rs) throws DAOException {
        Author author = new Author();
        try {
            author.setId(rs.getInt("id"));
            author.setName(rs.getString("name"));
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        return author;
    }
}

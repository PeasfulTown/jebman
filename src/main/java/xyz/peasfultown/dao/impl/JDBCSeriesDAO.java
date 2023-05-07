package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Series;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JDBCSeriesDAO extends JDBCAbstractDAO<Series> {
    @Override
    protected void assignObjectId(Series object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO series (name) VALUES (?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE series SET name=? WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT * FROM series;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT * FROM series WHERE id=?;";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM series WHERE id=?;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "SELECT * FROM series WHERE name=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(*) FROM series AS count;";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, Series object) throws DAOException {
        try {
            stmt.setString(1, object.getName());
            if (object.getId() > 0)
                stmt.setInt(2, object.getId());
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected Series getObjectFromResultSet(ResultSet rs) throws DAOException {
        Series series = null;
        try {
            series = new Series(rs.getInt("id"), rs.getString("name"));
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
        return series;
    }
}

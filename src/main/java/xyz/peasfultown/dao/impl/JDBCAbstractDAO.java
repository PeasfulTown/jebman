package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.dao.GenericDAO;
import xyz.peasfultown.helpers.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public abstract class JDBCAbstractDAO<P> implements GenericDAO<P> {
    private static final int EXECUTE_UPDATE_SUCCESS = 1;
    private static final String SQL_QUERY_LAST_INSERT_ID = "SELECT last_insert_rowid() as id;";

    protected String getLastInsertIdQuery() {
        return SQL_QUERY_LAST_INSERT_ID;
    }

    protected abstract void assignObjectId(P object, Integer id);

    protected abstract String getCreateQuery();

    protected abstract String getUpdateQuery();

    protected abstract String getReadAllQuery();

    protected abstract String getReadByIdQuery();
    protected abstract String getReadByNameQuery();

    protected abstract String getDeleteQuery();

    protected abstract String getCountRowsQuery();

    protected abstract void setStatementObject(PreparedStatement stmt, P object) throws DAOException;

    protected void setStatementString(PreparedStatement stmt, String str) throws DAOException {
        try {
            stmt.setString(1, str);
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    protected abstract P getObjectFromResultSet(ResultSet rs) throws DAOException;


    public void setStatementId(PreparedStatement stmt, int id) throws DAOException {
        try {
            stmt.setInt(1, id);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void create(P object) throws DAOException {
        String createQuery = this.getCreateQuery();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(createQuery)) {
            setStatementObject(stmt, object);
            if (stmt.executeUpdate() < EXECUTE_UPDATE_SUCCESS) {
                throw new DAOException("Failed to create object.");
            }
            setNewId(con, object);
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public List<P> readAll() throws DAOException {
        String readAllQuery = this.getReadAllQuery();

        try (Connection con = ConnectionFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(readAllQuery)) {
            try (ResultSet rs = stmt.executeQuery()) {
                List<P> objects = new LinkedList<>();
                while (rs.next()) {
                    getObjectFromResultSet(rs);
                }
                return objects;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public P read(int id) throws DAOException {
        String readQuery = this.getReadByIdQuery();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(readQuery)) {
            setStatementId(stmt, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return getObjectFromResultSet(rs);
                else
                    return null;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public P read(String str) throws DAOException {
        String readQuery = this.getReadByNameQuery();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(readQuery)) {
            setStatementString(stmt, str);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return getObjectFromResultSet(rs);
                else
                    return null;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void update(P object) throws DAOException {
        String updateQuery = getUpdateQuery();
        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(updateQuery)) {

            setStatementObject(stmt, object);

            if (stmt.executeUpdate() < EXECUTE_UPDATE_SUCCESS)
                throw new DAOException("Failed to update record.");
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    public void delete(int id) throws DAOException {
        String deleteQuery = getDeleteQuery();

        try (Connection con = ConnectionFactory.getConnection();
             PreparedStatement stmt = con.prepareStatement(deleteQuery)) {
            setStatementId(stmt, id);
            if (stmt.executeUpdate() < EXECUTE_UPDATE_SUCCESS) {
                throw new DAOException("Failed to delete record.");
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public void setNewId(Connection con, P object) throws DAOException {
        String lastIdQuery = getLastInsertIdQuery();
        try (PreparedStatement stmt = con.prepareStatement(lastIdQuery);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
                assignObjectId(object, rs.getInt("id"));
            else
                throw new DAOException("Failed to assign new record ID");
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    public int count() throws DAOException {
        String countQuery = this.getCountRowsQuery();
        try (Connection con = ConnectionFactory.getConnection();
            PreparedStatement stmt = con.prepareStatement(countQuery)) {
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt("count");
                return 0;
            }
        } catch (Exception e) {
            throw new DAOException(e.getMessage(), e);
        }
    }
}

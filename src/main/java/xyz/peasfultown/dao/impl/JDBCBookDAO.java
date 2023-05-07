package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;
import xyz.peasfultown.domain.Series;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.Map;

public class JDBCBookDAO extends JDBCAbstractDAO<Book> {
    private Map<Integer, Series> collectedSeriesMap;
    private Map<Integer, Publisher> collectedPublisherMap;

    public JDBCBookDAO(Map<Integer, Series> series, Map<Integer, Publisher> publishers) {
        this.collectedSeriesMap = series;
        this.collectedPublisherMap = publishers;
    }

    @Override
    protected void assignObjectId(Book object, Integer id) {
        object.setId(id);
    }

    @Override
    protected String getCreateQuery() {
        return "INSERT INTO books " +
                "(isbn, uuid, title, series_id, series_number, publisher_id, " +
                "date_published, date_added, date_modified, path) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?);";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE books " +
                "SET isbn=?,uuid=?,title=?,series_id=?,series_number=?,publisher_id=?," +
                "date_published=?,date_added=?,date_modified=?,path=? " +
                "WHERE id=?;";
    }

    @Override
    protected String getReadAllQuery() {
        return "SELECT books.*, publishers.name AS publishers_name, series.name AS series_name " +
                "LEFT JOIN publishers ON books.publisher_id = publishers.id, " +
                "LEFT JOIN series ON books.series_id = series.id;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT books.*, publishers.name AS publishers_name, series.name AS series_name " +
                "LEFT JOIN publishers ON books.publisher_id = publishers.id, " +
                "LEFT JOIN series ON books.series_id = series.id " +
                "WHERE id=?;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "SELECT books.*, publishers.name AS publishers_name, series.name AS series_name " +
                "LEFT JOIN publishers ON books.publisher_id = publishers.id, " +
                "LEFT JOIN series ON books.series_id = series.id " +
                "WHERE name=?;";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM books WHERE id=?;";
    }

    @Override
    protected String getCountRowsQuery() {
        return "SELECT COUNT(*) from books AS count;";
    }

    @Override
    protected void setStatementObject(PreparedStatement stmt, Book object) throws DAOException {
        try {
            stmt.setString(1, object.getIsbn());
            stmt.setString(2, object.getUuid());
            stmt.setString(3, object.getTitle());

            if (object.getSeries() == null)
                stmt.setNull(4, Types.NULL);
            else
                stmt.setInt(4, object.getSeries().getId());

            stmt.setDouble(5, object.getSeriesNumber());

            if (object.getPublisher() == null)
                stmt.setNull(6, Types.NULL);
            else
                stmt.setInt(6, object.getPublisher().getId());

            stmt.setString(7, object.getPublishDate().toString());
            stmt.setString(8, object.getAddedDate().toString());
            stmt.setString(9, object.getModifiedDate().toString());
            stmt.setString(10, object.getPath());

            if (object.getId() > 0)
                stmt.setInt(11, object.getId());
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
    }

    @Override
    protected Book getObjectFromResultSet(ResultSet rs) throws DAOException {
        Book book = new Book();
        try {
            book.setId(rs.getInt("id"));
            book.setUuid(rs.getString("uuid"));
            book.setIsbn(rs.getString("isbn"));
            book.setTitle(rs.getString("title"));
            Integer seriesId = rs.getInt("series_id");
            if (seriesId > 0) {
                Series series = collectedSeriesMap.get(seriesId);
                if (series == null) {
                    series = new Series(rs.getString("series_name"));
                    collectedSeriesMap.put(seriesId, series);
                }
                book.setSeries(series);
            }
            Integer publisherId = rs.getInt("publisher_id");
            if (publisherId > 0) {
                Publisher publisher = collectedPublisherMap.get(publisherId);
                if (publisher == null) {
                    publisher = new Publisher(rs.getString("publisher_name"));
                    collectedPublisherMap.put(publisherId, publisher);
                }
                book.setPublisher(publisher);
            }
            book.setSeriesNumber(rs.getDouble("series_number"));
            book.setPublishDate(Instant.parse(rs.getString("date_published")));
            book.setAddedDate(Instant.parse(rs.getString("date_added")));
            book.setModifiedDate(Instant.parse(rs.getString("date_modified")));
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return null;
    }
}

package xyz.peasfultown.dao.impl;

import xyz.peasfultown.dao.DAOException;
import xyz.peasfultown.domain.Book;
import xyz.peasfultown.domain.Publisher;
import xyz.peasfultown.domain.SearchableRecordSet;
import xyz.peasfultown.domain.Series;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Instant;
import java.util.Set;

public class JDBCBookDAO extends JDBCAbstractDAO<Book> {
    private SearchableRecordSet<Series> seriesSet;
    private SearchableRecordSet<Publisher> publisherSet;

    public JDBCBookDAO(SearchableRecordSet<Series> series, SearchableRecordSet<Publisher> publishers) {
        this.seriesSet = series;
        this.publisherSet = publishers;
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
        return "SELECT B.*, P.name AS publisher_name, S.name AS series_name FROM books B " +
                "LEFT JOIN publishers P ON B.publisher_id = P.id " +
                "LEFT JOIN series S ON B.series_id = S.id ORDER BY B.id ASC;";
    }

    @Override
    protected String getReadByIdQuery() {
        return "SELECT B.*, P.name AS publisher_name, S.name AS series_name FROM books B " +
                "LEFT JOIN publishers P ON B.publisher_id = P.id " +
                "LEFT JOIN series S ON B.series_id = S.id " +
                "WHERE B.id=?;";
    }

    @Override
    protected String getReadByNameQuery() {
        return "SELECT B.*, P.name AS publisher_name, S.name AS series_name FROM books B " +
                "LEFT JOIN publishers P ON B.publisher_id = P.id " +
                "LEFT JOIN series S ON B.series_id = S.id " +
                "WHERE B.title=?;";
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
                Series series = (Series) seriesSet.getById(seriesId);
                if (series == null) {
                    series = new Series(rs.getString("series_name"));
                    seriesSet.add(series);
                }
                book.setSeries(series);
            }
            Integer publisherId = rs.getInt("publisher_id");
            if (publisherId > 0) {
                Publisher publisher = (Publisher) publisherSet.getById(publisherId);
                if (publisher == null) {
                    publisher = new Publisher(rs.getString("publisher_name"));
                    publisherSet.add(publisher);
                }
                book.setPublisher(publisher);
            }
            book.setSeriesNumber(rs.getDouble("series_number"));
            book.setPublishDate(Instant.parse(rs.getString("date_published")));
            book.setAddedDate(Instant.parse(rs.getString("date_added")));
            book.setModifiedDate(Instant.parse(rs.getString("date_modified")));
            book.setPath(rs.getString("path"));
        } catch (SQLException e) {
            throw new DAOException(e.getMessage(), e);
        }
        return book;
    }
}

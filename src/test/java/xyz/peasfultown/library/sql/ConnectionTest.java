package xyz.peasfultown.library.sql;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionTest {
    public static final Logger logger = LoggerFactory.getLogger(ConnectionTest.class);

    public static final String CONNECTION_STRING = "jdbc:sqlite:";

    Connection con = null;

    @Test
    void connectionWorking() {
        logger.info("executing connection test");
        establishConnection("testcon.db");

        String insertCoffee = "INSERT INTO coffee (name, price) VALUES('mcdonalds', 10.1)";
        String queryCoffee  = "SELECT * FROM coffee";

        try (Statement stmt = con.createStatement()){
            con.setAutoCommit(false);
            createTable(stmt);

            stmt.executeUpdate(insertCoffee);

            ResultSet rs = stmt.executeQuery(queryCoffee);

            if (rs.next()) {
                logger.info("results retrieved");
                logger.info("coffee name: {} | price: ${}", rs.getString("name"), rs.getDouble("price"));
            }

            con.setAutoCommit(true);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        closeConnection();

        Path dbfile = Paths.get("testcon.db");

        boolean fileExists = Files.exists(dbfile);
        assertTrue(fileExists, "Database file should have existed");
        logger.info("database file found");

        try {
            Files.delete(dbfile);
            logger.info("database file deleted");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    void createTable(Statement stmt) throws SQLException {
        String createTable = "CREATE TABLE IF NOT EXISTS coffee (" +
                "product_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT," +
                "price REAL" +
                ")";

        stmt.executeUpdate(createTable);
    }

    void establishConnection(String dbname) {
        try {
            con = DriverManager.getConnection(CONNECTION_STRING + dbname);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    void closeConnection() {
        try {
            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}

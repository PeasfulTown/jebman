package xyz.peasfultown.db;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class DbConnectionTest {
    public static final Logger logger = LoggerFactory.getLogger(DbConnectionTest.class);

    Connection con;

    @Test
    void connectionWorking() {
        logger.info("executing connection test");
        establishConnection();

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

        String dbFile = new StringBuilder(System.getProperty("java.io.tmpdir"))
                .append(System.getProperty("file.separator"))
                .append("test-metadata.db").toString();

        boolean fileExists = Files.exists(Path.of(dbFile));
        assertTrue(fileExists, "Database file should have existed");
        logger.info("database file found");

        try {
            Files.delete(Path.of(dbFile));
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

    void establishConnection() {
        try {
            con = DbConnection.getTestConnection();
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

/**
 * The MIT License (MIT)
 * =====================
 * <p>
 * Copyright © 2023 PeasfulTown
 * <p>
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * <p>
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

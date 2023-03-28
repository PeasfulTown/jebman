package xyz.peasfultown.library.sql;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class SQLBookTest {
    private static final Logger logger = LoggerFactory.getLogger(SQLBookTest.class);

    private static final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS book (" +
            "id         INTEGER," +
            "uuid       TEXT," +
            "isbn       TEXT," +
            "title      TEXT NOT NULL," +
            "authors    TEXT," +
            "add_date   TEXT," +
            "pub_date   TEXT," +
            "publisher  TEXT," +
            ""

    private static Connection con;

    @BeforeAll
    static void setup() {
        logger.info("Setting up SQLBookTest");
        establishConnection();
    }

    @AfterAll
    static void teardown() {
        closeConnection();
        logger.info("SQLBookTest tear down");
    }

    private static void createTableIfNotExists() {
    }

    private static void establishConnection() {
        try {
            con = DriverManager.getConnection("jdbc:sqlite:booktest.db");
            logger.info("---SQLite connection created");
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    private static void closeConnection() {
        try {
            if (con != null) {
                con.close();
                logger.info("---SQLite connection closed---");
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }
}
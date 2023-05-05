package xyz.peasfultown.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;

public class ScriptRunner {
    private static final char DELIM = ';';
    private static final String SQL_COMMENT_PREFIX = "--";
    public static void runScript(Connection con, File script) throws SQLException {
        con.setAutoCommit(false);
        try (BufferedReader br = Files.newBufferedReader(script.toPath())) {
            Iterator<String> iLines = br.lines().iterator();
            StringBuilder sb = new StringBuilder();
            processScript(con, sb, iLines);
        } catch (Exception e) {
            throw new SQLException(e.getMessage(), e);
        } finally {
            con.setAutoCommit(true);
        }
    }

    private static void processScript(Connection con, StringBuilder sb, Iterator<String> iLines) throws SQLException {
        while (iLines.hasNext()) {
            String line = iLines.next().trim();
            if (line.startsWith(SQL_COMMENT_PREFIX)) {
                // Do nothing
            } else if (line.endsWith(String.valueOf(DELIM))) {
                sb.append(line.substring(0, line.indexOf(DELIM) + 1));
                sb.append(" ");
                try {
                    executeUpdate(con, sb.toString());
                } catch (SQLException e) {
                    throw new SQLException(e.getMessage(), e);
                }
                sb = new StringBuilder();
            } else {
                sb.append(line);
                sb.append("\n");
            }
        }
    }

    private static void executeUpdate(Connection con, String query) throws SQLException {
        try (PreparedStatement stmt = con.prepareStatement(query)) {
            stmt.executeUpdate();
        }
    }
}

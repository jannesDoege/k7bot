package de.klassenserver7b.k7bot.sql;

import de.klassenserver7b.k7bot.util.InternalStatusCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class LiteSQL {
    private static Connection conn;
    public final static Logger dblog = LoggerFactory.getLogger("Database-Log");

    public static void connect() {
        conn = null;

        try {
            File file = new File("resources/datenbank.db");
            if (file.createNewFile()) {
                dblog.info("Databasefile created - assuming first start.");
            }

            String url = "jdbc:sqlite:" + file.getPath();
            conn = DriverManager.getConnection(url);
            dblog.info("Connection to SQLite has been established.");
        } catch (SQLException | IOException e) {
            dblog.error(e.getMessage(), e);
        }
    }

    public static void disconnect() {
        try {
            if (conn != null) {
                conn.close();
                dblog.info("Connection to SQLite has been closed.");
            }
        } catch (SQLException e) {
            dblog.error(e.getMessage(), e);
        }
    }

	public static int onUpdate(String sqlpattern, Object... parameters) {

		if (parameters.length != countMatches(sqlpattern, "?")) {
			IllegalArgumentException e = new IllegalArgumentException(
					"Invalid SQLString! - parameter count does not match.", new Throwable().fillInStackTrace());
			dblog.error(e.getMessage(), e);
			return -5;
		}

		try (PreparedStatement p = conn.prepareStatement(sqlpattern)) {

            for (int i = 0; i < parameters.length; i++) {
                p.setObject(i + 1, parameters[i]);
            }

            return p.executeUpdate();

        } catch (SQLException e) {
            dblog.error(e.getMessage(), e);
            return InternalStatusCodes.FAILURE;
        }
    }

    public static ResultSet onQuery(String sqlpattern, Object... parameters) throws SQLException {

        try {

			PreparedStatement p = conn.prepareStatement(sqlpattern);

            for (int i = 0; i < parameters.length; i++) {
                p.setObject(i + 1, parameters[i]);
            }

            return p.executeQuery();

        } catch (SQLException e) {
            dblog.error(e.getMessage(), e);
            throw e;
        }

    }

    public static Logger getdblog() {
        return dblog;
    }
}
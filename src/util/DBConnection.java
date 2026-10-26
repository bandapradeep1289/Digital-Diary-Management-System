package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Creates JDBC connections to MySQL.
 * Change the three constants below, or set the environment variables
 * DIARY_DB_URL, DIARY_DB_USER and DIARY_DB_PASS.
 */
public class DBConnection {
    private static final String URL  = env("DIARY_DB_URL",
            "jdbc:mysql://localhost:3306/digital_diary?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    private static final String USER = env("DIARY_DB_USER", "root");
    private static final String PASS = env("DIARY_DB_PASS", "root");

    private DBConnection() { }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isEmpty()) ? def : v;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}

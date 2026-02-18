package freshco.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Database helper for obtaining a shared JDBC connection and executing queries.
 *
 * NOTE: Legacy methods executeSearch(String) / executeIUD(String) remain for compatibility, but
 * new code should prefer the PreparedStatement-based overloads to avoid SQL injection.
 */
public class webDB {

    private static Connection connection;

    /**
     * Creates a singleton connection if one does not already exist.
     *
     * Connection properties are read from environment variables:
     *  - DB_URL (default: jdbc:mysql://localhost:3306/freshco)
     *  - DB_USER (default: root)
     *  - DB_PASSWORD (default: empty)
     */
    public static void createConnection() throws Exception { // Creating Connection Method
        if (connection == null || connection.isClosed()) {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Load Driver

            // Avoid hardcoding credentials; allow configuration via environment variables.
            String url = envOrDefault("DB_URL", "jdbc:mysql://localhost:3306/freshco");
            String user = envOrDefault("DB_USER", "root");
            String pass = envOrDefault("DB_PASSWORD", "");

            connection = DriverManager.getConnection(url, user, pass); // Establish the connection
        }
    }

    /**
     * Legacy helper: executes a raw SQL query string and returns a ResultSet.
     * Prefer executeSearch(String, Object...) to mitigate SQL injection.
     */
    public static ResultSet executeSearch(String query) throws Exception { // Executing Search Queries
        createConnection();// Create Connection
        return connection.createStatement().executeQuery(query);
    }

    /**
     * Legacy helper: executes a raw SQL I/U/D statement string and returns affected rows.
     * Prefer executeIUD(String, Object...) to mitigate SQL injection.
     */
    public static Integer executeIUD(String query) throws Exception { // Executing Input Update and Delete Queries
        createConnection(); //Create Connection
        return connection.createStatement().executeUpdate(query);
    }

    /**
     * Returns the shared connection (creating it if necessary).
     */
    public static Connection getConnection() throws Exception {
        createConnection();
        return connection;
    }

    // PUBLIC_INTERFACE
    public static ResultSet executeSearch(String sql, Object... params) throws Exception {
        /**
         * Executes a parameterized SELECT query using PreparedStatement.
         *
         * IMPORTANT: Caller MUST close the returned ResultSet. Closing the ResultSet will
         * also close the underlying PreparedStatement (per JDBC spec).
         *
         * @param sql SQL with '?' placeholders
         * @param params Parameters to bind in order
         * @return ResultSet of the executed query
         */
        createConnection();
        PreparedStatement ps = connection.prepareStatement(sql);
        bindParams(ps, params);
        return ps.executeQuery();
    }

    // PUBLIC_INTERFACE
    public static int executeIUD(String sql, Object... params) throws Exception {
        /**
         * Executes a parameterized INSERT/UPDATE/DELETE using PreparedStatement.
         *
         * @param sql SQL with '?' placeholders
         * @param params Parameters to bind in order
         * @return number of affected rows
         */
        createConnection();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        }
    }

    private static void bindParams(PreparedStatement ps, Object... params) throws Exception {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            // setObject handles most types safely; callers pass correct Java types.
            ps.setObject(i + 1, params[i]);
        }
    }

    private static String envOrDefault(String key, String defaultVal) {
        String val = System.getenv(key);
        return (val == null || val.isBlank()) ? defaultVal : val;
    }
}

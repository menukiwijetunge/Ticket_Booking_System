
package menuki.ticketing.data.jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Establishes connection between the Java code and the actual database.
 * Utilises db.properties
 * Used in other files to obtain JDBC connection
 */
public class DatabaseConnection {

    /* Contains the DB specific info needed for connection details */
    private static final String PROPERTIES_FILE = "/db.properties";

    /*
     * Loads DB configuration settings
     */
    private static Properties loadProps() throws Exception {
        Properties p = new Properties();
        try (InputStream in = DatabaseConnection.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) throw new RuntimeException("db.properties not found on classpath");
            p.load(in);
        }
        return p;
    }

    /*
     * Establishing root or server level JDBC connection
     */
    public static Connection getRootConnection() throws Exception {
        Properties p = loadProps();
        String url = p.getProperty("db.url");

        // Check if url contains any parameters after a '?' and seperate them from teh main URL part
        int q = url.indexOf("?");
        String base = (q >= 0) ? url.substring(0, q) : url;
        String params = (q >= 0) ? url.substring(q) : "";

        // if JDBC URL has a specific DB name remove it so we just have the server connection
        int slash = base.lastIndexOf("/");
        String prefix = (slash > "jdbc:mysql://".length()) ? base.substring(0, slash) : base;

        String finalUrl = prefix + params;
        return DriverManager.getConnection(finalUrl, p.getProperty("db.user"), p.getProperty("db.password"));
    }

    /*
     * Connect to a specific DB and get the connection
     */
    public static Connection getConnection(String dbName) throws Exception {
        Properties p = loadProps();
        String url = p.getProperty("db.url");

        // Separate JDBC URL into base and params
        int q = url.indexOf("?");
        String base = (q >= 0) ? url.substring(0, q) : url;
        String params = (q >= 0) ? url.substring(q) : "";


        //If the current base URL has a DB name at the end remove it so we can add a new DB name later on
        int slash = base.lastIndexOf("/");
        String prefix = (slash > "jdbc:mysql://".length()) ? base.substring(0, slash) : base;

        // If no params were given use a default string
        String paramPart = params.isEmpty()
                ? "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                : params;

        String finalUrl = prefix + "/" + dbName + paramPart;
        return DriverManager.getConnection(finalUrl, p.getProperty("db.user"), p.getProperty("db.password"));
    }

    /*
     * Create a normal JDBC connection to DB specified in db.properties and return
     */
    public static Connection getConnection() throws Exception {
        Properties p = loadProps();
        String url = p.getProperty("db.url");
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("db.url missing in db.properties");
        }
        return DriverManager.getConnection(url, p.getProperty("db.user"), p.getProperty("db.password"));
    }
}

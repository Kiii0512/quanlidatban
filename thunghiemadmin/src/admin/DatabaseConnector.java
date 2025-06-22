package admin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    public static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=restaurantdb;encrypt=true;trustServerCertificate=true;";
        String user = "appuser";
        String password = "1234";

        Connection conn = DriverManager.getConnection(url, user, password);
        return conn;
    }
}
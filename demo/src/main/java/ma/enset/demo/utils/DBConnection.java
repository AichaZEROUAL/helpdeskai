package ma.enset.demo.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    public Connection databaselink;

    public Connection getConnection() {
        String databaseName = "chatbot_db";
        String databaseUser = "root";
        String databasepwd = "";

        // Updated MySQL connection URL for compatibility with MySQL 8.0+
        String url = "jdbc:mysql://localhost:3306/" + databaseName + "?serverTimezone=UTC&useSSL=false";

        try {
            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Establish the connection
            databaselink = DriverManager.getConnection(url, databaseUser, databasepwd);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        return databaselink;
    }

    public void closeConnection() {
        try {
            if (databaselink != null && !databaselink.isClosed()) {
                databaselink.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
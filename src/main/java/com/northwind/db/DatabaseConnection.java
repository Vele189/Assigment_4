package com.northwind.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database connection parameters
    private static final String URL = "jdbc:mariadb://Chunky:3307/northwind";
    private static final String USER = "root";
    private static final String PASSWORD = "Dontcare777$"; // Empty password or set your password here

    private static Connection connection = null;

    // Get database connection
    public static Connection getConnection() {
        try {
            // Load the MariaDB JDBC driver
            Class.forName("org.mariadb.jdbc.Driver");
            System.out.println("MariaDB JDBC Driver loaded");
            
            System.out.println("Attempting to connect with:");
            System.out.println("URL: " + URL);
            System.out.println("User: " + USER);
            
            // Establish the connection
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MariaDB JDBC Driver not found");
            e.printStackTrace();
        } catch (SQLException e) {
            System.out.println("Connection to database failed");
            System.out.println("URL: " + URL);
            System.out.println("USER: " + USER);
            System.out.println("Error message: " + e.getMessage());
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        return null;
    }

    // Close database connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                System.err.println("Error closing database connection");
                e.printStackTrace();
            }
        }
    }
}

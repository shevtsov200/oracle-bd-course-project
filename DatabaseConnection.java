package com.project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DatabaseConnection {
    private Connection connection;
    private final String connectionString;
    private String username;
    private String password;

    DatabaseConnection() {
        connectionString = "jdbc:oracle:thin:@shevtsov200-N551JM:1521:XE";

        username = "shevtsov200";
        password = "1234";
    }

    Connection getConnection() {
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connection = DriverManager.getConnection(connectionString, username, password);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        if (connection != null) {
            System.out.println("Database connection has been established.");
        } else {
            System.out.println("Database connection failed.");
        }

        return connection;
    }
}

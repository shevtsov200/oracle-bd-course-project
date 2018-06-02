package com.project.database;

import com.sun.xml.internal.ws.util.StringUtils;
import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.Collections;

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

    public ResultSet getData(String procedureName) throws SQLException {
        Connection connection = getConnection();

        CallableStatement cs = connection.prepareCall("{ call ? := "+procedureName+" }");
        cs.registerOutParameter(1, OracleTypes.CURSOR);
        cs.execute();

        ResultSet resultSet = (ResultSet)cs.getObject(1);

        return(resultSet);
    }

    public ResultSet getData(String procedureName, String procedureParameter) throws SQLException {
        Connection connection = getConnection();

        String parameterString = "?";
        String callString = "{ call ? := " + procedureName + "( " +
                String.join("", Collections.nCopies(1, parameterString)) + ") }";
        System.out.println(callString);

        CallableStatement cs = connection.prepareCall(callString);

        cs.registerOutParameter(1, OracleTypes.CURSOR);
        cs.setString(2, procedureParameter);

        cs.execute();


        ResultSet resultSet = (ResultSet)cs.getObject(1);

        return(resultSet);
    }
}

package com.project.database;

import oracle.jdbc.OracleTypes;

import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

    ResultSet executeProcedure(String procedureName, SqlParameter[] procedureParameters) throws SQLException {
        Connection connection = getConnection();

        int inParametersNumber = (int)Arrays.stream(procedureParameters).filter(
                parameter -> (parameter.getParameterDirection() == SqlParameter.parameterDirections.IN)
                ).count();
        List<String> templateCharacters = Collections.nCopies(inParametersNumber, "?");
        String parameterString = String.join(", ", templateCharacters);


        boolean outParameterExists = Arrays.stream(procedureParameters).anyMatch(
                parameter -> (parameter.getParameterDirection() == SqlParameter.parameterDirections.OUT));

        String callString = outParameterExists ? "call ? :=" : "call";

        String callStatementString = "{ " + callString + " " + procedureName + "( " + parameterString + ") }";

        CallableStatement cs = connection.prepareCall(callStatementString);

        for (int i = 0; i < procedureParameters.length; ++i) {
            if(procedureParameters[i].getParameterDirection() == SqlParameter.parameterDirections.OUT) {
                cs.registerOutParameter(i+1,procedureParameters[i].getOracleType());
            } else if (procedureParameters[i].getParameterDirection() == SqlParameter.parameterDirections.IN) {
                cs.setString(i+1, procedureParameters[i].getValue());
            }
        }

        cs.execute();

        ResultSet resultSet = (ResultSet)cs.getObject(1);
        return(resultSet);
    }
}

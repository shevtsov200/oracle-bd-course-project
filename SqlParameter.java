package com.project.database;

import oracle.jdbc.OracleTypes;

public class SqlParameter {
    private final String value;

    private final int oracleType;

    enum parameterDirections {
        IN,
        OUT,
        INOUT
    }

    private final parameterDirections parameterDirection;

    public SqlParameter(String value, parameterDirections isOutParameter, int oracleType) {
        this.value = value;
        this.parameterDirection = isOutParameter;
        this.oracleType = oracleType;
    }

    public String getValue() {
        return value;
    }

    public parameterDirections getParameterDirection() {
        return parameterDirection;
    }
    public int getOracleType() {
        return oracleType;
    }
}

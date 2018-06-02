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

    private final parameterDirections isOutParameter;

    public SqlParameter(String value, parameterDirections isOutParameter, int oracleType) {
        this.value = value;
        this.isOutParameter = isOutParameter;
        this.oracleType = oracleType;
    }

    public String getValue() {
        return value;
    }

    public parameterDirections getIsOutParameter() {
        return isOutParameter;
    }
    public int getOracleType() {
        return oracleType;
    }
}

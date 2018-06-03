CREATE OR REPLACE PACKAGE types
AS
    TYPE ref_cursor IS REF CURSOR;
END;
/
CREATE OR REPLACE FUNCTION SELECT_GROUP(
    GROUP_ID_PARAMETER INTEGER
)
RETURN types.ref_cursor
AS
    groups_cursor types.ref_cursor;
BEGIN
    OPEN groups_cursor FOR
        SELECT g.GROUP_ID, GET_GROUP_NAME(g.GROUP_NAME) as "GROUP_NAME",
            GET_GROUP_YEAR(g.GROUP_NAME) as "GROUP_YEAR"
        FROM
        GROUPS g
        WHERE g.GROUP_ID = GROUP_ID_PARAMETER;
    RETURN groups_cursor;
END;
/
CREATE OR REPLACE FUNCTION SELECT_GROUPS
RETURN types.ref_cursor
AS
    groups_cursor types.ref_cursor;
BEGIN
    OPEN groups_cursor FOR
        SELECT g.GROUP_ID, GET_GROUP_NAME(g.GROUP_NAME) as "GROUP_NAME",
            GET_GROUP_YEAR(g.GROUP_NAME) as "GROUP_YEAR"
        FROM
        GROUPS g;
    RETURN groups_cursor;
END;
/
CREATE OR REPLACE PROCEDURE INSERT_GROUP (
    GROUP_NAME_PARAMETER VARCHAR
)
IS
BEGIN
    INSERT INTO GROUPS
    (GROUP_NAME)
    VALUES
    (GROUP_NAME_PARAMETER);
END;
/
CREATE OR REPLACE PROCEDURE UPDATE_GROUP (
    GROUP_ID_PARAMETER INTEGER,
    NAME_PARAMETER VARCHAR
)
IS
BEGIN
    UPDATE GROUPS g
    SET g.GROUP_NAME = NAME_PARAMETER
    WHERE g.GROUP_ID = GROUP_ID_PARAMETER;
END;
/
CREATE OR REPLACE PROCEDURE DELETE_GROUP (
    GROUP_ID_PARAMETER INTEGER
)
IS
BEGIN
    DELETE
    FROM GROUPS g
    WHERE g.GROUP_ID = GROUP_ID_PARAMETER;
END;
/
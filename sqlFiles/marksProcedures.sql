CREATE OR REPLACE PACKAGE types
AS
    TYPE ref_cursor IS REF CURSOR;
END;
/
-- get all marks
CREATE OR REPLACE FUNCTION SELECT_MARKS
RETURN types.ref_cursor
AS
    marks_cursor types.ref_cursor;
BEGIN
    OPEN marks_cursor FOR
        SELECT m.ID, m.STUDENT_ID, m.SUBJECT_ID, m.TEACHER_ID, m.MARK_VALUE, m.MARK_DATE
        FROM MARKS m;
    RETURN marks_cursor;
END;
/
-- get the mark using id
CREATE OR REPLACE FUNCTION SELECT_MARK (
    MARK_ID INTEGER
)
RETURN types.ref_cursor
AS
    marks_cursor types.ref_cursor;
BEGIN
    OPEN marks_cursor FOR
        SELECT m.ID, m.STUDENT_ID, std.LAST_NAME || ' ' || std.FIRST_NAME || ' ' || std.PATHER_NAME AS "STUDENT_NAME",
            m.SUBJECT_ID, s.SUBJECT_NAME,
            m.TEACHER_ID, tch.LAST_NAME || ' ' || tch.FIRST_NAME || ' ' || tch.PATHER_NAME AS "TEACHER_NAME",
            m.MARK_VALUE, m.MARK_DATE
        FROM MARKS m
        JOIN PEOPLE std ON m.STUDENT_ID = std.PEOPLE_ID
        JOIN PEOPLE tch ON m.TEACHER_ID = tch.PEOPLE_ID
        JOIN SUBJECTS s ON s.SUBJECT_ID = m.SUBJECT_ID
        WHERE m.ID = MARK_ID;
    RETURN marks_cursor;
END;
/
--get ids and names of all students
CREATE OR REPLACE FUNCTION SELECT_COMBO_STUDENTS
RETURN types.ref_cursor
AS
    students_cursor types.ref_cursor;
BEGIN
    OPEN students_cursor FOR
        SELECT p.LAST_NAME || ' ' || p.FIRST_NAME || ' ' || p.PATHER_NAME AS "STUDENT_NAME",
            p.PEOPLE_ID AS "STUDENT_ID"
        FROM PEOPLE p
        WHERE p.PEOPLE_TYPE = 'S';
    RETURN students_cursor;
END;
/
-- get ids and names of all teachers
CREATE OR REPLACE FUNCTION SELECT_COMBO_TEACHERS
RETURN types.ref_cursor
AS
    teachers_cursor types.ref_cursor;
BEGIN
    OPEN teachers_cursor FOR
        SELECT p.LAST_NAME || ' ' || p.FIRST_NAME || ' ' || p.PATHER_NAME AS "TEACHER_NAME",
            p.PEOPLE_ID AS "TEACHER_ID"
        FROM PEOPLE p
        WHERE p.PEOPLE_TYPE = 'T';
    RETURN teachers_cursor;
END;
/
-- get ids and names of all subjects
CREATE OR REPLACE FUNCTION SELECT_COMBO_SUBJECTS
RETURN types.ref_cursor
AS
    subjects_cursor types.ref_cursor;
BEGIN
    OPEN subjects_cursor FOR
        SELECT s.SUBJECT_NAME,s.SUBJECT_ID
        FROM SUBJECTS s;
    RETURN subjects_cursor;
END;
/
-- create a new mark
CREATE OR REPLACE PROCEDURE INSERT_MARK (
    STUDENT_ID_PARAMETER INTEGER,
    SUBJECT_ID_PARAMETER INTEGER,
    TEACHER_ID_PARAMETER INTEGER,
    MARK_VALUE_PARAMETER INTEGER,
    MARK_DATE_PARAMETER DATE
)
IS
BEGIN
    INSERT INTO MARKS
    (STUDENT_ID, SUBJECT_ID, TEACHER_ID, MARK_VALUE, MARK_DATE)
    VALUES
    (STUDENT_ID_PARAMETER, SUBJECT_ID_PARAMETER, TEACHER_ID_PARAMETER, MARK_VALUE_PARAMETER, MARK_DATE_PARAMETER);
END;
/
-- update the mark using id
CREATE OR REPLACE PROCEDURE UPDATE_MARK (
    MARK_ID_PARAMETER INTEGER,
    STUDENT_ID_PARAMETER INTEGER,
    SUBJECT_ID_PARAMETER INTEGER,
    TEACHER_ID_PARAMETER INTEGER,
    MARK_VALUE_PARAMETER INTEGER,
    MARK_DATE_PARAMETER DATE
)
IS
BEGIN
    UPDATE MARKS m
    SET m.STUDENT_ID = STUDENT_ID_PARAMETER,
        m.SUBJECT_ID = SUBJECT_ID_PARAMETER,
        m.TEACHER_ID = TEACHER_ID_PARAMETER,
        m.MARK_VALUE = MARK_VALUE_PARAMETER,
        m.MARK_DATE = MARK_DATE_PARAMETER
    WHERE m.ID = MARK_ID_PARAMETER;
END;
/
-- delete the mark using id
CREATE OR REPLACE PROCEDURE DELETE_MARK (
    MARK_ID_PARAMETER INTEGER
)
IS
BEGIN
    DELETE
    FROM MARKS m
    WHERE m.ID = MARK_ID_PARAMETER;
END;
/
-- trigger to control that mark value is between 2 and 5
CREATE OR REPLACE TRIGGER MARK_INTERVAL_TRIGGER
BEFORE INSERT ON MARKS
FOR EACH ROW
DECLARE
    NEW_MARK MARKS.MARK_VALUE%TYPE;
    MIN_MARK_VALUE NUMBER := 2;
    MAX_MARK_VALUE NUMBER := 5;   
BEGIN
    NEW_MARK := (:NEW.MARK_VALUE);
    
    IF NEW_MARK < MIN_MARK_VALUE OR
        NEW_MARK > MAX_MARK_VALUE
    THEN
        RAISE_APPLICATION_ERROR(-20000, 'Mark value must be in interval:('||MIN_MARK_VALUE||':'||MAX_MARK_VALUE||')');
    END IF;
END;
/
-- select an average mark value of the student
CREATE OR REPLACE FUNCTION SELECT_STUDENT_AVG (
    STUDENT_ID INTEGER,
    DATE_FROM DATE,
    DATE_TO DATE
)
RETURN types.ref_cursor
AS
    mark_cursor types.ref_cursor;
BEGIN
    OPEN mark_cursor FOR 
        SELECT AVG(m.MARK_VALUE)
        FROM MARKS m
        JOIN PEOPLE p ON p.PEOPLE_ID = m.STUDENT_ID
        WHERE m.STUDENT_ID = STUDENT_ID AND 
            m.MARK_DATE <= DATE_TO AND m.MARK_DATE >= DATE_FROM;
    RETURN mark_cursor;
END;
/
-- select an average mark value of the group
CREATE OR REPLACE FUNCTION SELECT_GROUP_AVG (
    GROUP_ID_PARAMETER INTEGER,
    DATE_FROM DATE,
    DATE_TO DATE
)
RETURN types.ref_cursor
AS
    mark_cursor types.ref_cursor;
BEGIN
    OPEN mark_cursor FOR 
        SELECT AVG(m.MARK_VALUE)
        FROM MARKS m
        JOIN PEOPLE p ON p.PEOPLE_ID = m.STUDENT_ID
        JOIN GROUPS g ON g.GROUP_ID = p.GROUP_ID
        WHERE g.GROUP_ID = GROUP_ID_PARAMETER AND
            m.MARK_DATE <= DATE_TO AND m.MARK_DATE >= DATE_FROM;
    RETURN mark_cursor;
END;
/
-- select average mark value of the subject
CREATE OR REPLACE FUNCTION SELECT_SUBJECT_AVG (
    SUBJECT_ID_PARAMETER INTEGER,
    DATE_FROM DATE,
    DATE_TO DATE
)
RETURN types.ref_cursor
AS
    mark_cursor types.ref_cursor;
BEGIN
    OPEN mark_cursor FOR 
        SELECT AVG(m.MARK_VALUE)
        FROM MARKS m
        WHERE m.SUBJECT_ID = SUBJECT_ID_PARAMETER AND
            m.MARK_DATE <= DATE_TO AND m.MARK_DATE >= DATE_FROM;
    RETURN mark_cursor;
END;
/
-- select average mark value of the teacher
CREATE OR REPLACE FUNCTION SELECT_TEACHER_AVG (
    TEACHER_ID_PARAMETER INTEGER,
    DATE_FROM DATE,
    DATE_TO DATE
)
RETURN types.ref_cursor
AS
    mark_cursor types.ref_cursor;
BEGIN
    OPEN mark_cursor FOR 
        SELECT AVG(m.MARK_VALUE)
        FROM MARKS m
        JOIN PEOPLE p ON p.PEOPLE_ID = m.TEACHER_ID
        WHERE m.TEACHER_ID = TEACHER_ID_PARAMETER AND 
            m.MARK_DATE <= DATE_TO AND m.MARK_DATE >= DATE_FROM;
    RETURN mark_cursor;
END;
/
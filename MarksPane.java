package com.project.database;

import net.codejava.swing.DateLabelFormatter;
import oracle.jdbc.OracleTypes;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class MarksPane {
    private JSplitPane marksSplitPane;

    private final DatabaseConnection dbConnection;

    private JLabel studentLabel;
    private JLabel subjectLabel;
    private JLabel teacherLabel;

    private JButton createButton;
    private JPanel comboBoxesPanel;
    private JSplitPane studentSplitPane;
    private JTable dbTable;
    private JPanel buttonsPanel;
    private JButton updateButton;
    private JButton deleteButton;
    private JPanel marksSplitPanel;
    private JComboBox studentComboBox;
    private JComboBox subjectComboBox;
    private JComboBox teacherComboBox;
    private JDatePickerImpl datePicker;
    private JLabel markLabel;
    private JComboBox markComboBox;
    private JLabel dateLabel;

    private static final String STUDENT_TEXT = "Студент";
    private static final String SUBJECT_TEXT = "Предмет";
    private static final String TEACHER_TEXT = "Преподаватель";
    private static final String MARK_TEXT = "Оценка";
    private static final String DATE_TEXT = "Дата";

    private static final String ADD_BUTTON_TEXT = "Создать";
    private static final String UPDATE_BUTTON_TEXT = "Обновить";
    private static final String DELETE_BUTTON_TEXT = "Удалить";

    private int currentRowId = 0;

    public MarksPane() {
        studentLabel.setText(STUDENT_TEXT);
        subjectLabel.setText(SUBJECT_TEXT);
        teacherLabel.setText(TEACHER_TEXT);
        markLabel.setText(MARK_TEXT);
        dateLabel.setText(DATE_TEXT);

        createButton.setText(ADD_BUTTON_TEXT);
        updateButton.setText(UPDATE_BUTTON_TEXT);
        deleteButton.setText(DELETE_BUTTON_TEXT);

        dbConnection = new DatabaseConnection();

        try {
            populateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        initialiseStudents();
        initialiseTeachers();
        initialiseSubjects();
        initialiseMarks();


        createButton.addActionListener(new MarksPane.CreateButtonClicked());
        updateButton.addActionListener(new MarksPane.UpdateButtonClicked());
        deleteButton.addActionListener(new MarksPane.DeleteButtonClicked());

        dbTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                DefaultTableModel tableModel = (DefaultTableModel) dbTable.getModel();

                int selectedRowIndex = dbTable.getSelectedRow();
                String id = tableModel.getValueAt(selectedRowIndex,0).toString();

                SqlParameter[] parameters = new SqlParameter[] {
                        new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
                        new SqlParameter(id, SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
                };

                try {
                    ResultSet resultSet = dbConnection.executeProcedure("SELECT_MARK", parameters);

                    while(resultSet.next()) {
                        int studentId = resultSet.getInt("student_id");
                        String studentName = resultSet.getString("student_name");
                        studentComboBox.setSelectedItem(new ComboItem(studentId, studentName));

                        int subjectId = resultSet.getInt("subject_id");
                        String subjectName = resultSet.getString("subject_name");
                        subjectComboBox.setSelectedItem(new ComboItem(subjectId, subjectName));

                        int teacherId = resultSet.getInt("teacher_id");
                        String teacherName = resultSet.getString("teacher_name");
                        teacherComboBox.setSelectedItem(new ComboItem(teacherId, teacherName));

                        int markValue = resultSet.getInt("mark_value");
                        markComboBox.setSelectedIndex(markValue-2);


                        java.sql.Date date = resultSet.getDate("mark_date");
                        String strDate = resultSet.getString("mark_date");
                        System.out.println(strDate);
                        LocalDate localDate = date.toLocalDate();

                        datePicker.getModel().setYear(localDate.getYear());
                        datePicker.getModel().setMonth(localDate.getMonthValue()-1);
                        datePicker.getModel().setDay(localDate.getDayOfMonth());
                        datePicker.getModel().setSelected(true);

                        currentRowId = resultSet.getInt("id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new MarksPane().marksSplitPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        System.out.println("packed frame");
        frame.setVisible(true);
    }

    private DefaultTableModel buildTableModel(ResultSet resultSet) throws SQLException {

        ResultSetMetaData metaData = resultSet.getMetaData();
        Vector<String> columnNames = new Vector<>();
        int columnCount = metaData.getColumnCount();
        for(int column = 1; column <= columnCount; ++column) {
            columnNames.add(metaData.getColumnName(column));
        }

        Vector<Vector<Object>> data = new Vector<>();
        while (resultSet.next()) {
            Vector<Object> vector = new Vector<>();
            for (int columnIndex = 1; columnIndex <= columnCount; ++columnIndex) {
                vector.add(resultSet.getObject(columnIndex));
            }
            data.add(vector);
        }
        return new DefaultTableModel(data, columnNames);
    }

    private void populateTable() throws SQLException {
        SqlParameter[] parameters = new SqlParameter[] {
                new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
        };

        ResultSet resultSet = dbConnection.executeProcedure("SELECT_MARKS", parameters);
        DefaultTableModel tableModel = buildTableModel(resultSet);
        dbTable.setModel(tableModel);
    }

    private void createUIComponents() {
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("test.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        model.setDate( 2014, 8, 24 );
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        System.out.println(datePicker.getModel().getValue());
    }

    private class CreateButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SqlParameter[] parameters = new SqlParameter[] {
//                    new SqlParameter(lastNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
//                    new SqlParameter(firstNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
//                    new SqlParameter(patherNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
            };

            try {
                dbConnection.executeProcedure("INSERT_TEACHER", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class UpdateButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SqlParameter[] parameters = new SqlParameter[] {
//                    new SqlParameter(Integer.toString(currentRowId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
//                    new SqlParameter(lastNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
//                    new SqlParameter(firstNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
//                    new SqlParameter(patherNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
            };

            try {
                dbConnection.executeProcedure("UPDATE_TEACHER", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class DeleteButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(Integer.toString(currentRowId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
            };

            try {
                dbConnection.executeProcedure("DELETE_TEACHER", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void initialiseStudents() {
        try {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
            };
            ResultSet resultSet = dbConnection.executeProcedure("SELECT_COMBO_STUDENTS", parameters);

            while (resultSet.next()) {
                String studentName = resultSet.getString("student_name");
                int studentId = resultSet.getInt("student_id");

                ComboItem comboItem = new ComboItem(studentId, studentName);
                studentComboBox.addItem(comboItem);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void initialiseTeachers() {
        try {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
            };
            ResultSet resultSet = dbConnection.executeProcedure("SELECT_COMBO_TEACHERS", parameters);

            while (resultSet.next()) {
                String studentName = resultSet.getString("teacher_name");
                int studentId = resultSet.getInt("teacher_id");

                ComboItem comboItem = new ComboItem(studentId, studentName);
                teacherComboBox.addItem(comboItem);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void initialiseSubjects() {
        try {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
            };
            ResultSet resultSet = dbConnection.executeProcedure("SELECT_COMBO_SUBJECTS", parameters);

            while (resultSet.next()) {
                String studentName = resultSet.getString("subject_name");
                int studentId = resultSet.getInt("subject_id");

                ComboItem comboItem = new ComboItem(studentId, studentName);
                subjectComboBox.addItem(comboItem);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void initialiseMarks() {
        String[] marks = {"2", "3", "4", "5"};

        markComboBox.setModel(new DefaultComboBoxModel(marks));
    }
}

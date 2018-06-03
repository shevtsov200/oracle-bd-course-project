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
import java.util.Properties;
import java.util.Vector;

public class MarksPane {
    private JSplitPane teacherSplitPane;

    private final DatabaseConnection dbConnection;

    private JTextField firstNameTextField;
    private JTextField patherNameTextField;
    private JTextField lastNameTextField;

    private JLabel studentLabel;
    private JLabel subjectLabel;
    private JLabel teacherLabel;

    private JButton createButton;
    private JPanel addStudentPanel;
    private JSplitPane studentSplitPane;
    private JTable dbTable;
    private JPanel buttonsPanel;
    private JButton updateButton;
    private JButton deleteButton;
    private JPanel teacherPanel;
    private JComboBox studentComboBox;
    private JComboBox subjectComboBox;
    private JComboBox teacherComboBox;
    private JDatePickerImpl datePicker;
    private JLabel markLabel;
    private JComboBox markComboBox;
    private JLabel dateLabel;

    private static final String FIRST_NAME_TEXT = "Имя";
    private static final String LAST_NAME_TEXT = "Фамилия";
    private static final String PATHER_NAME_TEXT = "Отчество";

    private static final String ADD_BUTTON_TEXT = "Создать";
    private static final String UPDATE_BUTTON_TEXT = "Обновить";
    private static final String DELETE_BUTTON_TEXT = "Удалить";

    private int currentRowId = 0;

    public MarksPane() {

        studentLabel.setText(LAST_NAME_TEXT);
        subjectLabel.setText(FIRST_NAME_TEXT);
        teacherLabel.setText(PATHER_NAME_TEXT);

        createButton.setText(ADD_BUTTON_TEXT);
        updateButton.setText(UPDATE_BUTTON_TEXT);
        deleteButton.setText(DELETE_BUTTON_TEXT);

        dbConnection = new DatabaseConnection();

        try {
            populateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
                    ResultSet resultSet = dbConnection.executeProcedure("SELECT_TEACHER", parameters);

                    while(resultSet.next()) {
                        firstNameTextField.setText(resultSet.getString("first_name"));
                        lastNameTextField.setText(resultSet.getString("last_name"));
                        patherNameTextField.setText(resultSet.getString("pather_name"));

                        currentRowId = resultSet.getInt("people_id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new MarksPane().teacherPanel);
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

        ResultSet resultSet = dbConnection.executeProcedure("SELECT_TEACHERS", parameters);
        DefaultTableModel tableModel = buildTableModel(resultSet);
        dbTable.setModel(tableModel);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
        UtilDateModel model = new UtilDateModel();
        Properties properties = new Properties();
        properties.put("test.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, properties);
        datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());
    }

    private class CreateButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(lastNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(firstNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(patherNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
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
                    new SqlParameter(Integer.toString(currentRowId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(lastNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(firstNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(patherNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
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
}

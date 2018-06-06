package com.project.database;

import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class DatabasePane extends JSplitPane {

    private JPanel panel;
    private JSplitPane databaseSplitPane;

    private JTabbedPane addStudentPane;
    private final DatabaseConnection dbConnection;

    private JTextField firstNameTextField;
    private JTextField patherNameTextField;
    private JTextField lastNameTextField;
    private JComboBox<ComboItem> groupComboBox;

    private JLabel lastNameLabel;
    private JLabel firstNameLabel;
    private JLabel patherNameLabel;
    private JLabel groupNameLabel;

    private JButton addStudentButton;
    private JPanel addStudentPanel;
    private JSplitPane studentSplitPane;
    private JTable studentsTable;
    private JPanel buttonsPanel;
    private JButton updateStudentButton;
    private JButton deleteStudentButton;
    private JButton averageMarkButton;
    private JPanel databasePane;

    private static final String FIRST_NAME_TEXT = "Имя";
    private static final String LAST_NAME_TEXT = "Фамилия";
    private static final String PATHER_NAME_TEXT = "Отчество";
    private static final String GROUP_TEXT = "Группа";

    private static final String ADD_BUTTON_TEXT = "Создать";
    private static final String UPDATE_BUTTON_TEXT = "Обновить";
    private static final String DELETE_BUTTON_TEXT = "Удалить";

    private int currentRowId = 0;

    public DatabasePane() {

        lastNameLabel.setText(LAST_NAME_TEXT);
        firstNameLabel.setText(FIRST_NAME_TEXT);
        patherNameLabel.setText(PATHER_NAME_TEXT);
        groupNameLabel.setText(GROUP_TEXT);

        addStudentButton.setText(ADD_BUTTON_TEXT);
        updateStudentButton.setText(UPDATE_BUTTON_TEXT);
        deleteStudentButton.setText(DELETE_BUTTON_TEXT);

        dbConnection = new DatabaseConnection();

        groupComboBox.removeAllItems();
        initialiseGroups();

        try {
            populateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        addStudentButton.addActionListener(new DatabasePane.AddStudentButtonClicked());
        updateStudentButton.addActionListener(new DatabasePane.UpdateStudentButtonClicked());
        deleteStudentButton.addActionListener(new DatabasePane.DeleteStudentButtonClicked());

        studentsTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                super.mouseClicked(mouseEvent);
                DefaultTableModel tableModel = (DefaultTableModel) studentsTable.getModel();

                int selectedRowIndex = studentsTable.getSelectedRow();
                String id = tableModel.getValueAt(selectedRowIndex,0).toString();

                SqlParameter[] parameters = new SqlParameter[] {
                        new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
                        new SqlParameter(id, SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
                };

                try {
                    ResultSet resultSet = dbConnection.executeProcedure("SELECT_STUDENT", parameters);

                    while(resultSet.next()) {
                        firstNameTextField.setText(resultSet.getString("first_name"));
                        lastNameTextField.setText(resultSet.getString("last_name"));
                        patherNameTextField.setText(resultSet.getString("pather_name"));

                        int groupId = resultSet.getInt("group_id");
                        String groupName = resultSet.getString("group_name");
                        System.out.println("selected " + groupId + " " + groupName);
                        groupComboBox.setSelectedItem(new ComboItem(groupId, groupName));

                        currentRowId = resultSet.getInt("people_id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        averageMarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        AnalysisPane averageMarkPane = new AnalysisPane("SELECT_STUDENT_AVG",5);
                        JDialog dialog = new JDialog();

                        dialog.setContentPane(averageMarkPane.getPanel());
                        //dialog.setModal(true);
                        dialog.pack();
                        //dialog.setSize(500,500);
                        dialog.setVisible(true);
                    }
                });

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new DatabasePane().panel);
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

    private void initialiseGroups() {
        try {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
            };
            ResultSet resultSet = dbConnection.executeProcedure("SELECT_GROUPS_ID_NAMES", parameters);

            while (resultSet.next()) {
                String groupName = resultSet.getString("group_name");
                int groupId = resultSet.getInt("group_id");

                ComboItem comboItem = new ComboItem(groupId, groupName);
                groupComboBox.addItem(comboItem);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    private void populateTable() throws SQLException {
        SqlParameter[] parameters = new SqlParameter[] {
                new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
        };

        ResultSet resultSet = dbConnection.executeProcedure("SELECT_STUDENTS", parameters);
        DefaultTableModel tableModel = buildTableModel(resultSet);
        studentsTable.setModel(tableModel);
    }

    private class AddStudentButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ComboItem selectedItem = (ComboItem) groupComboBox.getSelectedItem();
            int groupId = selectedItem.getId();
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(lastNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(firstNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(patherNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(Integer.toString(groupId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
            };

            try {
                dbConnection.executeProcedure("INSERT_STUDENT", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class UpdateStudentButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ComboItem selectedItem = (ComboItem) groupComboBox.getSelectedItem();
            int groupId = selectedItem.getId();
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(Integer.toString(currentRowId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(lastNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(firstNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(patherNameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(Integer.toString(groupId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
            };

            try {
                dbConnection.executeProcedure("UPDATE_STUDENT", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class DeleteStudentButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(Integer.toString(currentRowId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
            };

            try {
                dbConnection.executeProcedure("DELETE_STUDENT", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
}

package com.project.database;

import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Vector;

public class SubjectsPane {
    private JSplitPane subjectSplitPane;
    private JTable dbTable;
    private JPanel textFieldsPanel;
    private JPanel buttonsPanel;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton createButton;
    private JLabel yearLabel;
    private JTextField nameTextField;
    private JTextField yearTextField;
    private JLabel nameLabel;
    private JPanel subjectsPanel;
    private JPanel groupPanel;

    private static final String SUBJECT_NAME = "Название предмета";

    private static final String CREATE_BUTTON_TEXT = "Создать";
    private static final String UPDATE_BUTTON_TEXT = "Обновить";
    private static final String DELETE_BUTTON_TEXT = "Удалить";

    private final DatabaseConnection dbConnection;

    private int currentRowId = 0;

    public SubjectsPane() {
        nameLabel.setText(SUBJECT_NAME);

        createButton.setText(CREATE_BUTTON_TEXT);
        updateButton.setText(UPDATE_BUTTON_TEXT);
        deleteButton.setText(DELETE_BUTTON_TEXT);

        dbConnection = new DatabaseConnection();

        try {
            populateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        createButton.addActionListener(new SubjectsPane.CreateButtonClicked());
        updateButton.addActionListener(new SubjectsPane.UpdateButtonClicked());
        deleteButton.addActionListener(new SubjectsPane.DeleteButtonClicked());

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
                    ResultSet resultSet = dbConnection.executeProcedure("SELECT_SUBJECT", parameters);

                    while(resultSet.next()) {
                        nameTextField.setText(resultSet.getString("subject_name"));

                        currentRowId = resultSet.getInt("subject_id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new SubjectsPane().groupPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
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

        ResultSet resultSet = dbConnection.executeProcedure("SELECT_SUBJECTS", parameters);
        DefaultTableModel tableModel = buildTableModel(resultSet);
        dbTable.setModel(tableModel);
    }

    private class CreateButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(nameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
            };

            try {
                dbConnection.executeProcedure("INSERT_SUBJECT", parameters);
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
                    new SqlParameter(nameTextField.getText(), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
            };

            try {
                dbConnection.executeProcedure("UPDATE_SUBJECT", parameters);
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
                dbConnection.executeProcedure("DELETE_SUBJECT", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }
}

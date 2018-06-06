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

public class TeachersPane {
    private JTable dbTable;

    private JPanel teacherPanel;
    private JSplitPane teacherSplitPane;
    private JLabel patherNameLabel;
    private JTextField patherNameTextField;
    private JLabel firstNameLabel;
    private JTextField lastNameTextField;
    private JTextField firstNameTextField;
    private JLabel lastNameLabel;
    private JPanel textFieldsPanel;
    private ButtonPane buttonsPanel;

    private static final String FIRST_NAME_TEXT = "Имя";
    private static final String LAST_NAME_TEXT = "Фамилия";
    private static final String PATHER_NAME_TEXT = "Отчество";

    private final DatabaseConnection dbConnection;

    private int currentRowId = 0;

    public TeachersPane() {
        lastNameLabel.setText(LAST_NAME_TEXT);
        firstNameLabel.setText(FIRST_NAME_TEXT);
        patherNameLabel.setText(PATHER_NAME_TEXT);

        dbConnection = new DatabaseConnection();

        try {
            populateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }

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
        frame.setContentPane(new TeachersPane().teacherPanel);
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
        buttonsPanel = new ButtonPane(new TeachersPane.CreateButtonClicked(), new TeachersPane.UpdateButtonClicked(),
                new TeachersPane.DeleteButtonClicked(), new TeachersPane.AverageButtonClicked());
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

    private class AverageButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    AnalysisPane averageMarkPane = new AnalysisPane("SELECT_TEACHER_AVG",currentRowId);
                    JDialog dialog = new JDialog();

                    dialog.setContentPane(averageMarkPane.getPanel());
                    dialog.pack();
                    dialog.setVisible(true);
                }
            });

        }
    }
}

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

public class GroupsPane {
    private JSplitPane groupSplitPane;
    private JTable dbTable;
    private JPanel textFieldsPanel;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton createButton;
    private JLabel yearLabel;
    private JTextField nameTextField;
    private JTextField yearTextField;
    private JLabel nameLabel;
    private JPanel groupPanel;
    private ButtonPane buttonsPanel;

    private static final String GROUP_NUMBER = "Номер группы";
    private static final String GROUP_YEAR = "Год выпуска";

    private final DatabaseConnection dbConnection;

    private int currentRowId = 0;

    public GroupsPane() {

        nameLabel.setText(GROUP_NUMBER);
        yearLabel.setText(GROUP_YEAR);

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
                    ResultSet resultSet = dbConnection.executeProcedure("SELECT_GROUP", parameters);

                    while(resultSet.next()) {
                        nameTextField.setText(resultSet.getString("group_name"));
                        yearTextField.setText(resultSet.getString("group_year"));

                        currentRowId = resultSet.getInt("group_id");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new GroupsPane().groupPanel);
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

        ResultSet resultSet = dbConnection.executeProcedure("SELECT_GROUPS", parameters);
        DefaultTableModel tableModel = buildTableModel(resultSet);
        dbTable.setModel(tableModel);
    }

    private void createUIComponents() {
        buttonsPanel = new ButtonPane(new GroupsPane.CreateButtonClicked(), new GroupsPane.UpdateButtonClicked(),
                new GroupsPane.DeleteButtonClicked(), new GroupsPane.AverageButtonClicked());
    }

    private class CreateButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String concatenatedName = getConcatenatedGroupName();

            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(concatenatedName, SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
            };

            try {
                dbConnection.executeProcedure("INSERT_GROUP", parameters);
                populateTable();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    private class UpdateButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String concatenatedName = getConcatenatedGroupName();


            SqlParameter[] parameters = new SqlParameter[] {
                    new SqlParameter(Integer.toString(currentRowId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                    new SqlParameter(concatenatedName, SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
            };

            try {
                dbConnection.executeProcedure("UPDATE_GROUP", parameters);
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
                dbConnection.executeProcedure("DELETE_GROUP", parameters);
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
                    AnalysisPane averageMarkPane = new AnalysisPane("SELECT_GROUP_AVG",currentRowId);
                    JDialog dialog = new JDialog();

                    dialog.setContentPane(averageMarkPane.getPanel());
                    dialog.pack();
                    dialog.setVisible(true);
                }
            });

        }
    }

    private String getConcatenatedGroupName() {
        String groupName = nameTextField.getText();
        String groupYear = yearTextField.getText();

        return(groupName+"_"+groupYear);
    }
}

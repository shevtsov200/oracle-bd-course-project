package com.project.database;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import oracle.jdbc.OracleTypes;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static javax.swing.UIManager.put;

class DeanOffice {
    private JTabbedPane addStudentPane;
    private JPanel panel1;
    private final DatabaseConnection dbConnection;

    private JTextField firstNameTextField;
    private JTextField patherNameTextField;
    private JTextField lastNameTextField;
    private JComboBox groupComboBox;

    private JLabel lastNameLabel;
    private JLabel firstNameLabel;
    private JLabel patherNameLabel;
    private JLabel groupNameLabel;

    private JButton addStudentButton;
    private JPanel addStudentPanel;
    private JSplitPane studentSplitPane;
    private JTable studentsTable;

    private static final Map<Integer, String> PANE_TITLES = new HashMap<Integer, String>() {{
        put(0, "Добавить студента");
    }};
    private static final String FIRST_NAME_TEXT = "Имя";
    private static final String LAST_NAME_TEXT = "Фамилия";
    private static final String PATHER_NAME_TEXT = "Отчество";
    private static final String GROUP_TEXT = "Группа";
    private static final String ADD_STUDENT_TEXT = "Добавить студента";

    public DeanOffice() {

        lastNameLabel.setText(LAST_NAME_TEXT);
        firstNameLabel.setText(FIRST_NAME_TEXT);
        patherNameLabel.setText(PATHER_NAME_TEXT);
        groupNameLabel.setText(GROUP_TEXT);
        addStudentButton.setText(ADD_STUDENT_TEXT);

        for (int i = 0; i < addStudentPane.getTabCount(); ++i) {
            addStudentPane.setTitleAt(i, PANE_TITLES.get(i));
        }

        dbConnection = new DatabaseConnection();

        groupComboBox.removeAllItems();
        initialiseGroups();

        try {
            populateTable();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addStudentButton.addActionListener(new AddStudentButtonClicked());
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new DeanOffice().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void initialiseGroups() {
        final String GROUP_NAME_COLUMN = "GROUP_NAME";
        final String GROUP_ID_COLUMN = "GROUP_ID";

        Connection connection = dbConnection.getConnection();

        try {
            CallableStatement cs = connection.prepareCall("{ call ? := SELECT_GROUPS }");
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            ResultSet resultSet = (ResultSet)cs.getObject(1);

            while (resultSet.next()) {
                String groupName = resultSet.getString(GROUP_NAME_COLUMN);
                int groupId = resultSet.getInt(GROUP_ID_COLUMN);
                ComboItem comboItem = new ComboItem(groupId, groupName);
                groupComboBox.addItem(comboItem);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
        Connection connection = dbConnection.getConnection();

        CallableStatement cs = connection.prepareCall("{ call ? := SELECT_STUDENTS }");
        cs.registerOutParameter(1, OracleTypes.CURSOR);
        cs.execute();

        ResultSet resultSet = (ResultSet)cs.getObject(1);
        DefaultTableModel tableModel = buildTableModel(resultSet);
        studentsTable.setModel(tableModel);
    }

    private class AddStudentButtonClicked implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Connection connection = dbConnection.getConnection();

            try {
                CallableStatement cs = connection.prepareCall("{ call INSERT_STUDENT(?, ?, ?, ?) }");

                cs.setString(1, firstNameTextField.getText());
                cs.setString(2, lastNameTextField.getText());
                cs.setString(3, patherNameTextField.getText());

                ComboItem selectedItem = (ComboItem) groupComboBox.getSelectedItem();
                int groupId = selectedItem.getId();
                cs.setInt(4, groupId);

                cs.execute();

            } catch (Exception exception) {
                System.out.println("Exception: " + exception);
            } finally {
                try {
                    connection.close();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}

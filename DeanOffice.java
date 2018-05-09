package com.project.database;

import oracle.jdbc.OracleTypes;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;



public class DeanOffice {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private DatabaseConnection dbConnection;

    private JTextField firstNameTextField;
    private JTextField patherNameTextField;
    private JTextField lastNameTextField;
    private JComboBox groupComboBox;

    private JLabel lastNameLabel;
    private JLabel firstNameLabel;
    private JLabel patherNameLabel;
    private JLabel groupNameLabel;

    private JButton addStudentButton;

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

        dbConnection = new DatabaseConnection();

        groupComboBox.removeAllItems();
        List<String> groupList = listGroups();
        for (String group : groupList) {
            groupComboBox.addItem(group);
        }


    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new DeanOffice().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private List<String> listGroups() {
        List<String> groupsList = new ArrayList<>();

        Connection connection = dbConnection.getConnection();

        try {
            CallableStatement cs = connection.prepareCall("{ call ? := SELECT_GROUPS }");
            cs.registerOutParameter(1, OracleTypes.CURSOR);
            cs.execute();

            ResultSet resultSet = (ResultSet)cs.getObject(1);

            while (resultSet.next()) {
                groupsList.add(resultSet.getString("group_name"));
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        return groupsList;
    }
}

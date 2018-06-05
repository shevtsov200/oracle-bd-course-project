package com.project.database;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

class DeanOffice {
    private JTabbedPane tabbedPane;
    private JPanel panel1;
    private JPanel databasePane;
    private JPanel groupsPane;
    private JPanel teacherPanel;
    private JPanel subjectsPanel;
    private JPanel marksPane;

    private static final Map<Integer, String> PANE_TITLES = new HashMap<Integer, String>() {{
        put(0, "Студенты");
        put(1, "Группы");
        put(2,"Преподаватели");
        put(3, "Предметы");
        put(4, "Оценки");
    }};

    public DeanOffice() {
        System.out.println("wtf");
        for (int i = 0; i < tabbedPane.getTabCount(); ++i) {
            tabbedPane.setTitleAt(i, PANE_TITLES.get(i));
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DeanOffice");
        frame.setContentPane(new DeanOffice().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}

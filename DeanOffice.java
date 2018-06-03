package com.project.database;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

class DeanOffice {
    private JTabbedPane tabbedPane;
    private JPanel panel1;
    private JPanel databasePane;

    private static final Map<Integer, String> PANE_TITLES = new HashMap<Integer, String>() {{
        put(0, "Добавить студента");
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

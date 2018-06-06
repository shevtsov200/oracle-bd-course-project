package com.project.database;

import javax.swing.*;
import java.awt.event.ActionListener;

public class ButtonPane extends JPanel {
    private JPanel buttonsPanel;
    private JButton createButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JButton averageButton;

    private static final String ADD_BUTTON_TEXT = "Создать";
    private static final String UPDATE_BUTTON_TEXT = "Обновить";
    private static final String DELETE_BUTTON_TEXT = "Удалить";
    private static final String AVERAGE_MARK_BUTTON_TEXT = "Средний Балл";

    ButtonPane(ActionListener createListener, ActionListener updateListener,
               ActionListener deleteListener, ActionListener averageListener) {
        createButton.setText(ADD_BUTTON_TEXT);
        updateButton.setText(UPDATE_BUTTON_TEXT);
        deleteButton.setText(DELETE_BUTTON_TEXT);
        averageButton.setText(AVERAGE_MARK_BUTTON_TEXT);

        createButton.addActionListener(createListener);
        updateButton.addActionListener(updateListener);
        deleteButton.addActionListener(deleteListener);
        averageButton.addActionListener(averageListener);
    }

    public JPanel getPanel() {
        return buttonsPanel;
    }
}

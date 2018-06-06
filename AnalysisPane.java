package com.project.database;

import oracle.jdbc.OracleTypes;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.SqlDateModel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class AnalysisPane extends JPanel {
    private JPanel analysisPanel;
    private JButton averageMarkButton;
    private JLabel fromLabel;
    private JLabel toLabel;
    private JPanel splitPanel;
    private JDatePickerImpl fromDatePicker;
    private JDatePickerImpl toDatePicker;
    private JTextField markResultTextField;
    private JLabel markResultLabel;
    private final DatabaseConnection dbConnection;

    private String avgFunctionName = null;
    private int selectedId = 0;

    private static final String FROM_TEXT = "С";
    private static final String TO_TEXT = "До";
    private static final String AVERAGE_MARK_TEXT = "Средний Балл";
    private static final String AVERAGE_BUTTON_TEXT = AVERAGE_MARK_TEXT;

    AnalysisPane(String avgFunctionName, int selectedId) {
        this.avgFunctionName = avgFunctionName;
        this.selectedId = selectedId;

        fromLabel.setText(FROM_TEXT);
        toLabel.setText(TO_TEXT);
        averageMarkButton.setText(AVERAGE_BUTTON_TEXT);
        markResultLabel.setText(AVERAGE_MARK_TEXT);

        dbConnection = new DatabaseConnection();

        averageMarkButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {

                SqlParameter[] parameters = new SqlParameter[] {
                        new SqlParameter("",SqlParameter.parameterDirections.OUT, OracleTypes.CURSOR),
                        new SqlParameter(Integer.toString(selectedId), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                        new SqlParameter(getSqlDateString(fromDatePicker), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR),
                        new SqlParameter(getSqlDateString(toDatePicker), SqlParameter.parameterDirections.IN, OracleTypes.VARCHAR)
                };

                try {
                    ResultSet resultSet = dbConnection.executeProcedure(avgFunctionName, parameters);

                    while(resultSet.next()) {
                        Double averageMark = resultSet.getDouble(1);
                        markResultTextField.setText(averageMark.toString());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public JPanel getPanel() {
        return analysisPanel;
    }

    private void createUIComponents() {
        SqlDateModel modelFrom = new SqlDateModel();
        SqlDateModel modelTo = new SqlDateModel();


        Properties properties = new Properties();
        properties.put("test.today", "Today");
        properties.put("text.month", "Month");
        properties.put("text.year", "Year");
        //modelFrom.setDate( 2014, 8, 24 );


        JDatePanelImpl datePanelFrom = new JDatePanelImpl(modelFrom, properties);
        JDatePanelImpl datePanelTo = new JDatePanelImpl(modelTo, properties);
        fromDatePicker = new JDatePickerImpl(datePanelFrom, new net.codejava.swing.DateLabelFormatter());
        toDatePicker = new JDatePickerImpl(datePanelTo, new net.codejava.swing.DateLabelFormatter());
    }

    private String getSqlDateString(JDatePickerImpl datePicker) {
        java.sql.Date date = (java.sql.Date) datePicker.getModel().getValue();
        String dateString = date.toString();

        String formatted_date = null;
        try {
            Date parsed_date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
            formatted_date = new SimpleDateFormat("dd-MMM-yyyy").format(parsed_date);
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
        return formatted_date;
    }
}

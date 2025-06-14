import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import org.jdatepicker.impl.*;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JFormattedTextField.AbstractFormatter;

public class BalanceAppGUI {
    public void showInsertBalanceDialog() {
        // Date Picker Setup
        UtilDateModel model = new UtilDateModel();
        model.setValue(new Date());
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        // Revenue & Expense Fields
        JTextField revenueField = new JTextField();
        JTextField expenseField = new JTextField();

        // Build GUI Panel
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Select Date:"));
        panel.add(datePicker);
        panel.add(new JLabel("Revenue:"));
        panel.add(revenueField);
        panel.add(new JLabel("Expense:"));
        panel.add(expenseField);

        // Show Dialog
        int result = JOptionPane.showConfirmDialog(null, panel, "Insert Balance Entry",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                Date selectedDate = (Date) datePicker.getModel().getValue();
                if (selectedDate == null) throw new Exception("Date is required");

                LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                double revenue = Double.parseDouble(revenueField.getText());
                double expense = Double.parseDouble(expenseField.getText());
                DailyEntry entry = new DailyEntry(date, revenue, expense);
                DatabaseManager db = new DatabaseManager();
                db.insertDailyEntry(entry);
                JOptionPane.showMessageDialog(null, "Entry saved successfully.");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public class DateLabelFormatter extends AbstractFormatter {
        private final String datePattern = "dd.MM.yyyy";
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parse(text);
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime());
            }
            return "";
        }
    }

    public void showMonthlyBalanceTable() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        JComboBox<String> monthCombo = new JComboBox<>(months);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);

// Fix formatting: show integer only (2025 instead of 2.025)
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "####");
        yearSpinner.setEditor(editor);

        JPanel monthPanel = new JPanel(new GridLayout(2, 2));
        monthPanel.add(new JLabel("Select Month:"));
        monthPanel.add(monthCombo);
        monthPanel.add(new JLabel("Select Year:"));
        monthPanel.add(yearSpinner);

        int result = JOptionPane.showConfirmDialog(null, monthPanel, "Select Month and Year",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        int selectedMonth = monthCombo.getSelectedIndex() + 1; // January = 0, so +1 for LocalDate
        int selectedYear = (Integer) yearSpinner.getValue();

        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, 1);

        // Get the table name for this month/year:
        String tableName = DatabaseManager.getMonthlyTableName(selectedDate);

        DatabaseManager db = new DatabaseManager();

        // 'this' refers to your GUI JFrame (used as parent for message dialogs)
        List<DailyEntry> entries = db.getEntriesFromMonthlyTable(tableName, null);

        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No entries found or table '" + tableName + "' does not exist.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. M. yyyy");


        // Prepare data for JTable
        String[] columnNames = {"Date", "Revenue", "Expense", "Profit"};
        Object[][] data = new Object[entries.size()][4];

        for (int i = 0; i < entries.size(); i++) {
            DailyEntry e = entries.get(i);
            String formattedDate = e.getDate().format(formatter);
            data[i][0] = formattedDate;
            data[i][1] = e.getRevenue();
            data[i][2] = e.getExpense();
            data[i][3] = e.getProfit();
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        JOptionPane.showMessageDialog(null, scrollPane,
                "Balance for " + months[selectedMonth - 1] + " " + selectedYear,
                JOptionPane.INFORMATION_MESSAGE);
    }





}

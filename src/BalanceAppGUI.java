import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import org.jdatepicker.impl.*;

import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;
import java.util.Properties;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.stream.IntStream;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.table.DefaultTableCellRenderer;


public class BalanceAppGUI {

    // Shows a dialog for inserting a new balance entry (date, revenue, expense)
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


        JFormattedTextField revenueField = new JFormattedTextField();
        revenueField.setColumns(10);
        JFormattedTextField expenseField = new JFormattedTextField();
        expenseField.setColumns(10);

        // Panel layout
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Insert Daily Balance"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(datePicker, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Revenue (€):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(revenueField, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Expense (€):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(expenseField, gbc);

        // Show dialog
        int result = JOptionPane.showConfirmDialog(
                null,
                inputPanel,
                "Insert Balance Entry",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                Date selectedDate = (Date) datePicker.getModel().getValue();
                if (selectedDate == null) throw new Exception("Date is required");

                LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                String revenueText = revenueField.getText().trim();
                String expenseText = expenseField.getText().trim();
                if (revenueText.isEmpty() || expenseText.isEmpty()) {
                    throw new Exception("Both revenue and expense must be entered.");
                }

                double revenue = Double.parseDouble(revenueText.replace(",", ""));
                double expense = Double.parseDouble(expenseText.replace(",", ""));

                DailyEntry entry = new DailyEntry(date, revenue, expense);
                DatabaseManager db = new DatabaseManager();
                db.insertDailyEntry(entry);

                JOptionPane.showMessageDialog(null, "Entry saved successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // Formatter class to handle display and parsing of date in "dd.MM.yyyy" format
    public class DateLabelFormatter extends AbstractFormatter {
        private final String datePattern = "dd.MM.yyyy";
        private final SimpleDateFormat dateFormatter = new SimpleDateFormat(datePattern);

        @Override
        public Object stringToValue(String text) throws ParseException {
            return dateFormatter.parse(text); // Convert string to Date
        }

        @Override
        public String valueToString(Object value) {
            if (value != null) {
                Calendar cal = (Calendar) value;
                return dateFormatter.format(cal.getTime()); // Convert Date to formatted string
            }
            return "";
        }
    }

    // Displays the monthly balance entries in a table
    public void showMonthlyBalanceTable() {
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        JComboBox<String> monthCombo = new JComboBox<>(months);

        SpinnerNumberModel yearModel = new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
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

        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = (Integer) yearSpinner.getValue();
        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, 1);

        String tableName = DatabaseManager.getMonthlyTableName(selectedDate);
        DatabaseManager db = new DatabaseManager();
        List<DailyEntry> entries = db.getEntriesFromMonthlyTable(tableName, null);

        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No entries found or table '" + tableName + "' does not exist.");
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d. M. yyyy");
        String[] columnNames = {"Date", "Revenue", "Expense", "Profit"};
        Object[][] data = new Object[entries.size()][4];

        for (int i = 0; i < entries.size(); i++) {
            DailyEntry e = entries.get(i);
            data[i][0] = e.getDate().format(formatter);
            data[i][1] = e.getRevenue();
            data[i][2] = e.getExpense();
            data[i][3] = e.getProfit();
        }

        JTable table = new JTable(data, columnNames);

        // Custom cell renderer for Profit column
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (value instanceof Number) {
                    double profit = ((Number) value).doubleValue();
                    if (profit > 0) {
                        c.setForeground(new Color(0, 128, 0)); // Green
                    } else if (profit < 0) {
                        c.setForeground(Color.RED); // Red
                    } else {
                        c.setForeground(Color.BLACK); // Zero
                    }
                } else {
                    c.setForeground(Color.BLACK); // Default
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        JOptionPane.showMessageDialog(null, scrollPane,
                "Balance for " + months[selectedMonth - 1] + " " + selectedYear,
                JOptionPane.INFORMATION_MESSAGE);
    }

    public void showMonthlyProfitSummary() {

        // Month names
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };

        // Create styled combo and spinner
        JComboBox<String> monthCombo = new JComboBox<>(months);
        SpinnerNumberModel yearModel = new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);
        yearSpinner.setEditor(new JSpinner.NumberEditor(yearSpinner, "####"));

        // Build modern input form
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Month:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(monthCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Select Year:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(yearSpinner, gbc);

        int result = JOptionPane.showConfirmDialog(null, inputPanel, "Select Month and Year",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) return;

        // Get selected values
        int selectedMonth = monthCombo.getSelectedIndex() + 1;
        int selectedYear = (Integer) yearSpinner.getValue();
        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, 1);
        String tableName = DatabaseManager.getMonthlyTableName(selectedDate);

        DatabaseManager db = new DatabaseManager();

        if (!db.doesMonthlyTableExist(tableName)) {
            JOptionPane.showMessageDialog(null,
                    "Table for " + months[selectedMonth - 1] + " " + selectedYear + " does not exist.",
                    "Missing Table", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Double totalProfit = db.getTotalProfitFromTable(tableName);

        if (totalProfit != null) {
            // Create a styled label to show the result with color
            JLabel profitLabel = new JLabel();
            profitLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            profitLabel.setHorizontalAlignment(SwingConstants.CENTER);

            String profitColor = totalProfit >= 0 ? "green" : "red";
            String profitText = String.format(
                    "<html><div style='text-align:center;'>Total Profit for <b>%s %d</b><br>" +
                            "<span style='color:%s;'>%.2f €</span></div></html>",
                    months[selectedMonth - 1], selectedYear, profitColor, totalProfit
            );

            profitLabel.setText(profitText);

            // Wrap label in panel for margin
            JPanel profitPanel = new JPanel(new BorderLayout());
            profitPanel.setBorder(BorderFactory.createEmptyBorder(15, 30, 15, 30));
            profitPanel.add(profitLabel, BorderLayout.CENTER);

            JOptionPane.showMessageDialog(null, profitPanel, "Monthly Profit Summary",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    "Could not retrieve profit data for the selected month.",
                    "Data Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void showUpdateDailyEntryDialog() {
        // --- DATE PICKER ---
        UtilDateModel model = new UtilDateModel();
        model.setValue(new Date());
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        // --- INPUT FIELDS ---
        JFormattedTextField revenueField = new JFormattedTextField();
        revenueField.setColumns(10);
        JFormattedTextField expenseField = new JFormattedTextField();
        expenseField.setColumns(10);

        // --- PANEL LAYOUT ---
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Update Daily Entry"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Select Date:"), gbc);
        gbc.gridx = 1;
        panel.add(datePicker, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("New Revenue (€):"), gbc);
        gbc.gridx = 1;
        panel.add(revenueField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("New Expense (€):"), gbc);
        gbc.gridx = 1;
        panel.add(expenseField, gbc);

        // --- SHOW DIALOG ---
        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Update Daily Entry",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            try {
                Date selectedDate = (Date) datePicker.getModel().getValue();
                if (selectedDate == null) throw new Exception("Date is required");

                LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                double revenue = Double.parseDouble(revenueField.getText().trim());
                double expense = Double.parseDouble(expenseField.getText().trim());

                DailyEntry updatedEntry = new DailyEntry(date, revenue, expense);
                DatabaseManager db = new DatabaseManager();
                String tableName = DatabaseManager.getMonthlyTableName(date);

                boolean success = db.updateEntryInMonthlyTable(updatedEntry, tableName);

                if (success) {
                    JOptionPane.showMessageDialog(null, "Entry updated successfully.");
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to update entry. Entry for the date may not exist.", "Update Failed", JOptionPane.WARNING_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error: " + e.getMessage(), "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



}

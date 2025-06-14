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
import java.util.List;
import java.util.ArrayList;
import java.time.format.DateTimeFormatter;
import javax.swing.JFormattedTextField.AbstractFormatter;

public class BalanceAppGUI {

    // Shows a dialog for inserting a new balance entry (date, revenue, expense)
    public void showInsertBalanceDialog() {
        // Setup the date picker component
        UtilDateModel model = new UtilDateModel();
        model.setValue(new Date()); // Default to today's date
        Properties p = new Properties();
        p.put("text.today", "Today");
        p.put("text.month", "Month");
        p.put("text.year", "Year");
        JDatePanelImpl datePanel = new JDatePanelImpl(model, p);
        JDatePickerImpl datePicker = new JDatePickerImpl(datePanel, new DateLabelFormatter());

        // Input fields for revenue and expense
        JTextField revenueField = new JTextField();
        JTextField expenseField = new JTextField();

        // Build the input form
        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Select Date:"));
        panel.add(datePicker);
        panel.add(new JLabel("Revenue:"));
        panel.add(revenueField);
        panel.add(new JLabel("Expense:"));
        panel.add(expenseField);

        // Show confirmation dialog with the input form
        int result = JOptionPane.showConfirmDialog(null, panel, "Insert Balance Entry",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                // Get selected date
                Date selectedDate = (Date) datePicker.getModel().getValue();
                if (selectedDate == null) throw new Exception("Date is required");

                // Convert date to LocalDate
                LocalDate date = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

                // Parse revenue and expense values
                double revenue = Double.parseDouble(revenueField.getText());
                double expense = Double.parseDouble(expenseField.getText());

                // Create entry and insert into DB
                DailyEntry entry = new DailyEntry(date, revenue, expense);
                DatabaseManager db = new DatabaseManager();
                db.insertDailyEntry(entry);

                JOptionPane.showMessageDialog(null, "Entry saved successfully.");
            } catch (Exception e) {
                // Show error if input is invalid
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
        // Month selection dropdown
        String[] months = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        JComboBox<String> monthCombo = new JComboBox<>(months);

        // Year spinner with range 2000–2100
        SpinnerNumberModel yearModel = new SpinnerNumberModel(LocalDate.now().getYear(), 2000, 2100, 1);
        JSpinner yearSpinner = new JSpinner(yearModel);

        // Ensure spinner displays year as full number (e.g., 2025 instead of 2.025)
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "####");
        yearSpinner.setEditor(editor);

        // Create the panel for month/year selection
        JPanel monthPanel = new JPanel(new GridLayout(2, 2));
        monthPanel.add(new JLabel("Select Month:"));
        monthPanel.add(monthCombo);
        monthPanel.add(new JLabel("Select Year:"));
        monthPanel.add(yearSpinner);

        // Show dialog for selecting month and year
        int result = JOptionPane.showConfirmDialog(null, monthPanel, "Select Month and Year",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // Cancel if user pressed Cancel
        if (result != JOptionPane.OK_OPTION) return;

        // Get selected month and year
        int selectedMonth = monthCombo.getSelectedIndex() + 1; // Combo index starts at 0
        int selectedYear = (Integer) yearSpinner.getValue();
        LocalDate selectedDate = LocalDate.of(selectedYear, selectedMonth, 1);

        // Determine the table name for selected month/year
        String tableName = DatabaseManager.getMonthlyTableName(selectedDate);
        DatabaseManager db = new DatabaseManager();

        // Retrieve entries for the selected month
        List<DailyEntry> entries = db.getEntriesFromMonthlyTable(tableName, null);

        if (entries.isEmpty()) {
            // Show message if no data found
            JOptionPane.showMessageDialog(null, "No entries found or table '" + tableName + "' does not exist.");
            return;
        }

        // Format for displaying dates in table
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

        // Create and show table in a scroll pane
        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(true);

        // Display the table in a dialog
        JOptionPane.showMessageDialog(null, scrollPane,
                "Balance for " + months[selectedMonth - 1] + " " + selectedYear,
                JOptionPane.INFORMATION_MESSAGE);
    }
}

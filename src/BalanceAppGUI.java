import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import org.jdatepicker.impl.*;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(datePicker, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Revenue (€):"), gbc);
        gbc.gridx = 1;
        inputPanel.add(revenueField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
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

    // Displays the monthly balance entries in a fullscreen table
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
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));

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

        // Create fullscreen frame
        JFrame frame = new JFrame("Balance for " + months[selectedMonth - 1] + " " + selectedYear);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        frame.add(scrollPane, BorderLayout.CENTER);

        // Add close button at bottom
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.addActionListener(e -> frame.dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }


    // Displays the balance for a given table in fullscreen
    public void showMonthlyBalanceTable2(String tableName) {
        DatabaseManager db = new DatabaseManager();
        List<DailyEntry> entries = db.getEntriesFromMonthlyTable(tableName, null);

        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No entries found or table '" + tableName + "' does not exist.");
            return;
        }

        // Convert "may_25" -> "May 2025"
        String[] parts = tableName.split("_");
        String month = parts[0];
        int yearSuffix = Integer.parseInt(parts[1]);
        int fullYear = 2000 + yearSuffix;
        String formattedMonth = month.substring(0, 1).toUpperCase() + month.substring(1);
        String title = formattedMonth + " " + fullYear;

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
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Custom renderer for Profit column
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

        // Fullscreen frame
        JFrame frame = new JFrame("Balance for " + title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen
        frame.add(scrollPane, BorderLayout.CENTER);

        // Close button at bottom
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.addActionListener(e -> frame.dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
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

        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Select Month:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(monthCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
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


    public void showAvailableMonthsPanel() {
        DatabaseManager db = new DatabaseManager();
        List<String> tables = db.getExistingMonthlyTables();

        if (tables.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No months with data found.");
            return;
        }

        // Convert table names into formatted names for display
        DefaultListModel<String> listModel = new DefaultListModel<>();
        Map<String, String> tableMap = new HashMap<>(); // displayName -> tableName

        for (String table : tables) {
            String formatted = db.formatTableName(table); // e.g. "May 2025"
            listModel.addElement(formatted);
            tableMap.put(formatted, table); // store mapping
        }

        // Create the list
        JList<String> monthList = new JList<>(listModel);
        monthList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(monthList);
        scrollPane.setPreferredSize(new Dimension(250, 200));

        int result = JOptionPane.showConfirmDialog(
                null,
                scrollPane,
                "Available Months",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String selected = monthList.getSelectedValue();
            if (selected != null) {
                String tableName = tableMap.get(selected);
                showMonthlyBalanceTable2(tableName);
            } else {
                JOptionPane.showMessageDialog(null, "Please select a month.");
            }
        }
    }

    public boolean showLoginDialog() {
        JDialog dialog = new JDialog((Frame) null, "Login", true); // modal
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Make full screen
        dialog.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;

        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField(20);
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField(20);

        JButton loginBtn = new JButton("Login");
        JButton addUserBtn = new JButton("Add User");

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(userLabel, gbc);
        gbc.gridx = 1;
        panel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(passLabel, gbc);
        gbc.gridx = 1;
        panel.add(passField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(loginBtn, gbc);
        gbc.gridx = 1;
        panel.add(addUserBtn, gbc);

        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            if (DatabaseManager.validateUser(username, password)) {
                dialog.dispose(); // Close dialog
            } else {
                JOptionPane.showMessageDialog(dialog, "Invalid username or password!");
            }
        });

        addUserBtn.addActionListener(e -> {
            showAddUserDialog(dialog); // Pass current dialog as parent
        });

        dialog.add(panel);
        dialog.setVisible(true);

        // You can track login success with a field instead of always returning true
        return true;
    }

    // Non-static method now
    private void showAddUserDialog(JDialog parent) {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField newUserField = new JTextField();
        JPasswordField newPassField = new JPasswordField();
        JPasswordField confirmPassField = new JPasswordField();

        panel.add(new JLabel("New Username:"));
        panel.add(newUserField);
        panel.add(new JLabel("Password:"));
        panel.add(newPassField);
        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmPassField);

        int result = JOptionPane.showConfirmDialog(parent, panel, "Add New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = newUserField.getText().trim();
            String password = new String(newPassField.getPassword());
            String confirm = new String(confirmPassField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(parent, "Username and password cannot be empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(parent, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (DatabaseManager.addUser(username, password)) {
                JOptionPane.showMessageDialog(parent, "User added successfully!");
            } else {
                JOptionPane.showMessageDialog(parent, "Error adding user (maybe username exists).", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import org.jdatepicker.impl.*;
import java.time.ZoneId;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.border.EmptyBorder;
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

        JPanel monthPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        monthPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        monthPanel.add(new JLabel("Select Month:"));
        monthPanel.add(monthCombo);
        monthPanel.add(new JLabel("Select Year:"));
        monthPanel.add(yearSpinner);

        int result = JOptionPane.showConfirmDialog(
                null,
                monthPanel,
                "Select Month and Year",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

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
        String[] columnNames = {"Date", "Revenue (€)", "Expense (€)", "Profit (€)"};
        Object[][] data = new Object[entries.size()][4];

        DecimalFormat df = new DecimalFormat("#0.00"); // Always 2 decimals

        for (int i = 0; i < entries.size(); i++) {
            DailyEntry e = entries.get(i);
            data[i][0] = e.getDate().format(formatter);
            data[i][1] = df.format(e.getRevenue());
            data[i][2] = df.format(e.getExpense());
            data[i][3] = df.format(e.getProfit());
        }

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Alternate row colors (modern look)
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(250, 250, 250)); // light gray
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                } else {
                    c.setBackground(new Color(200, 220, 255)); // highlight blue
                }

                // Profit column: green/red text
                if (column == 3 && value instanceof String) {
                    try {
                        double profit = Double.parseDouble(((String) value).replace(",", "."));
                        if (profit > 0) {
                            c.setForeground(new Color(0, 128, 0)); // green
                        } else if (profit < 0) {
                            c.setForeground(Color.RED); // red
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } catch (NumberFormatException ex) {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // === Frame ===
        JFrame frame = new JFrame("Balance for " + months[selectedMonth - 1] + " " + selectedYear);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout(20, 20));

        // Add table
        frame.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel with Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setBackground(new Color(66, 133, 244));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        closeBtn.addActionListener(e -> frame.dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
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
        String[] columnNames = {"Date", "Revenue (€)", "Expense (€)", "Profit (€)"};
        Object[][] data = new Object[entries.size()][4];

        DecimalFormat df = new DecimalFormat("#0.00"); // Always 2 decimals

        for (int i = 0; i < entries.size(); i++) {
            DailyEntry e = entries.get(i);
            data[i][0] = e.getDate().format(formatter);
            data[i][1] = df.format(e.getRevenue());
            data[i][2] = df.format(e.getExpense());
            data[i][3] = df.format(e.getProfit());
        }

        JTable table = new JTable(data, columnNames);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 18));
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Zebra rows + profit coloring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? new Color(250, 250, 250) : Color.WHITE);
                } else {
                    c.setBackground(new Color(200, 220, 255)); // light blue
                }

                // Profit column → red/green/black
                if (column == 3 && value instanceof String) {
                    try {
                        double profit = Double.parseDouble(((String) value).replace(",", "."));
                        if (profit > 0) {
                            c.setForeground(new Color(0, 128, 0)); // green
                        } else if (profit < 0) {
                            c.setForeground(Color.RED); // red
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                    } catch (NumberFormatException ex) {
                        c.setForeground(Color.BLACK);
                    }
                } else {
                    c.setForeground(Color.BLACK);
                }

                setHorizontalAlignment(CENTER);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);

        // === Frame ===
        JFrame frame = new JFrame("Balance for " + title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout(20, 20));
        frame.add(scrollPane, BorderLayout.CENTER);

        // Close button
        JButton closeBtn = new JButton("Close");
        closeBtn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        closeBtn.setBackground(new Color(66, 133, 244));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> frame.dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
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

    public String showLoginFrame() {

        final String[] usernameLoggedIn = {null};

        JFrame frame = new JFrame("Login");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        frame.setLayout(new GridBagLayout()); // Center background

        // Background panel
        JPanel background = new JPanel(new GridBagLayout());
        background.setBackground(new Color(245, 245, 245)); // Light gray

        // Card panel (white box with padding + border)
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
                new EmptyBorder(30, 40, 30, 40)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        // Title
        JLabel title = new JLabel("Welcome Back!", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        card.add(title, gbc);

        // Username row
        gbc.gridy++;
        gbc.gridwidth = 1;
        card.add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        JTextField userField = new JTextField(15);
        card.add(userField, gbc);

        // Password row
        gbc.gridy++;
        gbc.gridx = 0;
        card.add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        JPasswordField passField = new JPasswordField(15);
        card.add(passField, gbc);

        // Buttons row
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 0));
        btnPanel.setOpaque(false);

        JButton loginBtn = new JButton("Login");
        JButton addUserBtn = new JButton("Add User");

        Dimension btnSize = new Dimension(110, 40);
        loginBtn.setPreferredSize(btnSize);
        addUserBtn.setPreferredSize(btnSize);

        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addUserBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Style Login button (blue)
        loginBtn.setBackground(new Color(66, 133, 244));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFocusPainted(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setOpaque(true);
        loginBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Style Add User button (gray)
        addUserBtn.setBackground(new Color(155, 155, 155));
        addUserBtn.setForeground(Color.WHITE);
        addUserBtn.setFocusPainted(false);
        addUserBtn.setBorderPainted(false);
        addUserBtn.setOpaque(true);
        addUserBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnPanel.add(loginBtn);
        btnPanel.add(addUserBtn);
        card.add(btnPanel, gbc);

        // Add card to background
        background.add(card);

        frame.add(background);
        frame.setVisible(true);


        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            if (DatabaseManager.validateUser(username, password)) {
                usernameLoggedIn[0] = username; // store username
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame, "Invalid username or password!");
            }
        });

        // Add user button
        addUserBtn.addActionListener(e -> showAddUserDialog(frame));

        // Wait until frame is disposed
        while (frame.isDisplayable()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }

        return usernameLoggedIn[0];
    }



    private void showAddUserDialog(JFrame parent) {
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

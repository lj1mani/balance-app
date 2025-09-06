import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class DatabaseManager {

    private static final String URL;
    private static final String USER;
    private static final String PASSWORD;

    // Load database config from db.properties
    static {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
            URL = props.getProperty("DB_URL");
            USER = props.getProperty("DB_USER");
            PASSWORD = props.getProperty("DB_PASSWORD");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not load database configuration. Please check db.properties Or Missing db.properties file! Please copy db.properties.example and configure it.");
        }
    }


    // Returns a connection to the database
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Ensures the monthly table exists in the database (creates it if it doesn't)
    public void ensureMonthlyTable(LocalDate date) {
        String tableName = formatMonthName(date); // Format table name based on current date
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "entry_date DATE NOT NULL UNIQUE, "
                + "revenue DECIMAL(10,2), "
                + "expense DECIMAL(10,2), "
                + "profit DECIMAL(10,2) GENERATED ALWAYS AS (revenue - expense) STORED"
                + ")";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql); // Execute the SQL to create table if it doesn't exist
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Formats the table name as "month_yy" (e.g., june_25)
    public String formatMonthName(LocalDate date) {
        String month = date.getMonth().toString().toLowerCase(); // Convert month to lowercase
        int year = date.getYear() % 100; // Get last 2 digits of year
        return month + "_" + year;
    }

    // Static version of table name formatter
    public static String getMonthlyTableName(LocalDate date) {
        String month = date.getMonth().toString().toLowerCase();  // e.g., "june"
        String yearTwoDigits = String.valueOf(date.getYear()).substring(2); // Get "25" from "2025"
        return month + "_" + yearTwoDigits;  // Return table name like "june_25"
    }

    // Inserts or updates a daily entry in the monthly table
    public void insertDailyEntry(DailyEntry entry) {

        ensureMonthlyTable(entry.getDate()); // Make sure table for this month exists
        String tableName = formatMonthName(entry.getDate());
        //doesMonthlyTableExist(tableName);

        // SQL with ON DUPLICATE KEY UPDATE ensures only one entry per date
        String sql = "INSERT INTO " + tableName + " (entry_date, revenue, expense) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE revenue = VALUES(revenue), expense = VALUES(expense)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(entry.getDate()));
            stmt.setDouble(2, entry.getRevenue());
            stmt.setDouble(3, entry.getExpense());
            stmt.executeUpdate(); // Insert or update the entry
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean insertDailyEntry2(DailyEntry entry) {
        try {
            return true;  // success
        } catch (Exception e) {
            e.printStackTrace();
            return false;  // failed
        }
    }

    // Retrieves all entries from a specific monthly table
    public List<DailyEntry> getEntriesFromMonthlyTable(String tableName, Component parent) {
        List<DailyEntry> entries = new ArrayList<>();

        // Show warning if the table does not exist
        if (!doesMonthlyTableExist(tableName)) {
            JOptionPane.showMessageDialog(parent, "Table '" + tableName + "' does not exist.",
                    "Missing Table", JOptionPane.WARNING_MESSAGE);
            return entries;
        }

        String sql = "SELECT * FROM " + tableName + " ORDER BY entry_date ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Loop through result set and create DailyEntry objects
            while (rs.next()) {
                LocalDate date = rs.getDate("entry_date").toLocalDate();
                double revenue = rs.getDouble("revenue");
                double expense = rs.getDouble("expense");

                DailyEntry entry = new DailyEntry(date, revenue, expense);
                entries.add(entry); // Add to list
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Database error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return entries;
    }

    // Checks if a specific monthly table exists in the database
    public boolean doesMonthlyTableExist(String tableName) {
        String sql = "SHOW TABLES LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName); // Bind table name
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Return true if table exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Double getTotalProfitFromTable(String tableName) {
        String sql = "SELECT SUM(profit) AS total_profit FROM " + tableName;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble("total_profit");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // return null if error or no result
    }

    // Fetch daily entry by date
    public DailyEntry getDailyEntry(LocalDate date) {

        String tableName = formatMonthName(LocalDate.now()); // Format table name based on current date
        String sql = "SELECT revenue, expense FROM" + tableName + "WHERE date = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double revenue = rs.getDouble("revenue");
                double expense = rs.getDouble("expense");
                return new DailyEntry(date, revenue, expense);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // No entry found or error
    }

    // Update existing daily entry
    public boolean updateEntryInMonthlyTable(DailyEntry entry, String tableName) {
        String sql = "UPDATE " + tableName + " SET revenue = ?, expense = ? WHERE entry_date = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, entry.getRevenue());
            stmt.setDouble(2, entry.getExpense());
            stmt.setDate(3, Date.valueOf(entry.getDate()));

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

        public String formatTableName(String tableName) {
        try {
            // Split "may_25" into ["may", "25"]
            String[] parts = tableName.split("_");
            String monthName = parts[0];
            int yearSuffix = Integer.parseInt(parts[1]);

            // Convert suffix into full year (assume 2000+)
            int year = 2000 + yearSuffix;

            // Capitalize month
            String formattedMonth = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

            return formattedMonth + " " + year;
        } catch (Exception e) {
            return tableName; // fallback if parsing fails
        }
    }

    public List<String> getExistingMonthlyTables() {
        List<String> tables = new ArrayList<>();
        String sql = "SHOW TABLES";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String tableName = rs.getString(1);
                if (tableName.matches("^[a-z]+_\\d{2}$")) { // matches may_25, june_25
                    tables.add(tableName);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tables;
    }
}



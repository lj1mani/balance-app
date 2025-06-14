import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:mariadb://localhost:3306/balance_db";
    private static final String USER = "root";
    private static final String PASSWORD = "database";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    public void ensureMonthlyTable(LocalDate date) {
        String tableName = formatMonthName(LocalDate.now());
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName + " ("
                + "id INT AUTO_INCREMENT PRIMARY KEY, "
                + "entry_date DATE NOT NULL UNIQUE, "
                + "revenue DECIMAL(10,2), "
                + "expense DECIMAL(10,2), "
                + "profit DECIMAL(10,2) GENERATED ALWAYS AS (revenue - expense) STORED"
                + ")";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String formatMonthName(LocalDate date) {
        String month = date.getMonth().toString().toLowerCase(); // e.g. "MAY" → "may"
        int year = date.getYear() % 100; // e.g. 2025 → 25
        return month + "_" + year;
    }


    public static String getMonthlyTableName(LocalDate date) {
        String month = date.getMonth().toString().toLowerCase();  // e.g., "june"
        String yearTwoDigits = String.valueOf(date.getYear()).substring(2); // last two digits, e.g., "25"
        return month + "_" + yearTwoDigits;  // e.g., "june_25"
    }

    public void insertDailyEntry(DailyEntry entry) {
        ensureMonthlyTable(entry.getDate());
        String tableName = formatMonthName(entry.getDate());

        String sql = "INSERT INTO " + tableName + " (entry_date, revenue, expense) " +
                "VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE revenue = VALUES(revenue), expense = VALUES(expense)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(entry.getDate()));
            stmt.setDouble(2, entry.getRevenue());
            stmt.setDouble(3, entry.getExpense());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<DailyEntry> getEntriesFromMonthlyTable(String tableName, Component parent) {
        List<DailyEntry> entries = new ArrayList<>();

        // Check if table exists
        if (!doesMonthlyTableExist(tableName)) {
            JOptionPane.showMessageDialog(parent, "Table '" + tableName + "' does not exist.",
                    "Missing Table", JOptionPane.WARNING_MESSAGE);
            return entries;
        }

        String sql = "SELECT * FROM " + tableName + " ORDER BY entry_date ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                LocalDate date = rs.getDate("entry_date").toLocalDate();
                double revenue = rs.getDouble("revenue");
                double expense = rs.getDouble("expense");

                DailyEntry entry = new DailyEntry(date, revenue, expense);
                entries.add(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "Database error: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }

        return entries;
    }

    public boolean doesMonthlyTableExist(String tableName) {
        String sql = "SHOW TABLES LIKE ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // true if table exists
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}

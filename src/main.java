import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

import java.awt.*;

public class main {
    public static void main(String[] args) {
        // Apply FlatLaf theme
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf.");
        }

        // Create your BalanceAppGUI instance
        BalanceAppGUI balanceGUI = new BalanceAppGUI();

        // Keep asking for login until successful or cancelled
        boolean loggedIn = false;
        while (!loggedIn) {
            loggedIn = balanceGUI.showLoginDialog();
            if (!loggedIn) {
                int choice = JOptionPane.showConfirmDialog(null, "Try again?", "Login", JOptionPane.YES_NO_OPTION);
                if (choice != JOptionPane.YES_OPTION) {
                    System.exit(0); // Exit if user chooses No
                }
            }
        }


        // Create main frame
        JFrame frame = new JFrame("Balance App Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make it fullscreen
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false); // keep window controls, set true for pure fullscreen

        // Create panel with GridLayout for buttons
        JPanel panel = new JPanel(new GridLayout(0, 1, 20, 20)); // vertical list of buttons
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        // Menu options
        String[] options = {"Insert daily", "SUM for month", "Show balance", "Update", "All months", "Exit"};

        for (String option : options) {
            JButton button = new JButton(option);
            button.setFont(new Font("Segoe UI", Font.BOLD, 18));
            button.setFocusPainted(false);

            // Action listeners
            button.addActionListener(e -> {
                switch (option) {
                    case "Insert daily":
                        balanceGUI.showInsertBalanceDialog();
                        break;
                    case "SUM for month":
                        balanceGUI.showMonthlyProfitSummary();
                        break;
                    case "Show balance":
                        balanceGUI.showMonthlyBalanceTable();
                        break;
                    case "Update":
                        balanceGUI.showUpdateDailyEntryDialog();
                        break;
                    case "All months":
                        balanceGUI.showAvailableMonthsPanel();
                        break;
                    case "Exit":
                        JOptionPane.showMessageDialog(frame, "Goodbye!");
                        System.exit(0);
                        break;
                }
            });

            panel.add(button);
        }

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}


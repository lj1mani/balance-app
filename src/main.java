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

        BalanceAppGUI balanceGUI = new BalanceAppGUI();

        while (true) {
            // 1. Show login frame
            boolean loggedIn = balanceGUI.showLoginFrame();

            if (!loggedIn) {
                // User pressed cancel/closed window
                System.exit(0);
            }

            // 2. Show main menu after successful login
            boolean logout = showMainMenu(balanceGUI);

            if (!logout) {
                // Exit chosen
                System.exit(0);
            }
            // else -> loop restarts and login is shown again
        }
    }

    // Show login loop until success or exit
    private static boolean showLoginLoop(BalanceAppGUI balanceGUI) {
        boolean loggedIn = false;
        while (!loggedIn) {
            loggedIn = balanceGUI.showLoginFrame();
            if (!loggedIn) {
                //int choice = JOptionPane.showConfirmDialog(null, "Try again?", "Login", JOptionPane.YES_NO_OPTION);
                //if (choice != JOptionPane.YES_OPTION) {
                    return false; // Exit app if user chooses No
                //}
            }
        }
        return true;
    }

    // Show the main menu
    private static boolean showMainMenu(BalanceAppGUI balanceGUI) {
        JFrame frame = new JFrame("Balance App Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Make it fullscreen
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false);

        JPanel panel = new JPanel(new GridLayout(0, 1, 20, 20)); // vertical list
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        String[] options = {"Insert daily", "SUM for month", "Show balance", "Update", "All months", "Logout"};

        for (String option : options) {
            JButton button = new JButton(option);
            button.setFont(new Font("Segoe UI", Font.BOLD, 18));

            final boolean[] logout = {false};

            button.setFocusPainted(false);

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
                    case "Logout":
                        logout[0] = true;
                        frame.dispose();
                        break;
                }
            });

            panel.add(button);
        }

        frame.add(panel, BorderLayout.CENTER);
        frame.setVisible(true);

        while (frame.isDisplayable()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {}
        }

        return true;

    }
}

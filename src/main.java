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
            String username = balanceGUI.showLoginFrame();

            if (username == null) {
                // User pressed cancel or login failed
                System.exit(0);
            }

            boolean logout = showMainMenu(balanceGUI, username);

            if (!logout) {
                System.exit(0);
            }
        }
    }

    // Show login loop until success or exit
    private static String showLoginLoop(BalanceAppGUI balanceGUI) {
        String username = null;

        while (username == null) {
            username = balanceGUI.showLoginFrame(); // now returns String

            if (username == null) {
                return null; // Exit app if user chooses No
            }
        }

        return username; // successful login
    }

    // Show the main menu
    private static boolean showMainMenu(BalanceAppGUI balanceGUI, String username) {
        JFrame frame = new JFrame("Balance App Menu");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setUndecorated(false);

        JPanel panel = new JPanel(new GridLayout(0, 1, 20, 20));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JPanel userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(new Color(240, 240, 240)); // light gray background
        userPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JLabel userLabel = new JLabel("Logged in as: " + username, SwingConstants.CENTER);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        userLabel.setForeground(new Color(66, 133, 244)); // modern blue
        userPanel.add(userLabel, BorderLayout.CENTER);

// Add the panel to the top of the main layout
        frame.add(userPanel, BorderLayout.NORTH);

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

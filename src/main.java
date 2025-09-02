import javax.swing.*;
import com.formdev.flatlaf.FlatLightLaf;

public class main {
    public static void main(String[] args) {

        BalanceAppGUI balanceGUI = new BalanceAppGUI();

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf.");
        }

        // Menu options for the user to choose from
        String[] options = {"Insert daily", "SUM for month", "Show balance", "Update", "All months"};

        // Infinite loop to keep showing the menu until user chooses "Exit"
        while (true) {

            // Show a dialog with options as buttons
            int choice = JOptionPane.showOptionDialog(
                    null,               // Parent component (null means center on screen)
                    null,                     // Message (none in this case)
                    "Balance app menu",        // Title of the dialog
                    JOptionPane.DEFAULT_OPTION, // Type of options
                    JOptionPane.PLAIN_MESSAGE,  // No icon
                    null,                  // No custom icon
                    options,                    // Options displayed as buttons
                    options[0]                  // Default selected option
            );

            // If user closes the dialog
            if (choice == -1) {
                JOptionPane.showMessageDialog(null, "Goodbye!"); // Say goodbye
                break;
            }

            // Handle user's choice using switch statement
            switch (choice) {
                case 0:
                    balanceGUI.showInsertBalanceDialog();
                    break;

                case 1:
                    balanceGUI.showMonthlyProfitSummary();
                    break;

                case 2:
                    balanceGUI.showMonthlyBalanceTable();
                    break;

                case 3:
                    balanceGUI.showUpdateDailyEntryDialog();
                    break;

                case 4:
                    balanceGUI.showAvailableMonthsPanel();
                    break;

                default:
                    break;
            }
        }
    }
}


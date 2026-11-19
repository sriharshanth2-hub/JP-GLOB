import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

// Main class providing the interactive Console User Interface for the Banking System.
// Handles user input via Scanner, menu navigation, input validation, and background auto-saving.
public class ConsoleMain {
    // Path to the persistence CSV file
    private static final String DATA_FILE = "data.csv";

    // Shared Bank instance
    private static Bank bank = new Bank();

    // Flag controlling the background auto-save thread
    private static volatile boolean isRunning = true;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=================================================");
        System.out.println("       WELCOME TO THE CORE JAVA BANKING SYSTEM   ");
        System.out.println("=================================================");

        // STEP 6: Load existing data on program startup
        try {
            bank.load(DATA_FILE);
        } catch (IOException e) {
            System.out.println("[Warning] Failed to load existing data: " + e.getMessage());
        }

        // STEP 7.5: Multithreading - Background Auto-Save Thread
        // Runs every 30 seconds to automatically persist data to disk.
        Thread autoSaveThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(30000); // Sleep for 30 seconds
                        if (isRunning) {
                            bank.save(DATA_FILE);
                            // Background log message (useful for demonstrative viva inspection)
                            // System.out.println("\n[Auto-Save] Background auto-save completed.");
                        }
                    } catch (InterruptedException e) {
                        // Thread interrupted on shutdown - exit cleanly
                        break;
                    } catch (IOException e) {
                        System.out.println("\n[Auto-Save Error] " + e.getMessage());
                    }
                }
            }
        }, "AutoSave-Thread");

        // Set as daemon so it does not prevent JVM shutdown if unexpected termination occurs
        autoSaveThread.setDaemon(true);
        autoSaveThread.start();

        // Main interaction menu loop
        while (isRunning) {
            printMenu();
            System.out.print("Enter your choice (1-9): ");
            String choiceStr = scanner.nextLine().trim();

            if (choiceStr.isEmpty()) {
                System.out.println("[Error] Input cannot be empty. Please enter a choice between 1 and 9.\n");
                continue;
            }

            int choice;
            try {
                choice = Integer.parseInt(choiceStr);
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid input '" + choiceStr + "'. Please enter a valid number (1-9).\n");
                continue;
            }

            try {
                switch (choice) {
                    case 1:
                        handleCreateAccount(scanner);
                        break;
                    case 2:
                        handleDeposit(scanner);
                        break;
                    case 3:
                        handleWithdraw(scanner);
                        break;
                    case 4:
                        handleTransfer(scanner);
                        break;
                    case 5:
                        handleCheckBalance(scanner);
                        break;
                    case 6:
                        handleMiniStatement(scanner);
                        break;
                    case 7:
                        handleApplyInterest();
                        break;
                    case 8:
                        handleDeleteAccount(scanner);
                        break;
                    case 9:
                        handleExitAndSave(autoSaveThread);
                        break;
                    default:
                        System.out.println("[Error] Invalid choice " + choice + ". Please select an option from 1 to 9.\n");
                }
            } catch (AccountNotFoundException e) {
                System.out.println("[Bank Error] Account Not Found: " + e.getMessage() + "\n");
            } catch (InsufficientBalanceException e) {
                System.out.println("[Bank Error] Insufficient Funds: " + e.getMessage() + "\n");
            } catch (InvalidAmountException e) {
                System.out.println("[Bank Error] Invalid Amount: " + e.getMessage() + "\n");
            } catch (IllegalArgumentException e) {
                System.out.println("[Validation Error] " + e.getMessage() + "\n");
            } catch (Exception e) {
                System.out.println("[Unexpected Error] " + e.getMessage() + "\n");
            }
        }

        scanner.close();
    }

    // Displays the main console menu options
    private static void printMenu() {
        System.out.println("-------------------------------------------------");
        System.out.println("                   MAIN MENU                     ");
        System.out.println("-------------------------------------------------");
        System.out.println(" 1. Create Account");
        System.out.println(" 2. Deposit Funds");
        System.out.println(" 3. Withdraw Funds");
        System.out.println(" 4. Transfer Funds");
        System.out.println(" 5. Check Balance");
        System.out.println(" 6. Mini Statement (Last 5 Transactions)");
        System.out.println(" 7. Apply Interest (Savings Accounts)");
        System.out.println(" 8. Delete Account");
        System.out.println(" 9. Exit and Save");
        System.out.println("-------------------------------------------------");
    }

    // Handles creation of a new bank account
    private static void handleCreateAccount(Scanner scanner)
            throws InvalidAmountException, InsufficientBalanceException {
        System.out.println("\n--- Create New Account ---");
        System.out.print("Enter Account Holder Name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be empty.");
        }

        System.out.print("Select Account Type (1 for Savings, 2 for Current): ");
        String typeChoice = scanner.nextLine().trim();
        String type;
        if (typeChoice.equals("1") || typeChoice.equalsIgnoreCase("Savings")) {
            type = "Savings";
        } else if (typeChoice.equals("2") || typeChoice.equalsIgnoreCase("Current")) {
            type = "Current";
        } else {
            throw new IllegalArgumentException("Invalid account type selection. Enter 1 for Savings or 2 for Current.");
        }

        System.out.print("Enter Initial Deposit Amount (Rs.): ");
        double initialDeposit = parseDoubleInput(scanner.nextLine().trim());

        String newAccNum = bank.createAccount(name, type, initialDeposit);
        System.out.printf("[Success] %s Account created successfully! Account Number: %s | Balance: Rs. %.2f\n\n",
                type, newAccNum, initialDeposit);
    }

    // Handles deposit into an existing account
    private static void handleDeposit(Scanner scanner)
            throws AccountNotFoundException, InvalidAmountException {
        System.out.println("\n--- Deposit Funds ---");
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();
        if (accNum.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }

        System.out.print("Enter Deposit Amount (Rs.): ");
        double amount = parseDoubleInput(scanner.nextLine().trim());

        bank.deposit(accNum, amount);
        System.out.printf("[Success] Deposited Rs. %.2f into %s. New Balance: Rs. %.2f\n\n",
                amount, accNum.toUpperCase(), bank.getBalance(accNum));
    }

    // Handles withdrawal from an existing account
    private static void handleWithdraw(Scanner scanner)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {
        System.out.println("\n--- Withdraw Funds ---");
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();
        if (accNum.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }

        System.out.print("Enter Withdrawal Amount (Rs.): ");
        double amount = parseDoubleInput(scanner.nextLine().trim());

        bank.withdraw(accNum, amount);
        System.out.printf("[Success] Withdrew Rs. %.2f from %s. New Balance: Rs. %.2f\n\n",
                amount, accNum.toUpperCase(), bank.getBalance(accNum));
    }

    // Handles transferring money between two accounts
    private static void handleTransfer(Scanner scanner)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {
        System.out.println("\n--- Transfer Funds ---");
        System.out.print("Enter Sender Account Number: ");
        String fromAcc = scanner.nextLine().trim();
        if (fromAcc.isEmpty()) {
            throw new IllegalArgumentException("Sender account number cannot be empty.");
        }

        System.out.print("Enter Receiver Account Number: ");
        String toAcc = scanner.nextLine().trim();
        if (toAcc.isEmpty()) {
            throw new IllegalArgumentException("Receiver account number cannot be empty.");
        }

        System.out.print("Enter Transfer Amount (Rs.): ");
        double amount = parseDoubleInput(scanner.nextLine().trim());

        bank.transfer(fromAcc, toAcc, amount);
        System.out.printf("[Success] Transferred Rs. %.2f from %s to %s.\n",
                amount, fromAcc.toUpperCase(), toAcc.toUpperCase());
        System.out.printf("Sender (%s) New Balance: Rs. %.2f\n\n", fromAcc.toUpperCase(), bank.getBalance(fromAcc));
    }

    // Handles checking balance of an account
    private static void handleCheckBalance(Scanner scanner) throws AccountNotFoundException {
        System.out.println("\n--- Check Account Balance ---");
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();
        if (accNum.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }

        Account acc = bank.findAccount(accNum);
        System.out.println("-------------------------------------------------");
        System.out.printf(" Account Number : %s\n", acc.getAccountNumber());
        System.out.printf(" Holder Name    : %s\n", acc.getHolderName());
        System.out.printf(" Account Type   : %s\n", acc.getAccountType());
        System.out.printf(" Current Balance: Rs. %.2f\n", acc.getBalance());
        System.out.println("-------------------------------------------------\n");
    }

    // Handles fetching and displaying mini-statement (last 5 transactions)
    private static void handleMiniStatement(Scanner scanner) throws AccountNotFoundException {
        System.out.println("\n--- Mini Statement ---");
        System.out.print("Enter Account Number: ");
        String accNum = scanner.nextLine().trim();
        if (accNum.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }

        Account acc = bank.findAccount(accNum);
        ArrayList<Transaction> transactions = bank.getMiniStatement(accNum);

        System.out.println("-------------------------------------------------------------------------");
        System.out.printf(" MINI STATEMENT FOR ACC: %s (%s - %s)\n",
                acc.getAccountNumber(), acc.getHolderName(), acc.getAccountType());
        System.out.println("-------------------------------------------------------------------------");
        System.out.printf("%-19s | %-12s | %-13s | %s\n", "Date & Time", "Type", "Amount", "Balance After");
        System.out.println("-------------------------------------------------------------------------");

        if (transactions.isEmpty()) {
            System.out.println(" No transactions found for this account.");
        } else {
            for (Transaction tx : transactions) {
                System.out.println(tx);
            }
        }
        System.out.println("-------------------------------------------------------------------------\n");
    }

    // Handles applying interest polymorphically across accounts
    private static void handleApplyInterest() {
        System.out.println("\n--- Apply Annual Interest ---");
        int count = bank.applyInterest();
        System.out.printf("[Success] Applied 4%% interest to %d Savings account(s).\n\n", count);
    }

    // Handles closing/deleting an account
    private static void handleDeleteAccount(Scanner scanner) throws AccountNotFoundException {
        System.out.println("\n--- Delete Account ---");
        System.out.print("Enter Account Number to Delete: ");
        String accNum = scanner.nextLine().trim();
        if (accNum.isEmpty()) {
            throw new IllegalArgumentException("Account number cannot be empty.");
        }

        Account acc = bank.findAccount(accNum);
        System.out.printf("Are you sure you want to delete account %s (%s)? (yes/no): ",
                acc.getAccountNumber(), acc.getHolderName());
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("yes") || confirm.equalsIgnoreCase("y")) {
            bank.deleteAccount(accNum);
            System.out.printf("[Success] Account %s deleted successfully.\n\n", accNum.toUpperCase());
        } else {
            System.out.println("[Cancelled] Account deletion cancelled.\n");
        }
    }

    // Handles graceful exit, stopping background thread and saving data
    private static void handleExitAndSave(Thread autoSaveThread) {
        System.out.println("\nShutting down Banking System...");
        isRunning = false;

        // Gracefully stop background auto-save thread
        if (autoSaveThread != null && autoSaveThread.isAlive()) {
            autoSaveThread.interrupt();
            try {
                autoSaveThread.join(1000); // Wait up to 1 second for thread to terminate cleanly
            } catch (InterruptedException ignored) {}
        }

        // Save data to CSV file
        try {
            bank.save(DATA_FILE);
            System.out.println("[Success] All banking data successfully saved to '" + DATA_FILE + "'.");
        } catch (IOException e) {
            System.out.println("[Error] Failed to save data during shutdown: " + e.getMessage());
        }

        System.out.println("Thank you for using Banking System. Goodbye!");
    }

    // Helper method to safely parse double inputs and reject non-numeric values
    private static double parseDoubleInput(String input) throws InvalidAmountException {
        if (input == null || input.trim().isEmpty()) {
            throw new InvalidAmountException("Amount cannot be empty. Please enter a valid number.");
        }
        try {
            return Double.parseDouble(input.trim());
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Invalid numeric value '" + input + "'. Please enter numbers only.");
        }
    }
}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Bank class managing accounts, transactions, and persistence.
// Uses Java Collections (HashMap, ArrayList) and File I/O (BufferedReader, BufferedWriter).
public class Bank {
    // HashMap storing accounts keyed by account number (O(1) lookup)
    private HashMap<String, Account> accounts;

    // HashMap storing transaction history lists keyed by account number
    private HashMap<String, ArrayList<Transaction>> history;

    // Counter used to auto-generate unique account numbers (e.g., ACC1001)
    private int nextAccountCounter = 1001;

    // Default constructor
    public Bank() {
        this.accounts = new HashMap<>();
        this.history = new HashMap<>();
    }

    // Auto-generates the next sequential account number
    private synchronized String generateAccountNumber() {
        return "ACC" + (nextAccountCounter++);
    }

    // Creates a new account (Savings or Current) and records an opening transaction
    public synchronized String createAccount(String holderName, String type, double initialBalance)
            throws InvalidAmountException, InsufficientBalanceException {
        if (holderName == null || holderName.trim().isEmpty()) {
            throw new IllegalArgumentException("Account holder name cannot be empty.");
        }

        String accNum = generateAccountNumber();
        Account newAccount;

        if (type.equalsIgnoreCase("Savings")) {
            if (initialBalance < SavingsAccount.MINIMUM_BALANCE) {
                throw new InsufficientBalanceException(
                    String.format("Initial deposit for Savings Account must be at least Rs. %.2f", SavingsAccount.MINIMUM_BALANCE)
                );
            }
            newAccount = new SavingsAccount(accNum, holderName.trim(), initialBalance);
        } else if (type.equalsIgnoreCase("Current")) {
            if (initialBalance < 0) {
                throw new InvalidAmountException("Initial deposit for Current Account cannot be negative.");
            }
            newAccount = new CurrentAccount(accNum, holderName.trim(), initialBalance);
        } else {
            throw new IllegalArgumentException("Invalid account type: " + type + ". Must be 'Savings' or 'Current'.");
        }

        accounts.put(accNum, newAccount);
        ArrayList<Transaction> txList = new ArrayList<>();
        if (initialBalance > 0) {
            txList.add(new Transaction("OPENING_DEP", initialBalance, initialBalance));
        }
        history.put(accNum, txList);

        return accNum;
    }

    // Finds an account by account number, throws AccountNotFoundException if not found
    public Account findAccount(String accountNumber) throws AccountNotFoundException {
        if (accountNumber == null || !accounts.containsKey(accountNumber.trim().toUpperCase())) {
            throw new AccountNotFoundException("Account '" + accountNumber + "' not found.");
        }
        return accounts.get(accountNumber.trim().toUpperCase());
    }

    // Deposits funds into the given account and records transaction in history
    public synchronized void deposit(String accountNumber, double amount)
            throws AccountNotFoundException, InvalidAmountException {
        Account acc = findAccount(accountNumber);
        acc.deposit(amount);
        history.get(acc.getAccountNumber()).add(new Transaction("DEPOSIT", amount, acc.getBalance()));
    }

    // Withdraws funds from the given account and records transaction in history
    public synchronized void withdraw(String accountNumber, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {
        Account acc = findAccount(accountNumber);
        acc.withdraw(amount);
        history.get(acc.getAccountNumber()).add(new Transaction("WITHDRAW", amount, acc.getBalance()));
    }

    // Transfers funds from one account to another
    public synchronized void transfer(String fromAccNum, String toAccNum, double amount)
            throws AccountNotFoundException, InvalidAmountException, InsufficientBalanceException {
        if (fromAccNum == null || toAccNum == null) {
            throw new IllegalArgumentException("Account numbers cannot be null.");
        }
        String fromClean = fromAccNum.trim().toUpperCase();
        String toClean = toAccNum.trim().toUpperCase();

        if (fromClean.equalsIgnoreCase(toClean)) {
            throw new InvalidAmountException("Sender and receiver accounts must be different.");
        }

        // Validate both accounts exist before initiating transfer
        Account sender = findAccount(fromClean);
        Account receiver = findAccount(toClean);

        // Perform debit and credit
        sender.withdraw(amount);
        receiver.deposit(amount);

        // Record respective transactions in history
        history.get(fromClean).add(new Transaction("TRANSFER_OUT", amount, sender.getBalance()));
        history.get(toClean).add(new Transaction("TRANSFER_IN", amount, receiver.getBalance()));
    }

    // Retrieves current balance of an account
    public double getBalance(String accountNumber) throws AccountNotFoundException {
        Account acc = findAccount(accountNumber);
        return acc.getBalance();
    }

    // Retrieves the last 5 transactions for an account (Mini Statement)
    public ArrayList<Transaction> getMiniStatement(String accountNumber) throws AccountNotFoundException {
        Account acc = findAccount(accountNumber);
        ArrayList<Transaction> list = history.getOrDefault(acc.getAccountNumber(), new ArrayList<>());

        int total = list.size();
        int startIndex = Math.max(0, total - 5);
        ArrayList<Transaction> miniStatement = new ArrayList<>();
        for (int i = startIndex; i < total; i++) {
            miniStatement.add(list.get(i));
        }
        return miniStatement;
    }

    // Applies interest polymorphically: calls calculateInterest() on each account.
    // For SavingsAccount, returns 4% interest; for CurrentAccount, returns 0.0.
    public synchronized int applyInterest() {
        int appliedCount = 0;
        for (Account acc : accounts.values()) {
            // Polymorphic method call: dynamic binding executes subclass implementation
            double interest = acc.calculateInterest();
            if (interest > 0) {
                try {
                    acc.deposit(interest);
                    history.get(acc.getAccountNumber()).add(new Transaction("INTEREST", interest, acc.getBalance()));
                    appliedCount++;
                } catch (InvalidAmountException e) {
                    // Won't occur since interest > 0
                }
            }
        }
        return appliedCount;
    }

    // Deletes an account and its associated transaction history
    public synchronized boolean deleteAccount(String accountNumber) throws AccountNotFoundException {
        Account acc = findAccount(accountNumber);
        accounts.remove(acc.getAccountNumber());
        history.remove(acc.getAccountNumber());
        return true;
    }

    // Returns a map of all accounts (for reporting/display)
    public Map<String, Account> getAllAccounts() {
        return new HashMap<>(accounts);
    }

    // ==========================================
    // STEP 6: File Handling (save & load)
    // ==========================================

    // Saves all accounts and transactions into a CSV file using BufferedWriter
    public synchronized void save(String filePath) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Section 1: Accounts
            writer.write("#ACCOUNTS");
            writer.newLine();
            for (Account acc : accounts.values()) {
                // CSV line: accountNumber,holderName,type,balance
                writer.write(String.format("%s,%s,%s,%.2f",
                        acc.getAccountNumber(),
                        acc.getHolderName(),
                        acc.getAccountType(),
                        acc.getBalance()));
                writer.newLine();
            }

            // Section 2: Transactions
            writer.write("#TRANSACTIONS");
            writer.newLine();
            for (Map.Entry<String, ArrayList<Transaction>> entry : history.entrySet()) {
                String accNum = entry.getKey();
                for (Transaction tx : entry.getValue()) {
                    // CSV line: accountNumber,type,amount,dateTime,balanceAfter
                    writer.write(accNum + "," + tx.toCsv());
                    writer.newLine();
                }
            }
        }
    }

    // Loads accounts and transactions from a CSV file using BufferedReader
    public synchronized void load(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("[Notice] Data file '" + filePath + "' not found. Initializing with empty bank database.");
            return;
        }

        accounts.clear();
        history.clear();
        int maxAccId = 1000;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            String currentSection = "";

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                if (line.startsWith("#")) {
                    currentSection = line;
                    continue;
                }

                if (currentSection.equals("#ACCOUNTS")) {
                    // Format: accountNumber,holderName,type,balance
                    String[] parts = line.split(",");
                    if (parts.length == 4) {
                        String accNum = parts[0].trim();
                        String name = parts[1].trim();
                        String type = parts[2].trim();
                        double balance = Double.parseDouble(parts[3].trim());

                        Account acc;
                        if (type.equalsIgnoreCase("Savings")) {
                            acc = new SavingsAccount(accNum, name, balance);
                        } else {
                            acc = new CurrentAccount(accNum, name, balance);
                        }
                        accounts.put(accNum, acc);
                        history.put(accNum, new ArrayList<>());

                        // Update nextAccountCounter to prevent ID collisions
                        if (accNum.startsWith("ACC")) {
                            try {
                                int id = Integer.parseInt(accNum.substring(3));
                                if (id > maxAccId) {
                                    maxAccId = id;
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                } else if (currentSection.equals("#TRANSACTIONS")) {
                    // Format: accountNumber,type,amount,dateTime,balanceAfter
                    String[] parts = line.split(",");
                    if (parts.length == 5) {
                        String accNum = parts[0].trim();
                        String type = parts[1].trim();
                        double amount = Double.parseDouble(parts[2].trim());
                        String dateTime = parts[3].trim();
                        double balanceAfter = Double.parseDouble(parts[4].trim());

                        Transaction tx = new Transaction(type, amount, dateTime, balanceAfter);
                        if (!history.containsKey(accNum)) {
                            history.put(accNum, new ArrayList<>());
                        }
                        history.get(accNum).add(tx);
                    }
                }
            }
        }

        this.nextAccountCounter = maxAccId + 1;
        System.out.println("[Success] Loaded " + accounts.size() + " accounts from '" + filePath + "'.");
    }
}

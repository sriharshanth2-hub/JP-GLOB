import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Class representing a single banking transaction (deposit, withdrawal, transfer, interest).
// Encapsulates transaction details and provides formatting for display and CSV file persistence.
public class Transaction {
    private String type;         // E.g., DEPOSIT, WITHDRAW, TRANSFER_IN, TRANSFER_OUT, INTEREST
    private double amount;       // Amount involved in the transaction
    private String dateTime;     // Timestamp of the transaction
    private double balanceAfter; // Account balance immediately after this transaction

    // Formatter for consistent readable date-time strings
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Constructor for a new live transaction (auto-generates current timestamp)
    public Transaction(String type, double amount, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.dateTime = LocalDateTime.now().format(FORMATTER);
        this.balanceAfter = balanceAfter;
    }

    // Overloaded constructor for loading an existing transaction from file
    public Transaction(String type, double amount, String dateTime, double balanceAfter) {
        this.type = type;
        this.amount = amount;
        this.dateTime = dateTime;
        this.balanceAfter = balanceAfter;
    }

    // Getters for transaction properties
    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    // Convert transaction details to a CSV format line for file saving
    public String toCsv() {
        return type + "," + amount + "," + dateTime + "," + balanceAfter;
    }

    // Human-friendly representation for printing mini-statements on console
    @Override
    public String toString() {
        return String.format("%-19s | %-12s | Rs. %-9.2f | Balance: Rs. %.2f",
                dateTime, type, amount, balanceAfter);
    }
}

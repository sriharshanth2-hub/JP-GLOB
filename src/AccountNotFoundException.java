// Custom exception thrown when a requested account number is not found in the bank.
public class AccountNotFoundException extends Exception {
    // Constructor accepting a custom error message
    public AccountNotFoundException(String message) {
        super(message);
    }
}

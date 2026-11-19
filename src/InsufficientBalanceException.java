// Custom exception thrown when an account does not have enough funds for a withdrawal or transfer.
public class InsufficientBalanceException extends Exception {
    // Constructor accepting a custom error message
    public InsufficientBalanceException(String message) {
        super(message);
    }
}

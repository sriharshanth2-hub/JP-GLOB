// Custom exception thrown when a user enters a non-positive (zero or negative) amount for banking operations.
public class InvalidAmountException extends Exception {
    // Constructor accepting a custom error message
    public InvalidAmountException(String message) {
        super(message);
    }
}

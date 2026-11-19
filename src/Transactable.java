// Interface defining standard banking transaction operations.
// Demonstrates the OOP concept of Interface (contract for deposit and withdraw operations).
public interface Transactable {
    // Method to deposit money into an account
    void deposit(double amount) throws InvalidAmountException;

    // Method to withdraw money from an account
    void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException;
}

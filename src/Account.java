// Abstract base class representing a generic bank account.
// Implements Transactable interface and demonstrates Abstraction and Encapsulation.
public abstract class Account implements Transactable {
    // Private attributes (Encapsulation: fields hidden from direct external modification)
    private String accountNumber;
    private String holderName;
    private double balance;

    // Parameterized constructor initializing account attributes
    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    // Getter methods providing read-only access to private attributes
    public String getAccountNumber() {
        return accountNumber;
    }

    public String getHolderName() {
        return holderName;
    }

    public double getBalance() {
        return balance;
    }

    // Helper method for subclasses to safely deduct balance after validation
    protected void deductBalance(double amount) {
        this.balance -= amount;
    }

    // Concrete deposit implementation with input validation
    @Override
    public void deposit(double amount) throws InvalidAmountException {
        if (amount <= 0) {
            throw new InvalidAmountException("Deposit amount must be greater than zero. Provided: " + amount);
        }
        this.balance += amount;
    }

    // Concrete withdraw validation checking for positive withdrawal amount
    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        if (amount <= 0) {
            throw new InvalidAmountException("Withdrawal amount must be greater than zero. Provided: " + amount);
        }
    }

    // Abstract methods to be specifically implemented by subclasses (Polymorphism & Abstraction)
    public abstract String getAccountType();
    public abstract double calculateInterest();

    @Override
    public String toString() {
        return String.format("[%s] Acc No: %s | Holder: %s | Balance: Rs. %.2f",
                getAccountType(), accountNumber, holderName, balance);
    }
}

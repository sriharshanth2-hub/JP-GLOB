// SavingsAccount subclass extending Account (Demonstrates Inheritance).
// Enforces a minimum balance rule of Rs. 500 and provides a 4% annual interest rate.
public class SavingsAccount extends Account {
    // Minimum balance required for Savings accounts
    public static final double MINIMUM_BALANCE = 500.0;
    // Annual interest rate (4%)
    public static final double INTEREST_RATE = 0.04;

    // Constructor chaining using super() to initialize parent class fields
    public SavingsAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    // Overridden withdraw method enforcing the minimum balance requirement
    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        // First validate that amount is positive via parent class method
        super.withdraw(amount);

        // Check if balance after withdrawal falls below minimum balance
        if (getBalance() - amount < MINIMUM_BALANCE) {
            throw new InsufficientBalanceException(
                String.format("Withdrawal denied: Minimum balance of Rs. %.2f must be maintained. Current balance: Rs. %.2f, Requested: Rs. %.2f",
                        MINIMUM_BALANCE, getBalance(), amount)
            );
        }

        // Deduct balance safely
        deductBalance(amount);
    }

    // Implements abstract method from Account
    @Override
    public String getAccountType() {
        return "Savings";
    }

    // Calculates 4% interest on the current balance
    @Override
    public double calculateInterest() {
        return getBalance() * INTEREST_RATE;
    }
}

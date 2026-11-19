// CurrentAccount subclass extending Account (Demonstrates Inheritance).
// Enforces an overdraft facility up to Rs. 5000 (balance can go negative down to -5000).
public class CurrentAccount extends Account {
    // Maximum allowable overdraft limit
    public static final double OVERDRAFT_LIMIT = 5000.0;

    // Constructor chaining using super() to initialize parent class fields
    public CurrentAccount(String accountNumber, String holderName, double balance) {
        super(accountNumber, holderName, balance);
    }

    // Overridden withdraw method enforcing the overdraft limit
    @Override
    public void withdraw(double amount) throws InvalidAmountException, InsufficientBalanceException {
        // First validate that amount is positive via parent class method
        super.withdraw(amount);

        // Check if balance after withdrawal exceeds allowable overdraft
        if (getBalance() - amount < -OVERDRAFT_LIMIT) {
            double maxWithdrawal = getBalance() + OVERDRAFT_LIMIT;
            throw new InsufficientBalanceException(
                String.format("Withdrawal denied: Overdraft limit of Rs. %.2f exceeded. Current balance: Rs. %.2f, Maximum allowable withdrawal: Rs. %.2f",
                        OVERDRAFT_LIMIT, getBalance(), Math.max(0.0, maxWithdrawal))
            );
        }

        // Deduct balance safely
        deductBalance(amount);
    }

    // Implements abstract method from Account
    @Override
    public String getAccountType() {
        return "Current";
    }

    // Current accounts do not earn interest
    @Override
    public double calculateInterest() {
        return 0.0;
    }
}

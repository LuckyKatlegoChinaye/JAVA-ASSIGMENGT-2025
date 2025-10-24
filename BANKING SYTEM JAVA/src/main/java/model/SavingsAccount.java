package model;

public class SavingsAccount extends Account implements InterestBearing, Withdrawal {

    private static final double INTEREST_RATE = 0.03;

    public SavingsAccount(long customerId, String accountNumber, double balance, String branch) {
        super(customerId, accountNumber, "SAVINGS", balance, branch);
    }

    @Override
    public void applyInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
    }

    @Override
    public boolean withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal must be positive.");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient balance in Savings Account.");
        }
        balance -= amount;
        return true;
    }
}

package model;

public class InvestmentAccount extends Account
        implements InterestBearing, Withdrawal {

    private static final double INTEREST_RATE = 0.05;
    private static final double MIN_INITIAL = 500.0;

    public InvestmentAccount(long customerId, String accountNumber,
                             double balance, String branch) {
        super(customerId, accountNumber, "INVESTMENT", balance, branch);
        if (balance < MIN_INITIAL) {
            throw new IllegalArgumentException("Minimum deposit for investment account is 500.00");
        }
    }

    @Override
    public void applyInterest() {
        double interest = balance * INTEREST_RATE;
        balance += interest;
    }

    @Override
    public boolean withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal must be positive");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds in Investment Account.");
        }
        balance -= amount;
        return true;
    }
}

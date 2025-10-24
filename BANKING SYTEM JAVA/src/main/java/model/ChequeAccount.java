package model;


public class ChequeAccount extends Account implements Withdrawal {

    private static final String ACCOUNT_TYPE = "CHEQUE";

    public ChequeAccount(long customerId, String accountNumber,
                         double balance, String branch) {
        super(customerId, accountNumber, ACCOUNT_TYPE, balance, branch);
    }

    @Override
    public boolean withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds in Cheque Account.");
        }
        balance -= amount; // directly update inherited protected field
        return true;
    }
}

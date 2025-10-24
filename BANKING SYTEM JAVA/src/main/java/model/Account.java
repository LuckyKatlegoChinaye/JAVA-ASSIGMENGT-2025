package model;

public abstract class Account {

    protected long id;
    protected long customerId;
    protected String accountNumber;
    protected String accountType;
    protected double balance;
    protected String branch;
    protected String status = "ACTIVE";

    public Account(long customerId, String accountNumber, String accountType,
                   double balance, String branch) {
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.branch = branch;
    }

    public Account(long id, long customerId, String accountNumber, String accountType,
                   double balance, String branch, String status) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = balance;
        this.branch = branch;
        this.status = status;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    
    public void deposit(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public long getCustomerId() { return customerId; }
    public String getAccountNumber() { return accountNumber; }
    public String getAccountType() { return accountType; }
    public double getBalance() { return balance; }
    public String getBranch() { return branch; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("%s Account [%s] Balance: %.2f", accountType, accountNumber, balance);
    }
}

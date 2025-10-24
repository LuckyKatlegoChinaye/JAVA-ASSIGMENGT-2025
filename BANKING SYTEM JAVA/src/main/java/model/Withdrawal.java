package model;

public interface Withdrawal {
    boolean withdraw(double amount) throws InsufficientFundsException;
}

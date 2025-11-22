package com.bank.model;

public class SavingsAccount extends Account {
    public SavingsAccount(int id, int userId, double balance, String status) {
        super(id, userId, balance, status, "SAVINGS");
    }
    // Add savings-specific fields/methods if needed
}

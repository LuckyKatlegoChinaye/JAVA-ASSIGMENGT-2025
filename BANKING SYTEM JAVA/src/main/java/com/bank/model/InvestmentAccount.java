package com.bank.model;

public class InvestmentAccount extends Account {
    public InvestmentAccount(int id, int userId, double balance, String status) {
        super(id, userId, balance, status, "INVESTMENT");
    }
    // Add investment-specific fields/methods if needed
}

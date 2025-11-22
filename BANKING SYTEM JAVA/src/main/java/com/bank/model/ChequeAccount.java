package com.bank.model;

public class ChequeAccount extends Account {
    public ChequeAccount(int id, int userId, double balance, String status) {
        super(id, userId, balance, status, "CHEQUE");
    }
    // Add cheque-specific fields/methods if needed
}

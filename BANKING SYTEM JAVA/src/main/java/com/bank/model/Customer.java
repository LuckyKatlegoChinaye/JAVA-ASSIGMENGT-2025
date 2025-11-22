package com.bank.model;

public class Customer extends User {
    public Customer(int id, String username, String password, String name, String email) {
        super(id, username, password, name, email, "CUSTOMER");
    }
    // Add customer-specific fields/methods if needed
}

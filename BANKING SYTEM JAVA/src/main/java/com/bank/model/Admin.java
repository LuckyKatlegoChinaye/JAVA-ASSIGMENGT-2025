package com.bank.model;

public class Admin extends User {
    public Admin(int id, String username, String password, String name, String email) {
        super(id, username, password, name, email, "ADMIN");
    }
    // Add admin-specific fields/methods if needed
}

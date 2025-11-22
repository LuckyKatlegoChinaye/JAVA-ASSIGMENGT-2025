package com.bank.model;

public abstract class Account {
    private int id;
    private int userId;
    private double balance;
    private String status; // PENDING, APPROVED, REJECTED, CLOSED
    private String type;

    public Account(int id, int userId, double balance, String status, String type) {
        this.id = id;
        this.userId = userId;
        this.balance = balance;
        this.status = status;
        this.type = type;
    }
    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + userId +
                ", balance=" + balance +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

package model;

public class Customer {
    private long id;
    private String fullName;
    private String username;
    private String passwordHash;
    private String email;
    private boolean approved;

    // Default constructor
    public Customer() {}

    // Constructor for quick creation (e.g., during registration)
    public Customer(String fullName, String username, String passwordHash, String email) {
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.approved = false; // default until staff approval
    }

    // Constructor for fetching from DB
    public Customer(long id, String fullName, String username, String passwordHash, String email, boolean approved) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.approved = approved;
    }

    // Getters
    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public boolean isApproved() { return approved; }

    // Setters
    public void setId(long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setEmail(String email) { this.email = email; }
    public void setApproved(boolean approved) { this.approved = approved; }

    @Override
    public String toString() {
        return String.format("Customer[id=%d, name=%s, username=%s, approved=%s]",
                id, fullName, username, approved ? "Yes" : "No");
    }
}

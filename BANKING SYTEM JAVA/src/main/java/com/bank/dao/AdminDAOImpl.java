package com.bank.dao;

import com.bank.model.*;
import java.sql.*;
import java.util.*;

public class AdminDAOImpl implements AdminDAO {
    @Override
    public Admin getAdminById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE id = ? AND role = 'ADMIN'")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Admin> getAllAdmins() {
        List<Admin> admins = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE role = 'ADMIN'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                admins.add(new Admin(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return admins;
    }

    @Override
    public boolean addAdmin(Admin admin) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO users (username, password, name, email, role) VALUES (?, ?, ?, ?, 'ADMIN')", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getName());
            stmt.setString(4, admin.getEmail());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) admin.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean updateAdmin(Admin admin) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE users SET username=?, password=?, name=?, email=? WHERE id=? AND role='ADMIN'")) {
            stmt.setString(1, admin.getUsername());
            stmt.setString(2, admin.getPassword());
            stmt.setString(3, admin.getName());
            stmt.setString(4, admin.getEmail());
            stmt.setInt(5, admin.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean deleteAdmin(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ? AND role = 'ADMIN'")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    // Example admin-specific business logic
    public List<Account> getPendingAccounts() {
        List<Account> pendingAccounts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE status = 'PENDING'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                pendingAccounts.add(new Account(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getDouble("balance"),
                    rs.getString("status"),
                    rs.getString("type")
                ) {
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return pendingAccounts;
    }

    public boolean approveAccount(int accountId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET status = 'APPROVED' WHERE id = ?")) {
            stmt.setInt(1, accountId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean rejectAccount(int accountId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET status = 'REJECTED' WHERE id = ?")) {
            stmt.setInt(1, accountId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean closeAccount(int accountId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET status = 'CLOSED' WHERE id = ?")) {
            stmt.setInt(1, accountId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE role = 'CUSTOMER'")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                customers.add(new Customer(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("email")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return customers;
    }
}

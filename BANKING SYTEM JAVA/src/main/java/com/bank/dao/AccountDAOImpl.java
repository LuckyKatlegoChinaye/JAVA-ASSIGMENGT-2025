package com.bank.dao;

import com.bank.model.*;
import java.sql.*;
import java.util.*;

public class AccountDAOImpl implements AccountDAO {
    @Override
    public Account getAccountById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Account(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getDouble("balance"),
                    rs.getString("status"),
                    rs.getString("type")
                ) {
                };
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Account> getAccountsByUserId(int userId) {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                accounts.add(new Account(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getDouble("balance"),
                    rs.getString("status"),
                    rs.getString("type")
                ) {
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return accounts;
    }

    @Override
    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM accounts");
            while (rs.next()) {
                accounts.add(new Account(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getDouble("balance"),
                    rs.getString("status"),
                    rs.getString("type")
                ) {
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return accounts;
    }

    @Override
    public boolean addAccount(Account account) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO accounts (user_id, type, balance, status) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getType());
            stmt.setDouble(3, account.getBalance());
            stmt.setString(4, account.getStatus());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) account.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean updateAccount(Account account) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "UPDATE accounts SET user_id=?, type=?, balance=?, status=? WHERE id=?")) {
            stmt.setInt(1, account.getUserId());
            stmt.setString(2, account.getType());
            stmt.setDouble(3, account.getBalance());
            stmt.setString(4, account.getStatus());
            stmt.setInt(5, account.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean deleteAccount(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean approveAccount(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET status = 'APPROVED' WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean rejectAccount(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET status = 'REJECTED' WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean closeAccount(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET status = 'CLOSED' WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}

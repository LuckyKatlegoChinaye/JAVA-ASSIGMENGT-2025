package com.bank.dao;

import com.bank.model.Transaction;
import java.sql.*;
import java.util.*;

public class TransactionDAOImpl implements TransactionDAO {
    @Override
    public Transaction getTransactionById(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transactions WHERE id = ?")) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getDate("date"),
                    rs.getString("description")
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    @Override
    public List<Transaction> getTransactionsByAccountId(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM transactions WHERE account_id = ?")) {
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getDate("date"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return transactions;
    }

    @Override
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM transactions");
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getDate("date"),
                    rs.getString("description")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return transactions;
    }

    @Override
    public boolean addTransaction(Transaction transaction) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                "INSERT INTO transactions (account_id, type, amount, date, description) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, transaction.getAccountId());
            stmt.setString(2, transaction.getType());
            stmt.setDouble(3, transaction.getAmount());
            stmt.setDate(4, new java.sql.Date(transaction.getDate().getTime()));
            stmt.setString(5, transaction.getDescription());
            int affected = stmt.executeUpdate();
            if (affected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) transaction.setId(rs.getInt(1));
                return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    @Override
    public boolean deleteTransaction(int id) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM transactions WHERE id = ?")) {
            stmt.setInt(1, id);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}

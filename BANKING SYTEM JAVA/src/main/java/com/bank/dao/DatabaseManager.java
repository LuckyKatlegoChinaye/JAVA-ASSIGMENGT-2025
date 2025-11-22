package com.bank.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:banking.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Users table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, role TEXT, name TEXT, email TEXT)");
            // Accounts table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS accounts (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, type TEXT, balance REAL, status TEXT, FOREIGN KEY(user_id) REFERENCES users(id))");
            // Transactions table
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS transactions (id INTEGER PRIMARY KEY AUTOINCREMENT, account_id INTEGER, type TEXT, amount REAL, date TEXT, description TEXT, FOREIGN KEY(account_id) REFERENCES accounts(id))");

            // Insert default admin if not exists
            String checkAdmin = "SELECT COUNT(*) FROM users WHERE username='admin' AND role='ADMIN'";
            try (java.sql.ResultSet rs = stmt.executeQuery(checkAdmin)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String insertAdmin = "INSERT INTO users (username, password, role, name, email) VALUES ('admin', 'admin', 'ADMIN', 'Default Admin', 'admin@bank.com')";
                    stmt.executeUpdate(insertAdmin);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

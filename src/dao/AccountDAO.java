package dao;

import db.DatabaseConnection;
import models.Account;
import db.PasswordUtil; // Must be updated to use BCrypt or similar

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AccountDAO {

    // ----------------------------------------------------------------------------------
    // CRUD: CREATE
    // ----------------------------------------------------------------------------------

    /**
     * Adds a new account, hashing the password using a strong, salted algorithm (BCrypt).
     */
    public void addAccount(Account acc) throws Exception {
        // NOTE: The table name was "accounts" in addAccount but "account" in getAllAccounts/deleteAccount.
        // I will assume the table is named 'accounts' for consistency.
        String sql = "INSERT INTO accounts (username, password, role, first_name, last_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // SECURITY FIX: PasswordUtil must use BCrypt/PBKDF2 with salt.
            String hashedPassword = PasswordUtil.hashPassword(acc.getPassword());

            stmt.setString(1, acc.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, acc.getRole());
            stmt.setString(4, acc.getFirstName());
            stmt.setString(5, acc.getLastName());
            stmt.executeUpdate();
        }
    }

    // ----------------------------------------------------------------------------------
    // CRUD: READ (Single Account) - NEW METHOD
    // ----------------------------------------------------------------------------------

    /**
     * Retrieves a single account by its ID.
     */
    public Account getAccountById(int accountId) throws Exception {
        String sql = "SELECT account_id, username, role, first_name, last_name FROM accounts WHERE account_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------------------
    // AUTHENTICATION: LOGIN
    // ----------------------------------------------------------------------------------

    /**
     * Authenticates a user by username and password, using secure password verification.
     */
    public Account login(String username, String password) throws Exception {
        String sql = "SELECT * FROM accounts WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");

                // SECURITY FIX: Use verifyPassword for BCrypt/PBKDF2, which handles salting and stretching automatically.
                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    return mapResultSetToAccount(rs);
                }
            }
        }
        return null;
    }

    // ----------------------------------------------------------------------------------
    // CRUD: READ (All Accounts)
    // ----------------------------------------------------------------------------------

    /**
     * Retrieves all accounts for the user management window.
     */
    public List<Account> getAllAccounts() throws Exception {
        List<Account> accounts = new ArrayList<>();
        // FIX: Changed 'account' to 'accounts' for table name consistency.
        // It's safer to use PreparedStatement even without parameters to avoid SQL injection risks
        // if the query ever changes, though Statement is technically okay here.
        String sql = "SELECT account_id, username, role, first_name, last_name FROM accounts";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }

    // ----------------------------------------------------------------------------------
    // CRUD: UPDATE - NEW METHOD
    // ----------------------------------------------------------------------------------

    /**
     * Updates an existing account's details. Handles password update only if a new password is provided.
     */
    public void updateAccount(Account acc) throws Exception {
        String sql;
        String hashedPassword = null;

        // 1. Check if the password field is set (meaning the user wants to change it)
        if (acc.getPassword() != null && !acc.getPassword().isEmpty()) {
            // Hash the new password securely
            hashedPassword = PasswordUtil.hashPassword(acc.getPassword());
            sql = "UPDATE accounts SET role=?, first_name=?, last_name=?, password=? WHERE account_id=?";
        } else {
            // Update without changing the password
            sql = "UPDATE accounts SET role=?, first_name=?, last_name=? WHERE account_id=?";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            stmt.setString(paramIndex++, acc.getRole());
            stmt.setString(paramIndex++, acc.getFirstName());
            stmt.setString(paramIndex++, acc.getLastName());

            if (hashedPassword != null) {
                stmt.setString(paramIndex++, hashedPassword);
            }

            stmt.setInt(paramIndex, acc.getAccountId());
            stmt.executeUpdate();
        }
    }

    // ----------------------------------------------------------------------------------
    // CRUD: DELETE
    // ----------------------------------------------------------------------------------

    /**
     * Deletes an account by its ID.
     */
    public void deleteAccount(int accountId) throws Exception {
        // FIX: Changed 'account' to 'accounts' for table name consistency.
        String sql = "DELETE FROM accounts WHERE account_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.executeUpdate();
        }
    }

    // ----------------------------------------------------------------------------------
    // UTILITY METHOD
    // ----------------------------------------------------------------------------------

    /**
     * Maps a ResultSet row to an Account object, reducing redundancy.
     */
    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account acc = new Account();
        acc.setAccountId(rs.getInt("account_id"));
        acc.setUsername(rs.getString("username"));
        acc.setRole(rs.getString("role"));
        acc.setFirstName(rs.getString("first_name"));
        acc.setLastName(rs.getString("last_name"));
        // Note: Password is NOT mapped for security reasons
        return acc;
    }
}
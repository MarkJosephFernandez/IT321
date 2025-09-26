package dao;

import db.DatabaseConnection;
import models.Account;
import java.sql.*;
import db.PasswordUtil;

public class AccountDAO {

    // ✅ Store account with SHA-256 hashed password
    public void addAccount(Account acc) throws Exception {
        String sql = "INSERT INTO accounts (username, password, role, first_name, last_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordUtil.hashPassword(acc.getPassword());

            stmt.setString(1, acc.getUsername());
            stmt.setString(2, hashedPassword);
            stmt.setString(3, acc.getRole());
            stmt.setString(4, acc.getFirstName());
            stmt.setString(5, acc.getLastName());
            stmt.executeUpdate();
        }
    }

    // ✅ Login by hashing input password and comparing with stored hash
    public Account login(String username, String password) throws Exception {
        String sql = "SELECT * FROM accounts WHERE username=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password");
                String inputHash = PasswordUtil.hashPassword(password);

                if (storedHash.equals(inputHash)) {
                    Account acc = new Account();
                    acc.setAccountId(rs.getInt("account_id"));
                    acc.setUsername(rs.getString("username"));
                    acc.setRole(rs.getString("role"));
                    acc.setFirstName(rs.getString("first_name"));
                    acc.setLastName(rs.getString("last_name"));
                    return acc;
                }
            }
        }
        return null;
    }
}

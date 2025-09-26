package dao;

import db.DatabaseConnection;
import models.Account;

import java.sql.*;

public class AccountDAO {

    public void addAccount(Account acc) throws Exception {
        String sql = "INSERT INTO account (username, password, role, first_name, last_name) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, acc.getUsername());
            stmt.setString(2, acc.getPassword()); // ⚠️ should hash in real app
            stmt.setString(3, acc.getRole());
            stmt.setString(4, acc.getFirstName());
            stmt.setString(5, acc.getLastName());
            stmt.executeUpdate();
        }
    }

    public Account login(String username, String password) throws Exception {
        String sql = "SELECT * FROM account WHERE username=? AND password=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Account acc = new Account();
                acc.setAccountId(rs.getInt("account_id"));
                acc.setUsername(rs.getString("username"));
                acc.setRole(rs.getString("role"));
                acc.setFirstName(rs.getString("first_name"));
                acc.setLastName(rs.getString("last_name"));
                return acc;
            }
        }
        return null; // login failed
    }
}

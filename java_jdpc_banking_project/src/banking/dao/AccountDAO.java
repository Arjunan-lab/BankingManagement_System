package banking.dao;

import banking.models.Account;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class AccountDAO {

    public int createAccount(Account account) throws SQLException {
        String sql = "INSERT INTO accounts (name,email,phone,password,balance) VALUES (?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, account.getName());
            ps.setString(2, account.getEmail());
            ps.setString(3, account.getPhone());
            ps.setString(4, account.getPassword());
            ps.setDouble(5, account.getBalance());
            int affected = ps.executeUpdate();
            if (affected == 0) throw new SQLException("Creating account failed, no rows affected.");
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Creating account failed, no ID obtained.");
                }
            }
        }
    }

    public Account findById(int accountId) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    public Account findByCredentials(int accountId, String password) throws SQLException {
        String sql = "SELECT * FROM accounts WHERE account_id = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
                return null;
            }
        }
    }

    public void updateBalance(int accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE account_id = ?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setInt(2, accountId);
            ps.executeUpdate();
        }
    }

    public java.util.List<Account> findAll() throws SQLException {
        String sql = "SELECT * FROM accounts ORDER BY account_id";
        java.util.List<Account> accounts = new java.util.ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accounts.add(mapRow(rs));
            }
        }
        return accounts;
    }

    private Account mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("account_id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String password = rs.getString("password");
        double balance = rs.getDouble("balance");
        Timestamp ts = rs.getTimestamp("created_at");
        LocalDateTime createdAt = ts != null ? ts.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime() : null;
        String status = rs.getString("status");
        return new Account(id, name, email, phone, password, balance, createdAt, status);
    }
}

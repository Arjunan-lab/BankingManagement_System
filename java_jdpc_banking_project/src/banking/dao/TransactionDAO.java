package banking.dao;

import banking.models.Transaction;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    public void insertTransaction(Transaction t) throws SQLException {
        String sql = "INSERT INTO transactions (account_id, type, amount, remarks) VALUES (?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, t.getAccountId());
            ps.setString(2, t.getType());
            ps.setDouble(3, t.getAmount());
            ps.setString(4, t.getRemarks());
            ps.executeUpdate();
        }
    }

    public List<Transaction> findByAccountId(int accountId) throws SQLException {
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        List<Transaction> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction t = new Transaction();
                    t.setTransactionId(rs.getInt("transaction_id"));
                    t.setAccountId(rs.getInt("account_id"));
                    t.setType(rs.getString("type"));
                    t.setAmount(rs.getDouble("amount"));
                    Timestamp ts = rs.getTimestamp("timestamp");
                    if (ts != null) t.setTimestamp(ts.toLocalDateTime());
                    t.setRemarks(rs.getString("remarks"));
                    list.add(t);
                }
            }
        }
        return list;
    }
}

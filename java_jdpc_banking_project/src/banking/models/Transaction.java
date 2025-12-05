package banking.models;

import java.time.LocalDateTime;

public class Transaction {
    private int transactionId;
    private int accountId;
    private String type;
    private double amount;
    private LocalDateTime timestamp;
    private String remarks;

    public Transaction() {}

    public Transaction(int accountId, String type, double amount, String remarks) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.remarks = remarks;
    }

    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
}

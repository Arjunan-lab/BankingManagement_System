package banking.models;

import java.time.LocalDateTime;

public class Account {
    private int accountId;
    private String name;
    private String email;
    private String phone;
    private String password; // stored hashed externally
    private double balance;
    private LocalDateTime createdAt;
    private String status;

    public Account() {}

    public Account(String name, String email, String phone, String password, double balance) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.balance = balance;
    }

    public Account(int accountId, String name, String email, String phone, String password, double balance, LocalDateTime createdAt, String status) {
        this.accountId = accountId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.balance = balance;
        this.createdAt = createdAt;
        this.status = status;
    }

    public int getAccountId() { return accountId; }
    public void setAccountId(int accountId) { this.accountId = accountId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

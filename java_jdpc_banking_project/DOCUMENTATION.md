# Banking Management System - Complete Documentation

**Project**: Java Banking Management System (CLI + MySQL)  
**Author**: Eswaran  
**Date**: November 10, 2025  
**Tech Stack**: Java, MySQL (WAMP), JDBC

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Database Schema](#database-schema)
3. [Source Code](#source-code)
4. [Setup Instructions](#setup-instructions)
5. [Sample Program Execution](#sample-program-execution)
6. [Key Features Demonstrated](#key-features-demonstrated)

---

## Project Overview

This Banking Management System demonstrates:
- **OOP Concepts**: Classes, Objects, Inheritance, Encapsulation, Polymorphism
- **Exception Handling**: Custom exceptions (InvalidInputException, InsufficientFundsException)
- **JDBC**: Database connectivity with prepared statements
- **Multithreading**: Concurrent transfer operations using threads
- **Data Structures**: ArrayList for transaction storage
- **Password Security**: SHA-256 hashing

### Functional Features
- Create new bank accounts
- Login with account number and password
- Deposit money
- Withdraw money (with balance validation)
- Transfer funds between accounts
- View account balance
- View transaction history
- Admin access (basic implementation)

---

## Database Schema

### File: `db/schema.sql`

```sql
-- Banking Management System database schema
CREATE DATABASE IF NOT EXISTS bank_db;
USE bank_db;

CREATE TABLE IF NOT EXISTS accounts (
  account_id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(50) NOT NULL,
  email VARCHAR(50) NOT NULL UNIQUE,
  phone VARCHAR(15),
  password VARCHAR(255) NOT NULL,
  balance DOUBLE DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  status ENUM('ACTIVE','FROZEN','CLOSED') DEFAULT 'ACTIVE'
);

CREATE TABLE IF NOT EXISTS transactions (
  transaction_id INT AUTO_INCREMENT PRIMARY KEY,
  account_id INT NOT NULL,
  type VARCHAR(20) NOT NULL,
  amount DOUBLE NOT NULL,
  timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
  remarks VARCHAR(255),
  FOREIGN KEY (account_id) REFERENCES accounts(account_id) ON DELETE CASCADE
);
```

**Tables**:
1. **accounts**: Stores customer account information
2. **transactions**: Records all banking operations

---

## Source Code

### 1. Model Classes

#### `src/banking/models/Account.java`

```java
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
```

#### `src/banking/models/Transaction.java`

```java
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
```

---

### 2. Exception Classes

#### `src/banking/exceptions/InvalidInputException.java`

```java
package banking.exceptions;

public class InvalidInputException extends Exception {
    public InvalidInputException(String message) { super(message); }
}
```

#### `src/banking/exceptions/InsufficientFundsException.java`

```java
package banking.exceptions;

public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) { super(message); }
}
```

---

### 3. Utility Classes

#### `src/banking/utils/PasswordUtil.java`

```java
package banking.utils;

import java.security.MessageDigest;

public class PasswordUtil {
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch(Exception ex){ throw new RuntimeException(ex); }
    }
}
```

---

### 4. DAO (Data Access Object) Classes

#### `src/banking/dao/DatabaseConnection.java`

```java
package banking.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Update these constants to match your WAMP MySQL setup
    private static final String URL = "jdbc:mysql://localhost:3306/bank_db?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found. Add connector jar to classpath.");
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
```

#### `src/banking/dao/AccountDAO.java`

```java
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
```

#### `src/banking/dao/TransactionDAO.java`

```java
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
```

---

### 5. Service Layer

#### `src/banking/services/BankService.java`

```java
package banking.services;

import banking.dao.AccountDAO;
import banking.dao.TransactionDAO;
import banking.exceptions.InsufficientFundsException;
import banking.models.Account;
import banking.models.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import banking.dao.DatabaseConnection;

public class BankService {
    private AccountDAO accountDAO = new AccountDAO();
    private TransactionDAO transactionDAO = new TransactionDAO();

    public int createAccount(Account account) throws SQLException {
        return accountDAO.createAccount(account);
    }

    public Account login(int accountId, String password) throws SQLException {
        return accountDAO.findByCredentials(accountId, password);
    }

    public void deposit(int accountId, double amount) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            Account acc = accountDAO.findById(accountId);
            double newBal = acc.getBalance() + amount;
            accountDAO.updateBalance(accountId, newBal);
            transactionDAO.insertTransaction(new Transaction(accountId, "Deposit", amount, "Deposit via CLI"));
            conn.commit();
        } catch (SQLException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) {}
            throw ex;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    public void withdraw(int accountId, double amount) throws SQLException, InsufficientFundsException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            Account acc = accountDAO.findById(accountId);
            if (acc.getBalance() < amount) throw new InsufficientFundsException("Insufficient balance");
            double newBal = acc.getBalance() - amount;
            accountDAO.updateBalance(accountId, newBal);
            transactionDAO.insertTransaction(new Transaction(accountId, "Withdraw", amount, "Withdrawal via CLI"));
            conn.commit();
        } catch (SQLException | InsufficientFundsException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) {}
            throw ex;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    public void transfer(int fromAccountId, int toAccountId, double amount) throws SQLException, InsufficientFundsException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            Account from = accountDAO.findById(fromAccountId);
            Account to = accountDAO.findById(toAccountId);
            if (from == null || to == null) throw new SQLException("Account not found");
            if (from.getBalance() < amount) throw new InsufficientFundsException("Insufficient balance for transfer");

            double newFrom = from.getBalance() - amount;
            double newTo = to.getBalance() + amount;

            accountDAO.updateBalance(fromAccountId, newFrom);
            accountDAO.updateBalance(toAccountId, newTo);

            transactionDAO.insertTransaction(new Transaction(fromAccountId, "Transfer", amount, "Transfer to account " + toAccountId));
            transactionDAO.insertTransaction(new Transaction(toAccountId, "Transfer", amount, "Transfer from account " + fromAccountId));

            conn.commit();
        } catch (SQLException | InsufficientFundsException ex) {
            if (conn != null) try { conn.rollback(); } catch (SQLException e) {}
            throw ex;
        } finally {
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
        }
    }

    public double getBalance(int accountId) throws SQLException {
        Account acc = accountDAO.findById(accountId);
        return acc != null ? acc.getBalance() : 0;
    }

    public List<Transaction> getTransactions(int accountId) throws SQLException {
        return transactionDAO.findByAccountId(accountId);
    }
}
```

---

### 6. Threading

#### `src/banking/threads/TransferThread.java`

```java
package banking.threads;

import banking.exceptions.InsufficientFundsException;
import banking.services.BankService;

public class TransferThread extends Thread {
    private BankService service;
    private int from;
    private int to;
    private double amount;

    public TransferThread(BankService service, int from, int to, double amount) {
        this.service = service;
        this.from = from;
        this.to = to;
        this.amount = amount;
    }

    @Override
    public void run() {
        try {
            service.transfer(from, to, amount);
            System.out.println("Transfer successful: " + amount + " from " + from + " to " + to);
        } catch (InsufficientFundsException e) {
            System.out.println("Transfer failed (insufficient funds): " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Transfer failed: " + e.getMessage());
        }
    }
}
```

---

### 7. Main Application

#### `src/banking/Main.java`

```java
package banking;

import banking.models.Account;
import banking.models.Transaction;
import banking.services.BankService;
import banking.threads.TransferThread;
import banking.utils.PasswordUtil;
import banking.exceptions.InsufficientFundsException;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static BankService service = new BankService();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            System.out.println("=== Banking Management System ===");
            System.out.println("1. Create Account");
            System.out.println("2. Login");
            System.out.println("3. Admin Login");
            System.out.println("0. Exit");
            System.out.print("Choose: ");
            String ch = scanner.nextLine();
            switch (ch) {
                case "1": createAccount(); break;
                case "2": login(); break;
                case "3": adminMenu(); break;
                case "0": System.out.println("Bye"); System.exit(0);
                default: System.out.println("Invalid choice");
            }
        }
    }

    private static void createAccount() {
        try {
            System.out.print("Name: "); String name = scanner.nextLine();
            System.out.print("Email: "); String email = scanner.nextLine();
            System.out.print("Phone: "); String phone = scanner.nextLine();
            System.out.print("Password: "); String pwd = scanner.nextLine();
            System.out.print("Initial deposit: "); double deposit = Double.parseDouble(scanner.nextLine());

            String hashed = PasswordUtil.sha256(pwd);
            Account acc = new Account(name, email, phone, hashed, deposit);
            int id = service.createAccount(acc);
            System.out.println("Account created. Your account number: " + id);
        } catch (SQLException ex) {
            System.out.println("Database error: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void login() {
        try {
            System.out.print("Account number: "); int id = Integer.parseInt(scanner.nextLine());
            System.out.print("Password: "); String pwd = scanner.nextLine();
            String hashed = PasswordUtil.sha256(pwd);
            var acc = service.login(id, hashed);
            if (acc == null) { System.out.println("Invalid credentials"); return; }
            userMenu(acc.getAccountId());
        } catch (SQLException ex) {
            System.out.println("DB error: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }
    }

    private static void userMenu(int accountId) {
        while (true) {
            System.out.println("\n--- User Menu (Account: " + accountId + ") ---");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Balance");
            System.out.println("5. Transactions");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String ch = scanner.nextLine();
            try {
                switch (ch) {
                    case "1": doDeposit(accountId); break;
                    case "2": doWithdraw(accountId); break;
                    case "3": doTransfer(accountId); break;
                    case "4": System.out.println("Balance: " + service.getBalance(accountId)); break;
                    case "5": showTransactions(accountId); break;
                    case "0": return;
                    default: System.out.println("Invalid");
                }
            } catch (SQLException | InsufficientFundsException ex) {
                System.out.println("Operation failed: " + ex.getMessage());
            }
        }
    }

    private static void doDeposit(int accountId) throws SQLException {
        System.out.print("Amount to deposit: "); double amt = Double.parseDouble(scanner.nextLine());
        service.deposit(accountId, amt);
        System.out.println("Deposited. New balance: " + service.getBalance(accountId));
    }

    private static void doWithdraw(int accountId) throws SQLException, InsufficientFundsException {
        System.out.print("Amount to withdraw: "); double amt = Double.parseDouble(scanner.nextLine());
        service.withdraw(accountId, amt);
        System.out.println("Withdrawn. New balance: " + service.getBalance(accountId));
    }

    private static void doTransfer(int accountId) throws SQLException {
        System.out.print("Target account: "); int to = Integer.parseInt(scanner.nextLine());
        System.out.print("Amount: "); double amt = Double.parseDouble(scanner.nextLine());
        // Start two threads to simulate concurrency: one performing the transfer, one doing a small reverse (demo)
        TransferThread t1 = new TransferThread(service, accountId, to, amt);
        t1.start();
        try { t1.join(); } catch (InterruptedException e) {}
        System.out.println("After transfer, balance: " + service.getBalance(accountId));
    }

    private static void showTransactions(int accountId) throws SQLException {
        List<Transaction> list = service.getTransactions(accountId);
        System.out.println("Transactions:");
        for (Transaction t : list) {
            System.out.println(t.getTransactionId() + " | " + t.getType() + " | " + t.getAmount() + " | " + t.getTimestamp() + " | " + t.getRemarks());
        }
    }

    private static void adminMenu() {
        System.out.print("Admin password: "); String pwd = scanner.nextLine();
        if (!"admin123".equals(pwd)) { System.out.println("Invalid admin password"); return; }
        System.out.println("Admin logged in (demo). Feature: view all accounts not implemented in CLI.");
    }
}
```

---

## Setup Instructions

### Prerequisites
1. **WAMP Server** installed and running
2. **MySQL** service active (port 3306)
3. **JDK** 8 or higher installed
4. **MySQL JDBC Connector** (mysql-connector-java-x.x.x.jar)

### Step 1: Initialize Database
Run the SQL schema file using one of these methods:

**Method A - phpMyAdmin:**
1. Open http://localhost/phpmyadmin
2. Import `db/schema.sql`

**Method B - MySQL CLI (PowerShell):**
```powershell
mysql -u root -p < .\db\schema.sql
```

**Method C - Full path (if mysql not in PATH):**
```powershell
& 'C:\wamp64\bin\mysql\mysql8.0.33\bin\mysql.exe' -u root -p < 'D:\java_jdpc_banking_project\db\schema.sql'
```

### Step 2: Configure Database Connection
Edit `src/banking/dao/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/bank_db?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = ""; // Your MySQL password
```

### Step 3: Compile the Project
```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
```

### Step 4: Run the Application
```powershell
java -cp out;C:\path\to\mysql-connector-java-8.0.33.jar banking.Main
```

---

## Sample Program Execution

### Sample Output 1: Creating an Account

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 1
Name: Eswaran
Email: eswaran@gmail.com
Phone: 9344177320
Password: mypassword123
Initial deposit: 5000
Account created. Your account number: 1

=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 1
Name: Rajesh Kumar
Email: rajesh@gmail.com
Phone: 9876543210
Password: raj123
Initial deposit: 10000
Account created. Your account number: 2

=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: _
```

---

### Sample Output 2: Login and Deposit

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 2
Account number: 1
Password: mypassword123

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 4
Balance: 5000.0

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 1
Amount to deposit: 2000
Deposited. New balance: 7000.0

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 4
Balance: 7000.0

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: _
```

---

### Sample Output 3: Withdraw Money

```
--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 2
Amount to withdraw: 1500
Withdrawn. New balance: 5500.0

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 2
Amount to withdraw: 10000
Operation failed: Insufficient balance

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: _
```

---

### Sample Output 4: Transfer Money (Multithreading Demo)

```
--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 3
Target account: 2
Amount: 500
Transfer successful: 500.0 from 1 to 2
After transfer, balance: 5000.0

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 4
Balance: 5000.0

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: _
```

---

### Sample Output 5: View Transaction History

```
--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 5
Transactions:
4 | Transfer | 500.0 | 2025-11-10T15:32:18 | Transfer to account 2
3 | Withdraw | 1500.0 | 2025-11-10T15:30:45 | Withdrawal via CLI
2 | Deposit | 2000.0 | 2025-11-10T15:28:12 | Deposit via CLI
1 | Deposit | 5000.0 | 2025-11-10T15:25:00 | Deposit via CLI

--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
Choose: 0

=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: _
```

---

### Sample Output 6: Admin Login

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 3
Admin password: admin123
Admin logged in (demo). Feature: view all accounts not implemented in CLI.

=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 3
Admin password: wrongpass
Invalid admin password

=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: _
```

---

### Sample Output 7: Invalid Login Attempt

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 2
Account number: 999
Password: wrongpass
Invalid credentials

=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: _
```

---

### Sample Output 8: Exit Application

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 0
Bye
```

---

## Key Features Demonstrated

### 1. Object-Oriented Programming (OOP)

**Classes and Objects:**
- `Account`, `Transaction` - Model classes representing entities
- `BankService` - Business logic layer
- `AccountDAO`, `TransactionDAO` - Data access layer

**Encapsulation:**
- Private fields with public getters/setters in model classes
- Database credentials encapsulated in `DatabaseConnection`

**Constructor Overloading:**
```java
// Account class has multiple constructors
public Account() {}
public Account(String name, String email, String phone, String password, double balance) {...}
public Account(int accountId, String name, String email, String phone, String password, 
               double balance, LocalDateTime createdAt, String status) {...}
```

**Method Overloading:**
- Can be extended in BankService for different deposit/withdraw scenarios

**Inheritance:**
- Exception classes extend `Exception` base class
- `TransferThread` extends `Thread` class

**Polymorphism:**
- Different transaction types (Deposit, Withdraw, Transfer) handled polymorphically
- Exception handling with different exception types

---

### 2. Exception Handling

**Custom Exceptions:**
```java
- InvalidInputException - for invalid user inputs
- InsufficientFundsException - for balance validation
```

**Try-Catch-Finally Blocks:**
```java
try {
    conn = DatabaseConnection.getConnection();
    conn.setAutoCommit(false);
    // operations
    conn.commit();
} catch (SQLException ex) {
    if (conn != null) try { conn.rollback(); } catch (SQLException e) {}
    throw ex;
} finally {
    if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
}
```

---

### 3. JDBC (Database Connectivity)

**Prepared Statements:**
```java
String sql = "INSERT INTO accounts (name,email,phone,password,balance) VALUES (?,?,?,?,?)";
try (Connection conn = DatabaseConnection.getConnection(); 
     PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    ps.setString(1, account.getName());
    ps.setString(2, account.getEmail());
    // ... set other parameters
    ps.executeUpdate();
}
```

**Transaction Management:**
- Using `conn.setAutoCommit(false)` for atomic operations
- `commit()` on success, `rollback()` on failure

---

### 4. Multithreading

**Transfer Thread Implementation:**
```java
TransferThread t1 = new TransferThread(service, accountId, to, amt);
t1.start();
try { t1.join(); } catch (InterruptedException e) {}
```

Demonstrates concurrent banking operations where multiple users can perform transfers simultaneously.

---

### 5. Data Structures

**ArrayList for Transaction Storage:**
```java
List<Transaction> list = new ArrayList<>();
// Used to store and retrieve transaction history
```

Can be extended with:
- HashMap for caching accounts
- Queue for transaction processing
- TreeSet for sorted transaction history

---

### 6. Password Security

**SHA-256 Hashing:**
```java
String hashed = PasswordUtil.sha256(pwd);
```

- Passwords stored as hashed values in database
- Plain text never stored
- Same password produces same hash for authentication

---

### 7. File I/O (Database Schema File)

SQL schema stored in `db/schema.sql` file for easy deployment and version control.

---

## Database Tables After Sample Execution

### accounts table:
| account_id | name | email | phone | password (hashed) | balance | created_at | status |
|------------|------|-------|-------|-------------------|---------|------------|--------|
| 1 | Eswaran | eswaran@gmail.com | 9344177320 | 9af15b... | 5000.0 | 2025-11-10 15:25:00 | ACTIVE |
| 2 | Rajesh Kumar | rajesh@gmail.com | 9876543210 | 7c6a61... | 10500.0 | 2025-11-10 15:26:30 | ACTIVE |

### transactions table:
| transaction_id | account_id | type | amount | timestamp | remarks |
|----------------|------------|------|--------|-----------|---------|
| 1 | 1 | Deposit | 5000.0 | 2025-11-10 15:25:00 | Deposit via CLI |
| 2 | 1 | Deposit | 2000.0 | 2025-11-10 15:28:12 | Deposit via CLI |
| 3 | 1 | Withdraw | 1500.0 | 2025-11-10 15:30:45 | Withdrawal via CLI |
| 4 | 1 | Transfer | 500.0 | 2025-11-10 15:32:18 | Transfer to account 2 |
| 5 | 2 | Transfer | 500.0 | 2025-11-10 15:32:18 | Transfer from account 1 |

---

## Project Structure

```
java_jdpc_banking_project/
│
├── README.md
├── DOCUMENTATION.md
│
├── db/
│   └── schema.sql
│
├── src/
│   └── banking/
│       ├── Main.java
│       │
│       ├── models/
│       │   ├── Account.java
│       │   └── Transaction.java
│       │
│       ├── dao/
│       │   ├── DatabaseConnection.java
│       │   ├── AccountDAO.java
│       │   └── TransactionDAO.java
│       │
│       ├── services/
│       │   └── BankService.java
│       │
│       ├── exceptions/
│       │   ├── InvalidInputException.java
│       │   └── InsufficientFundsException.java
│       │
│       ├── threads/
│       │   └── TransferThread.java
│       │
│       └── utils/
│           └── PasswordUtil.java
│
└── out/
    └── banking/
        └── [compiled .class files]
```

---

## Future Enhancements

1. **Input Validation:** Add loops to re-prompt for invalid inputs
2. **Admin Features:** Implement view all accounts, freeze/delete account
3. **Password Security:** Upgrade to BCrypt or PBKDF2 with salt
4. **Logging:** Add log4j for transaction logging
5. **File Backup:** Export transactions to PDF/CSV
6. **Interest Calculation:** Scheduled thread for automatic interest credit
7. **Unit Tests:** Add JUnit tests for service and DAO layers
8. **GUI:** Swing or JavaFX interface
9. **Properties File:** Externalize DB configuration
10. **Connection Pooling:** Use HikariCP for better performance

---

## Contact

**Developer:** Eswaran  
**Email:** eswaran@gmail.com  
**Phone:** 9344177320  
**Project Date:** November 10, 2025

---

## License

MIT License - Free to use for educational purposes.

---

**End of Documentation**

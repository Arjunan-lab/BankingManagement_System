# Banking Management System

A full-featured **CLI-based Banking Management System** built with **Java**, **JDBC**, and **MySQL**. The application demonstrates core Java concepts including OOP, multithreading, exception handling, and secure password storage while providing real-world banking operations like account creation, deposits, withdrawals, fund transfers, and transaction history.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Architecture](#architecture)
- [Database Schema](#database-schema)
- [Prerequisites](#prerequisites)
- [Setup & Installation](#setup--installation)
- [Usage](#usage)
- [Java Concepts Demonstrated](#java-concepts-demonstrated)
- [Screenshots / Sample Execution](#screenshots--sample-execution)
- [License](#license)

---

## Features

### User Features
| Feature | Description |
|---|---|
| **Create Account** | Register with name, email, phone, password, and initial deposit |
| **Login** | Authenticate via account number + password (SHA-256 hashed) |
| **Deposit** | Add funds to your account |
| **Withdraw** | Withdraw funds with balance validation |
| **Transfer** | Transfer money between accounts (thread-based concurrency) |
| **View Balance** | Check current account balance |
| **Transaction History** | View all past transactions (deposits, withdrawals, transfers) |

### Admin Features
| Feature | Description |
|---|---|
| **View All Accounts** | List every account with ID, name, email, phone, balance, and status |
| **Search Account by ID** | Look up detailed info for a specific account |
| **View Account Transactions** | Inspect any account's transaction history |

### Security
- Passwords are hashed using **SHA-256** before storage — plaintext passwords are never persisted
- Admin access is protected by a separate admin password
- Prepared statements prevent SQL injection

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Java 17+** | Core programming language |
| **JDBC** | Database connectivity |
| **MySQL** | Relational database (via WAMP/XAMPP/standalone) |
| **MySQL Connector/J** | JDBC driver for MySQL |
| **SHA-256** | Password hashing |
| **IntelliJ IDEA** | Recommended IDE |

---

## Project Structure

```
java_jdpc_banking_project/
├── db/
│   └── schema.sql                        # Database creation script
├── src/
│   └── banking/
│       ├── Main.java                     # Entry point – menus & user interaction
│       ├── dao/
│       │   ├── DatabaseConnection.java   # JDBC connection factory
│       │   ├── AccountDAO.java           # CRUD operations for accounts
│       │   └── TransactionDAO.java       # CRUD operations for transactions
│       ├── exceptions/
│       │   ├── InsufficientFundsException.java
│       │   └── InvalidInputException.java
│       ├── models/
│       │   ├── Account.java              # Account entity (POJO)
│       │   └── Transaction.java          # Transaction entity (POJO)
│       ├── services/
│       │   └── BankService.java          # Business logic layer
│       ├── threads/
│       │   └── TransferThread.java       # Multithreaded fund transfer
│       └── utils/
│           └── PasswordUtil.java         # SHA-256 password hashing utility
├── DOCUMENTATION.md                      # Extended project documentation
└── java_jdpc_banking_project.iml         # IntelliJ module file
```

---

## Architecture

The project follows a **layered architecture** pattern:

```
┌──────────────────────────────────┐
│           Main.java              │  ← Presentation / CLI Layer
│      (User & Admin Menus)        │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│         BankService.java         │  ← Business Logic / Service Layer
│  (Validation, Transactions, etc) │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│   AccountDAO / TransactionDAO    │  ← Data Access Layer (DAO)
│     DatabaseConnection.java      │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│          MySQL (bank_db)         │  ← Persistence Layer
│   accounts | transactions tables │
└──────────────────────────────────┘
```

**Key design decisions:**
- **DAO Pattern**: Separates database operations from business logic
- **Service Layer**: Manages transaction boundaries with manual commit/rollback
- **Thread Layer**: Fund transfers run on a dedicated `TransferThread` to demonstrate concurrency
- **Utility Layer**: Centralized password hashing via `PasswordUtil`

---

## Database Schema

The application uses a MySQL database named `bank_db` with two tables:

### `accounts`
| Column | Type | Constraints |
|---|---|---|
| `account_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `name` | VARCHAR(50) | NOT NULL |
| `email` | VARCHAR(50) | NOT NULL, UNIQUE |
| `phone` | VARCHAR(15) | — |
| `password` | VARCHAR(255) | NOT NULL (SHA-256 hash) |
| `balance` | DOUBLE | DEFAULT 0 |
| `created_at` | DATETIME | DEFAULT CURRENT_TIMESTAMP |
| `status` | ENUM('ACTIVE','FROZEN','CLOSED') | DEFAULT 'ACTIVE' |

### `transactions`
| Column | Type | Constraints |
|---|---|---|
| `transaction_id` | INT | PRIMARY KEY, AUTO_INCREMENT |
| `account_id` | INT | FOREIGN KEY → accounts(account_id), ON DELETE CASCADE |
| `type` | VARCHAR(20) | NOT NULL (Deposit / Withdraw / Transfer) |
| `amount` | DOUBLE | NOT NULL |
| `timestamp` | DATETIME | DEFAULT CURRENT_TIMESTAMP |
| `remarks` | VARCHAR(255) | — |

### ER Diagram

```
┌──────────────┐          ┌──────────────────┐
│   accounts   │ 1──────M │  transactions    │
│──────────────│          │──────────────────│
│ account_id   │◄─────────│ account_id (FK)  │
│ name         │          │ transaction_id   │
│ email        │          │ type             │
│ phone        │          │ amount           │
│ password     │          │ timestamp        │
│ balance      │          │ remarks          │
│ created_at   │          └──────────────────┘
│ status       │
└──────────────┘
```

---

## Prerequisites

1. **Java JDK 17** or higher installed  
2. **MySQL Server** (via WAMP, XAMPP, or standalone MySQL)  
3. **MySQL Connector/J** JAR file (JDBC driver) — [Download](https://dev.mysql.com/downloads/connector/j/)  
4. **IntelliJ IDEA** (recommended) or any Java IDE / terminal  

---

## Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/Arjunan-lab/BankingManagement_System.git
cd BankingManagement_System
```

### 2. Set Up the Database

Start your MySQL server, then execute the schema script:

```bash
mysql -u root -p < java_jdpc_banking_project/db/schema.sql
```

Or open your MySQL client and run:

```sql
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

### 3. Configure Database Connection

Edit `src/banking/dao/DatabaseConnection.java` if your MySQL credentials differ from the defaults:

```java
private static final String URL  = "jdbc:mysql://localhost:3306/bank_db?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "";   // your MySQL password
```

### 4. Add JDBC Driver

- Download **mysql-connector-j-x.x.x.jar** from the [official site](https://dev.mysql.com/downloads/connector/j/)
- **IntelliJ**: File → Project Structure → Libraries → Add JAR → select the connector JAR
- **Command line**: Include it in the classpath when compiling/running

### 5. Compile & Run

**Using IntelliJ IDEA:**
- Open the project folder
- Mark `src/` as Sources Root
- Run `banking.Main`

**Using command line:**

```bash
# Compile
javac -cp ".;mysql-connector-j-x.x.x.jar" -d out src/banking/**/*.java src/banking/Main.java

# Run
java -cp "out;mysql-connector-j-x.x.x.jar" banking.Main
```

> **Note:** On Linux/macOS, replace `;` with `:` in the classpath separator.

---

## Usage

### Main Menu

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose:
```

### Creating an Account

```
Name: John Doe
Email: john@example.com
Phone: 9876543210
Password: mypassword
Initial deposit: 5000
Account created. Your account number: 1
```

### User Menu (after login)

```
--- User Menu (Account: 1) ---
1. Deposit
2. Withdraw
3. Transfer
4. Balance
5. Transactions
0. Logout
```

### Admin Menu (password: `admin123`)

```
--- Admin Menu ---
1. View All Accounts
2. Search Account by ID
3. View Account Transactions
0. Logout
```

---

## Java Concepts Demonstrated

| Concept | Implementation |
|---|---|
| **OOP – Encapsulation** | Model classes (`Account`, `Transaction`) with private fields and getters/setters |
| **OOP – Abstraction** | Service layer abstracts business logic from the DAO and UI layers |
| **Exception Handling** | Custom exceptions: `InsufficientFundsException`, `InvalidInputException`; try-catch-finally blocks throughout |
| **JDBC** | `PreparedStatement`, `ResultSet`, manual transaction management (`commit`/`rollback`), connection pooling basics |
| **Multithreading** | `TransferThread` extends `Thread` for concurrent fund transfers; `join()` for synchronization |
| **Collections (ArrayList)** | Transaction history and account lists stored/returned as `List<T>` |
| **Password Security** | SHA-256 hashing via `MessageDigest` in `PasswordUtil` |
| **DAO Pattern** | `AccountDAO` and `TransactionDAO` separate DB access from business logic |
| **Layered Architecture** | Clean separation: Presentation → Service → DAO → Database |

---

## Screenshots / Sample Execution

### Account Creation & Login Flow

```
=== Banking Management System ===
1. Create Account
2. Login
3. Admin Login
0. Exit
Choose: 1
Name: Alice
Email: alice@bank.com
Phone: 1234567890
Password: secure123
Initial deposit: 10000
Account created. Your account number: 1

Choose: 2
Account number: 1
Password: secure123

--- User Menu (Account: 1) ---
Choose: 4
Balance: 10000.0

Choose: 1
Amount to deposit: 5000
Deposited. New balance: 15000.0

Choose: 5
Transactions:
1 | Deposit | 10000.0 | 2025-11-10T10:00:00 | Deposit via CLI
2 | Deposit | 5000.0  | 2025-11-10T10:01:00 | Deposit via CLI
```

### Fund Transfer (Threaded)

```
Choose: 3
Target account: 2
Amount: 2000
Transfer successful: 2000.0 from 1 to 2
After transfer, balance: 13000.0
```

### Admin View

```
=== All Accounts ===
--------------------------------------------------------------------------------
ID       Name                 Email                     Phone           Balance      Status
--------------------------------------------------------------------------------
1        Alice                alice@bank.com            1234567890      $13000.00    ACTIVE
2        Bob                  bob@bank.com              9876543210      $7000.00     ACTIVE
--------------------------------------------------------------------------------
Total accounts: 2
```

---

## License

This project is licensed under the [MIT License](LICENSE).

---

> **Author**: Eswaran  
> **Repository**: [github.com/Arjunan-lab/BankingManagement_System](https://github.com/Arjunan-lab/BankingManagement_System)

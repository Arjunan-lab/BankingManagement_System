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
        System.out.println("Admin logged in successfully!");

        while (true) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. View All Accounts");
            System.out.println("2. Search Account by ID");
            System.out.println("3. View Account Transactions");
            System.out.println("0. Logout");
            System.out.print("Choose: ");
            String ch = scanner.nextLine();

            try {
                switch (ch) {
                    case "1": viewAllAccounts(); break;
                    case "2": searchAccount(); break;
                    case "3": adminViewTransactions(); break;
                    case "0":
                        System.out.println("Admin logged out.");
                        return;
                    default: System.out.println("Invalid choice");
                }
            } catch (SQLException ex) {
                System.out.println("Database error: " + ex.getMessage());
            }
        }
    }

    private static void viewAllAccounts() throws SQLException {
        List<Account> accounts = service.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
            return;
        }

        System.out.println("\n=== All Accounts ===");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-8s %-20s %-25s %-15s %-12s %-10s%n",
            "ID", "Name", "Email", "Phone", "Balance", "Status");
        System.out.println("--------------------------------------------------------------------------------");

        for (Account acc : accounts) {
            System.out.printf("%-8d %-20s %-25s %-15s $%-11.2f %-10s%n",
                acc.getAccountId(),
                acc.getName().length() > 20 ? acc.getName().substring(0, 17) + "..." : acc.getName(),
                acc.getEmail().length() > 25 ? acc.getEmail().substring(0, 22) + "..." : acc.getEmail(),
                acc.getPhone(),
                acc.getBalance(),
                acc.getStatus());
        }
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Total accounts: " + accounts.size());
    }

    private static void searchAccount() throws SQLException {
        System.out.print("Enter account ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            Account acc = service.login(id, "");  // We'll use a better approach

            // Direct query is better for admin
            List<Account> allAccounts = service.getAllAccounts();
            Account found = null;
            for (Account a : allAccounts) {
                if (a.getAccountId() == id) {
                    found = a;
                    break;
                }
            }

            if (found == null) {
                System.out.println("Account not found.");
                return;
            }

            System.out.println("\n=== Account Details ===");
            System.out.println("Account ID: " + found.getAccountId());
            System.out.println("Name: " + found.getName());
            System.out.println("Email: " + found.getEmail());
            System.out.println("Phone: " + found.getPhone());
            System.out.println("Balance: $" + String.format("%.2f", found.getBalance()));
            System.out.println("Status: " + found.getStatus());
            System.out.println("Created At: " + found.getCreatedAt());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid account ID format.");
        }
    }

    private static void adminViewTransactions() throws SQLException {
        System.out.print("Enter account ID: ");
        try {
            int id = Integer.parseInt(scanner.nextLine());
            List<Transaction> transactions = service.getTransactions(id);

            if (transactions.isEmpty()) {
                System.out.println("No transactions found for this account.");
                return;
            }

            System.out.println("\n=== Transactions for Account " + id + " ===");
            System.out.println("--------------------------------------------------------------------------------");
            System.out.printf("%-8s %-12s %-12s %-25s %-30s%n",
                "TX ID", "Type", "Amount", "Timestamp", "Remarks");
            System.out.println("--------------------------------------------------------------------------------");

            for (Transaction t : transactions) {
                System.out.printf("%-8d %-12s $%-11.2f %-25s %-30s%n",
                    t.getTransactionId(),
                    t.getType(),
                    t.getAmount(),
                    t.getTimestamp().toString(),
                    t.getRemarks().length() > 30 ? t.getRemarks().substring(0, 27) + "..." : t.getRemarks());
            }
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Total transactions: " + transactions.size());
        } catch (NumberFormatException ex) {
            System.out.println("Invalid account ID format.");
        }
    }
}

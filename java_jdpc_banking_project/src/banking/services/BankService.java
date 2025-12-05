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

    public List<Account> getAllAccounts() throws SQLException {
        return accountDAO.findAll();
    }
}

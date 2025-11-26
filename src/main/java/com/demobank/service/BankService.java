package com.demobank.service;

import com.demobank.entity.User;
import com.demobank.entity.Account;
import com.demobank.entity.CreditApplication;
import com.demobank.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BankService {
    
    @Autowired
    private DataSource dataSource;
    
    /**
     * User authentication method
     */
    public User authenticateUser(String username, String password) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            
            System.out.println("Executing SQL: " + sql);
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setEmail(rs.getString("email"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get user accounts
     */
    public List<Account> getUserAccounts(String userId) {
        List<Account> accounts = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE user_id = " + userId;
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setUserId(rs.getLong("user_id"));
                account.setAccountType(rs.getString("account_type"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountNumber(rs.getString("account_number"));
                accounts.add(account);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
    }
    
    /**
     * Money transfer method
     */
    public boolean transferMoney(String fromAccountId, String toAccountId, String amount) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            // Get account details before transfer
            Account fromAccount = getAccountById(fromAccountId);
            Account toAccount = getAccountById(toAccountId);
            
            if (fromAccount == null || toAccount == null) {
                return false;
            }
            
            BigDecimal transferAmount = new BigDecimal(amount);
            
            String debitSql = "UPDATE accounts SET balance = balance - " + amount + 
                             " WHERE id = " + fromAccountId;
            String creditSql = "UPDATE accounts SET balance = balance + " + amount + 
                              " WHERE id = " + toAccountId;
            
            System.out.println("Executing SQL: " + debitSql);
            System.out.println("Executing SQL: " + creditSql);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(debitSql);
            stmt.executeUpdate(creditSql);
            
            // Record transactions
            BigDecimal fromBalanceAfter = fromAccount.getBalance().subtract(transferAmount);
            BigDecimal toBalanceAfter = toAccount.getBalance().add(transferAmount);
            
            String insertFromTransaction = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                                         "transaction_date, description, reference_account_number, balance_after) VALUES (" +
                                         fromAccountId + ", 'TRANSFER_OUT', " + amount + ", CURRENT_TIMESTAMP, " +
                                         "'Transfer to " + toAccount.getAccountNumber() + "', '" + 
                                         toAccount.getAccountNumber() + "', " + fromBalanceAfter + ")";
            
            String insertToTransaction = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                                       "transaction_date, description, reference_account_number, balance_after) VALUES (" +
                                       toAccountId + ", 'TRANSFER_IN', " + amount + ", CURRENT_TIMESTAMP, " +
                                       "'Transfer from " + fromAccount.getAccountNumber() + "', '" + 
                                       fromAccount.getAccountNumber() + "', " + toBalanceAfter + ")";
            
            stmt.executeUpdate(insertFromTransaction);
            stmt.executeUpdate(insertToTransaction);
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Credit application search
     */
    public List<CreditApplication> searchCreditApplications(String searchTerm) {
        List<CreditApplication> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM credit_applications WHERE employment_status LIKE ? OR comments LIKE ?";
            
            System.out.println("Executing SQL: " + sql);
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, "%" + searchTerm + "%");
            stmt.setString(2, "%" + searchTerm + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CreditApplication app = new CreditApplication();
                app.setId(rs.getLong("id"));
                app.setUserId(rs.getLong("user_id"));
                app.setRequestedLimit(rs.getBigDecimal("requested_limit"));
                app.setAnnualIncome(rs.getBigDecimal("annual_income"));
                app.setEmploymentStatus(rs.getString("employment_status"));
                app.setStatus(rs.getString("status"));
                app.setComments(rs.getString("comments"));
                applications.add(app);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return applications;
    }
    
    /**
     * Create a new credit application
     */
    public boolean createCreditApplication(String userId, String requestedLimit, 
                                         String annualIncome, String employmentStatus, String comments) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO credit_applications (user_id, requested_limit, annual_income, " +
                        "employment_status, status, application_date, comments) VALUES (" +
                        userId + ", " + requestedLimit + ", " + annualIncome + ", '" +
                        employmentStatus + "', 'PENDING', CURRENT_TIMESTAMP, '" + comments + "')";
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            int result = stmt.executeUpdate(sql);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get account by account number
     */
    public Account getAccountByNumber(String accountNumber) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE account_number = ?";
            
            System.out.println("Executing SQL: " + sql);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, accountNumber);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setUserId(rs.getLong("user_id"));
                account.setAccountType(rs.getString("account_type"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountNumber(rs.getString("account_number"));
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get account by ID
     */
    public Account getAccountById(String accountId) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM accounts WHERE id = " + accountId;
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setUserId(rs.getLong("user_id"));
                account.setAccountType(rs.getString("account_type"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountNumber(rs.getString("account_number"));
                return account;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get transaction history for user accounts with search and filtering
     */
    public List<Transaction> getTransactionHistory(String userId, String searchTerm, 
                                                  String transactionType, String startDate, 
                                                  String endDate, String minAmount, String maxAmount) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT t.*, a.account_number FROM transactions t " +
                "JOIN accounts a ON t.account_id = a.id " +
                "WHERE a.user_id = " + userId
            );
            
            // Add search filters
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                sql.append(" AND (t.description LIKE '%").append(searchTerm).append("%'")
                   .append(" OR t.reference_account_number LIKE '%").append(searchTerm).append("%')")
                   .append(" OR a.account_number LIKE '%").append(searchTerm).append("%')");
            }
            
            if (transactionType != null && !transactionType.trim().isEmpty() && !"ALL".equals(transactionType)) {
                sql.append(" AND t.transaction_type = '").append(transactionType).append("'");
            }
            
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append(" AND DATE(t.transaction_date) >= '").append(startDate).append("'");
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append(" AND DATE(t.transaction_date) <= '").append(endDate).append("'");
            }
            
            if (minAmount != null && !minAmount.trim().isEmpty()) {
                sql.append(" AND t.amount >= ").append(minAmount);
            }
            
            if (maxAmount != null && !maxAmount.trim().isEmpty()) {
                sql.append(" AND t.amount <= ").append(maxAmount);
            }
            
            sql.append(" ORDER BY t.transaction_date DESC");
            
            System.out.println("Executing SQL: " + sql.toString());
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql.toString());
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getLong("id"));
                transaction.setAccountId(rs.getLong("account_id"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                transaction.setDescription(rs.getString("description"));
                transaction.setReferenceAccountNumber(rs.getString("reference_account_number"));
                transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Get recent transactions for dashboard
     */
    public List<Transaction> getRecentTransactions(String userId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT t.*, a.account_number FROM transactions t " +
                        "JOIN accounts a ON t.account_id = a.id " +
                        "WHERE a.user_id = " + userId + " " +
                        "ORDER BY t.transaction_date DESC LIMIT " + limit;
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getLong("id"));
                transaction.setAccountId(rs.getLong("account_id"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                transaction.setDescription(rs.getString("description"));
                transaction.setReferenceAccountNumber(rs.getString("reference_account_number"));
                transaction.setBalanceAfter(rs.getBigDecimal("balance_after"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
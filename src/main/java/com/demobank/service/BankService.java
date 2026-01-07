package com.demobank.service;

import com.demobank.entity.User;
import com.demobank.entity.Account;
import com.demobank.entity.CreditApplication;
import com.demobank.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
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
            String sql = "SELECT * FROM users WHERE username = '" + username + 
                        "' AND password = '" + password + "'";
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
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
    public boolean transferMoney(String fromAccountId, String toAccountId, String amount, String memo) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
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
            String fromDescription = memo != null && !memo.trim().isEmpty() 
                ? "Transfer to account " + toAccountId + " - " + memo 
                : "Transfer to account " + toAccountId;
            String toDescription = memo != null && !memo.trim().isEmpty() 
                ? "Transfer from account " + fromAccountId + " - " + memo 
                : "Transfer from account " + fromAccountId;
            
            String fromTransactionSql = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                                       "transaction_date, description, related_account_id) VALUES (" +
                                       fromAccountId + ", 'TRANSFER', " + amount + ", CURRENT_TIMESTAMP, '" +
                                       fromDescription.replace("'", "''") + "', " + toAccountId + ")";
            String toTransactionSql = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                                     "transaction_date, description, related_account_id) VALUES (" +
                                     toAccountId + ", 'TRANSFER', " + amount + ", CURRENT_TIMESTAMP, '" +
                                     toDescription.replace("'", "''") + "', " + fromAccountId + ")";
            
            System.out.println("Executing SQL: " + fromTransactionSql);
            System.out.println("Executing SQL: " + toTransactionSql);
            
            stmt.executeUpdate(fromTransactionSql);
            stmt.executeUpdate(toTransactionSql);
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Overloaded method for backward compatibility
     */
    public boolean transferMoney(String fromAccountId, String toAccountId, String amount) {
        return transferMoney(fromAccountId, toAccountId, amount, null);
    }
    
    /**
     * Credit application search
     */
    public List<CreditApplication> searchCreditApplications(String searchTerm) {
        List<CreditApplication> applications = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT * FROM credit_applications WHERE employment_status LIKE '%" + 
                        searchTerm + "%' OR comments LIKE '%" + searchTerm + "%'";
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
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
            String sql = "SELECT * FROM accounts WHERE account_number = '" + accountNumber + "'";
            
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
     * Get all transactions for a user's accounts
     */
    public List<Transaction> getUserTransactions(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT t.* FROM transactions t " +
                        "INNER JOIN accounts a ON t.account_id = a.id " +
                        "WHERE a.user_id = " + userId + " " +
                        "ORDER BY t.transaction_date DESC";
            
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
                if (rs.getObject("related_account_id") != null) {
                    transaction.setRelatedAccountId(rs.getLong("related_account_id"));
                }
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Search and filter transactions
     */
    public List<Transaction> searchTransactions(String userId, String minAmount, String maxAmount, 
                                                String startDate, String endDate, 
                                                String description, String transactionType) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT t.* FROM transactions t ");
            sql.append("INNER JOIN accounts a ON t.account_id = a.id ");
            sql.append("WHERE a.user_id = ").append(userId);
            
            if (minAmount != null && !minAmount.trim().isEmpty()) {
                sql.append(" AND t.amount >= ").append(minAmount);
            }
            if (maxAmount != null && !maxAmount.trim().isEmpty()) {
                sql.append(" AND t.amount <= ").append(maxAmount);
            }
            if (startDate != null && !startDate.trim().isEmpty()) {
                sql.append(" AND t.transaction_date >= '").append(startDate).append("'");
            }
            if (endDate != null && !endDate.trim().isEmpty()) {
                sql.append(" AND t.transaction_date <= '").append(endDate).append(" 23:59:59'");
            }
            if (description != null && !description.trim().isEmpty()) {
                sql.append(" AND t.description LIKE '%").append(description.replace("'", "''")).append("%'");
            }
            if (transactionType != null && !transactionType.trim().isEmpty() && !transactionType.equals("ALL")) {
                sql.append(" AND t.transaction_type = '").append(transactionType).append("'");
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
                if (rs.getObject("related_account_id") != null) {
                    transaction.setRelatedAccountId(rs.getLong("related_account_id"));
                }
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
}
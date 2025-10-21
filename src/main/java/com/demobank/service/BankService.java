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
import java.time.format.DateTimeFormatter;
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
            java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE user_id = ?");
            stmt.setString(1, userId);
            java.sql.ResultSet rs = stmt.executeQuery();
            
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
            
            // Get account details for transaction logging
            Account fromAccount = getAccountById(fromAccountId);
            Account toAccount = getAccountById(toAccountId);
            
            java.sql.PreparedStatement stmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
                      stmt.setString(1, amount);
                      stmt.setString(2, fromAccountId);
                      stmt.executeUpdate();
          
                      stmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
                      stmt.setString(1, amount);
                      stmt.setString(2, toAccountId);
                      stmt.executeUpdate();
            
            // Record transactions
            if (fromAccount != null && toAccount != null) {
                String description = "Transfer to account " + toAccount.getAccountNumber();
                recordTransaction(fromAccountId, "TRANSFER", amount, description, toAccountId, toAccount.getAccountNumber());
                
                description = "Transfer from account " + fromAccount.getAccountNumber();
                recordTransaction(toAccountId, "TRANSFER", amount, description, fromAccountId, fromAccount.getAccountNumber());
            }
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Record a transaction in the database
     */
    private void recordTransaction(String accountId, String transactionType, String amount, 
                                   String description, String relatedAccountId, String relatedAccountNumber) {
        try (Connection conn = dataSource.getConnection()) {
            java.sql.PreparedStatement stmt = conn.prepareStatement("INSERT INTO transactions (account_id, transaction_type, amount, transaction_date, " +
                        "description, related_account_id, related_account_number, status) VALUES (?, ?, ?, CURRENT_TIMESTAMP, ?, ?, ?, 'COMPLETED')");
            stmt.setString(1, accountId);
            stmt.setString(2, transactionType);
            stmt.setString(3, amount);
            stmt.setString(4, description);
            if (relatedAccountId != null) {
                stmt.setString(5, relatedAccountId);
            } else {
                stmt.setNull(5, java.sql.Types.VARCHAR);
            }
            stmt.setString(6, relatedAccountNumber != null ? relatedAccountNumber : "");
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get account by ID
     */
    private Account getAccountById(String accountId) {
        try (Connection conn = dataSource.getConnection()) {
            java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE id = ?");
            stmt.setString(1, accountId);
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
            java.sql.PreparedStatement stmt = conn.prepareStatement("INSERT INTO credit_applications (user_id, requested_limit, annual_income, " +
                        "employment_status, status, application_date, comments) VALUES (?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP, ?)");
            stmt.setString(1, userId);
            stmt.setString(2, requestedLimit);
            stmt.setString(3, annualIncome);
            stmt.setString(4, employmentStatus);
            stmt.setString(5, comments);

            int result = stmt.executeUpdate();
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
            java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT * FROM accounts WHERE account_number = ?");
            stmt.setString(1, accountNumber);

            System.out.println("Executing SQL: " + "SELECT * FROM accounts WHERE account_number = ?");

            java.sql.ResultSet rs = stmt.executeQuery();
            
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
                        "JOIN accounts a ON t.account_id = a.id " +
                        "WHERE a.user_id = ? " + 
                        "ORDER BY t.transaction_date DESC";
            
            System.out.println("Executing SQL: " + sql);
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Search and filter transactions with multiple criteria
     */
    public List<Transaction> searchTransactions(String userId, String transactionType, 
                                               String minAmount, String maxAmount,
                                               String startDate, String endDate, 
                                               String searchTerm) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT t.* FROM transactions t ");
            sql.append("JOIN accounts a ON t.account_id = a.id ");
            sql.append("WHERE a.user_id = ?");
            
            List<Object> parameters = new ArrayList<>();
            parameters.add(userId);
            
            // Add transaction type filter
            if (transactionType != null && !transactionType.isEmpty() && !transactionType.equals("ALL")) {
                sql.append(" AND t.transaction_type = ?");
                parameters.add(transactionType);
            }
            
            // Add amount range filters
            if (minAmount != null && !minAmount.isEmpty()) {
                sql.append(" AND t.amount >= ?");
                parameters.add(minAmount);
            }
            if (maxAmount != null && !maxAmount.isEmpty()) {
                sql.append(" AND t.amount <= ?");
                parameters.add(maxAmount);
            }
            
            // Add date range filters
            if (startDate != null && !startDate.isEmpty()) {
                sql.append(" AND t.transaction_date >= ?");
                parameters.add(startDate);
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql.append(" AND t.transaction_date <= ?");
                parameters.add(endDate + " 23:59:59");
            }
            
            // Add description search
            if (searchTerm != null && !searchTerm.isEmpty()) {
                sql.append(" AND t.description LIKE ?");
                parameters.add("%" + searchTerm + "%");
            }
            
            sql.append(" ORDER BY t.transaction_date DESC");
            
            System.out.println("Executing SQL: " + sql.toString());
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = mapResultSetToTransaction(rs);
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Helper method to map ResultSet to Transaction object
     */
    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setId(rs.getLong("id"));
        transaction.setAccountId(rs.getLong("account_id"));
        transaction.setTransactionType(rs.getString("transaction_type"));
        transaction.setAmount(rs.getBigDecimal("amount"));
        transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
        transaction.setDescription(rs.getString("description"));
        transaction.setRelatedAccountId(rs.getObject("related_account_id", Long.class));
        transaction.setRelatedAccountNumber(rs.getString("related_account_number"));
        transaction.setStatus(rs.getString("status"));
        return transaction;
    }
}
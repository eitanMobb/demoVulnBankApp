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
    public boolean transferMoney(String fromAccountId, String toAccountId, String amount) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            java.sql.PreparedStatement debitStmt = conn.prepareStatement("UPDATE accounts SET balance = balance - ? WHERE id = ?");
            debitStmt.setBigDecimal(1, new java.math.BigDecimal(amount));
            debitStmt.setString(2, fromAccountId);
            java.sql.PreparedStatement creditStmt = conn.prepareStatement("UPDATE accounts SET balance = balance + ? WHERE id = ?");
            creditStmt.setBigDecimal(1, new java.math.BigDecimal(amount));
            creditStmt.setString(2, toAccountId);

            System.out.println("Executing SQL: UPDATE accounts SET balance = balance - ? WHERE id = ?");
            System.out.println("Executing SQL: UPDATE accounts SET balance = balance + ? WHERE id = ?");

            debitStmt.executeUpdate();
            creditStmt.executeUpdate();
            
            // Create transaction records
            createTransactionRecord(fromAccountId, "TRANSFER_OUT", amount, 
                                  "Transfer to account " + toAccountId, toAccountId, "COMPLETED");
            createTransactionRecord(toAccountId, "TRANSFER_IN", amount, 
                                  "Transfer from account " + fromAccountId, fromAccountId, "COMPLETED");
            
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

            System.out.println("Executing SQL: " + stmt.toString());

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
     * Create a transaction record
     */
    private void createTransactionRecord(String accountId, String transactionType, 
                                        String amount, String description, 
                                        String relatedAccountId, String status) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO transactions (account_id, transaction_type, amount, " +
                        "transaction_date, description, related_account_id, status) VALUES (" +
                        accountId + ", '" + transactionType + "', " + amount + ", CURRENT_TIMESTAMP, '" +
                        description + "', " + relatedAccountId + ", '" + status + "')";
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Get transactions for a user's accounts
     */
    public List<Transaction> getUserTransactions(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            java.sql.PreparedStatement stmt = conn.prepareStatement("SELECT t.* FROM transactions t " +
                        "INNER JOIN accounts a ON t.account_id = a.id " +
                        "WHERE a.user_id = ? " +
                        "ORDER BY t.transaction_date DESC");
            stmt.setString(1, userId);

            System.out.println("Executing SQL: " + stmt.toString());

            java.sql.ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getLong("id"));
                transaction.setAccountId(rs.getLong("account_id"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                transaction.setDescription(rs.getString("description"));
                transaction.setRelatedAccountId(rs.getLong("related_account_id"));
                transaction.setStatus(rs.getString("status"));
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
    public List<Transaction> searchTransactions(String userId, String transactionType, 
                                               String minAmount, String maxAmount,
                                               String startDate, String endDate, 
                                               String description) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT t.* FROM transactions t " +
                "INNER JOIN accounts a ON t.account_id = a.id " +
                "WHERE a.user_id = " + userId
            );
            
            // Add filters dynamically
            if (transactionType != null && !transactionType.isEmpty() && !transactionType.equals("ALL")) {
                sql.append(" AND t.transaction_type = '").append(transactionType).append("'");
            }
            
            if (minAmount != null && !minAmount.isEmpty()) {
                sql.append(" AND t.amount >= ").append(minAmount);
            }
            
            if (maxAmount != null && !maxAmount.isEmpty()) {
                sql.append(" AND t.amount <= ").append(maxAmount);
            }
            
            if (startDate != null && !startDate.isEmpty()) {
                sql.append(" AND t.transaction_date >= '").append(startDate).append(" 00:00:00'");
            }
            
            if (endDate != null && !endDate.isEmpty()) {
                sql.append(" AND t.transaction_date <= '").append(endDate).append(" 23:59:59'");
            }
            
            if (description != null && !description.isEmpty()) {
                sql.append(" AND t.description LIKE '%").append(description).append("%'");
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
                transaction.setRelatedAccountId(rs.getLong("related_account_id"));
                transaction.setStatus(rs.getString("status"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Get account by ID
     */
    public Account getAccountById(Long accountId) {
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
}
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
            String sql = "SELECT * FROM accounts WHERE user_id = ?";
            
            System.out.println("Executing SQL: " + sql);
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setInt(1, Math.round(Float.parseFloat(userId)));
            } catch (NumberFormatException e) {
                // MOBB: consider printing this message to logger: mobb-62012408214a8695f52cf159a96ab273: Failed to convert input to type integer

                // MOBB: using a default value for the SQL parameter in case the input is not convertible.
                // This is important for preventing users from causing a denial of service to this application by throwing an exception here.
                stmt.setInt(1, 0);
            }
            ResultSet rs = stmt.executeQuery();
            
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
            
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            
            System.out.println("Executing SQL: " + debitSql);
            System.out.println("Executing SQL: " + creditSql);
            
            java.sql.PreparedStatement stmt = conn.prepareStatement(debitSql);
            stmt.setString(1, amount);
            stmt.setString(2, fromAccountId);
            stmt.executeUpdate();
            
            stmt = conn.prepareStatement(creditSql);
            stmt.setString(1, amount);
            stmt.setString(2, toAccountId);
            stmt.executeUpdate();
            
            // Create transaction records
            String referenceNumber = "TRF-" + System.currentTimeMillis();
            String transactionDesc = "Transfer between accounts";
            
            // Debit transaction for source account
            String debitTransSql = "INSERT INTO transactions (account_id, transaction_type, amount, description, " +
                                  "transaction_date, reference_number, to_account_id, status) VALUES (" +
                                  fromAccountId + ", 'TRANSFER', -" + amount + ", '" + transactionDesc + "', " +
                                  "CURRENT_TIMESTAMP, '" + referenceNumber + "', " + toAccountId + ", 'COMPLETED')";
            
            // Credit transaction for destination account  
            String creditTransSql = "INSERT INTO transactions (account_id, transaction_type, amount, description, " +
                                   "transaction_date, reference_number, to_account_id, status) VALUES (" +
                                   toAccountId + ", 'TRANSFER', " + amount + ", '" + transactionDesc + "', " +
                                   "CURRENT_TIMESTAMP, '" + referenceNumber + "', " + fromAccountId + ", 'COMPLETED')";
            
            System.out.println("Executing SQL: " + debitTransSql);
            System.out.println("Executing SQL: " + creditTransSql);
            
            stmt = conn.prepareStatement(debitTransSql);
            stmt.executeUpdate();
            
            stmt = conn.prepareStatement(creditTransSql);
            stmt.executeUpdate();
            
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
     * Get transactions for user's accounts with optional filters
     */
    public List<Transaction> getUserTransactions(String userId, String transactionType, 
                                               String fromDate, String toDate, 
                                               String minAmount, String maxAmount, 
                                               String description) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT t.*, a.account_number, a.account_type FROM transactions t ")
                     .append("INNER JOIN accounts a ON t.account_id = a.id ")
                     .append("WHERE a.user_id = ").append(userId);
            
            // Add filters
            if (transactionType != null && !transactionType.isEmpty() && !"ALL".equals(transactionType)) {
                sqlBuilder.append(" AND t.transaction_type = '").append(transactionType).append("'");
            }
            
            if (fromDate != null && !fromDate.isEmpty()) {
                sqlBuilder.append(" AND t.transaction_date >= '").append(fromDate).append("'");
            }
            
            if (toDate != null && !toDate.isEmpty()) {
                sqlBuilder.append(" AND t.transaction_date <= '").append(toDate).append(" 23:59:59'");
            }
            
            if (minAmount != null && !minAmount.isEmpty()) {
                sqlBuilder.append(" AND ABS(t.amount) >= ").append(minAmount);
            }
            
            if (maxAmount != null && !maxAmount.isEmpty()) {
                sqlBuilder.append(" AND ABS(t.amount) <= ").append(maxAmount);
            }
            
            if (description != null && !description.isEmpty()) {
                sqlBuilder.append(" AND t.description LIKE '%").append(description).append("%'");
            }
            
            sqlBuilder.append(" ORDER BY t.transaction_date DESC");
            
            String sql = sqlBuilder.toString();
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getLong("id"));
                transaction.setAccountId(rs.getLong("account_id"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                transaction.setReferenceNumber(rs.getString("reference_number"));
                transaction.setToAccountId(rs.getLong("to_account_id"));
                transaction.setStatus(rs.getString("status"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Get account information for a transaction (for display purposes)
     */
    public Map<String, Object> getTransactionWithAccountInfo(Transaction transaction) {
        Map<String, Object> transactionInfo = new HashMap<>();
        try (Connection conn = dataSource.getConnection()) {
            // Get source account info
            String sql = "SELECT account_number, account_type FROM accounts WHERE id = " + transaction.getAccountId();
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                transactionInfo.put("transaction", transaction);
                transactionInfo.put("accountNumber", rs.getString("account_number"));
                transactionInfo.put("accountType", rs.getString("account_type"));
                
                // For transfers, get destination account info
                if (transaction.getToAccountId() != null && transaction.getToAccountId() > 0) {
                    String destSql = "SELECT account_number, account_type FROM accounts WHERE id = " + transaction.getToAccountId();
                    System.out.println("Executing SQL: " + destSql);
                    
                    ResultSet destRs = stmt.executeQuery(destSql);
                    if (destRs.next()) {
                        transactionInfo.put("toAccountNumber", destRs.getString("account_number"));
                        transactionInfo.put("toAccountType", destRs.getString("account_type"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactionInfo;
    }
    
    /**
     * Create a transaction record
     */
    public boolean createTransaction(Long accountId, String transactionType, BigDecimal amount, 
                                   String description, String referenceNumber, Long toAccountId) {
        try (Connection conn = dataSource.getConnection()) {
            String sql = "INSERT INTO transactions (account_id, transaction_type, amount, description, " +
                        "transaction_date, reference_number, to_account_id, status) VALUES (" +
                        accountId + ", '" + transactionType + "', " + amount + ", '" + description + "', " +
                        "CURRENT_TIMESTAMP, '" + referenceNumber + "', " + 
                        (toAccountId != null ? toAccountId : "NULL") + ", 'COMPLETED')";
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            int result = stmt.executeUpdate(sql);
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
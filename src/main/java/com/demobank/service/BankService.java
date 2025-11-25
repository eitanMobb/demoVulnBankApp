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
        return transferMoney(fromAccountId, toAccountId, amount, "Money Transfer Action");
    }
    
    /**
     * Money transfer method with description
     */
    public boolean transferMoney(String fromAccountId, String toAccountId, String amount, String description) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            // Get account details for transaction logging
            String fromAccountSql = "SELECT account_number FROM accounts WHERE id = ?";
            String toAccountSql = "SELECT account_number FROM accounts WHERE id = ?";
            
            //the above might be vulnerable, need to check
            java.sql.PreparedStatement stmt = conn.prepareStatement(fromAccountSql);
            stmt.setString(1, fromAccountId);
            ResultSet fromRs = stmt.executeQuery();
            
            java.sql.PreparedStatement stmt2 = conn.prepareStatement(toAccountSql);
            stmt2.setString(1, toAccountId);
            ResultSet toRs = stmt2.executeQuery();
            
            String fromAccountNumber = "";
            String toAccountNumber = "";
            
            if (fromRs.next()) {
                fromAccountNumber = fromRs.getString("account_number");
            }
            if (toRs.next()) {
                toAccountNumber = toRs.getString("account_number");
            }
            
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ?";
            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
            
            System.out.println("Executing SQL: " + debitSql);
            System.out.println("Executing SQL: " + creditSql);
            
            java.sql.PreparedStatement debitStmt = conn.prepareStatement(debitSql);
            debitStmt.setString(1, amount);
            debitStmt.setString(2, fromAccountId);
            debitStmt.executeUpdate();
            
            java.sql.PreparedStatement creditStmt = conn.prepareStatement(creditSql);
            creditStmt.setString(1, amount);
            creditStmt.setString(2, toAccountId);
            creditStmt.executeUpdate();
            
            // Log the transaction
            String transactionSql = "INSERT INTO transactions (from_account_id, to_account_id, from_account_number, " +
                                  "to_account_number, amount, transaction_type, description, transaction_date, status) " +
                                  "VALUES (?, ?, ?, ?, ?, 'TRANSFER', ?, CURRENT_TIMESTAMP, 'COMPLETED')";
            
            System.out.println("Executing SQL: INSERT INTO transactions (from_account_id, to_account_id, from_account_number, " +
                                  "to_account_number, amount, transaction_type, description, transaction_date, status) " +
                                  "VALUES (?, ?, ?, ?, ?, 'TRANSFER', ?, CURRENT_TIMESTAMP, 'COMPLETED')");
            java.sql.PreparedStatement transStmt = conn.prepareStatement(transactionSql);
            transStmt.setString(1, fromAccountId);
            transStmt.setString(2, toAccountId);
            transStmt.setString(3, fromAccountNumber);
            transStmt.setString(4, toAccountNumber);
            transStmt.setString(5, amount);
            transStmt.setString(6, description);
            transStmt.executeUpdate();
            
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
     * Get transaction history for a user
     */
    public List<Transaction> getUserTransactionHistory(String userId) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            String sql = "SELECT t.* FROM transactions t " +
                        "JOIN accounts a1 ON t.from_account_id = a1.id " +
                        "JOIN accounts a2 ON t.to_account_id = a2.id " +
                        "WHERE a1.user_id = " + userId + " OR a2.user_id = " + userId + " " +
                        "ORDER BY t.transaction_date DESC";
            
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getLong("id"));
                transaction.setFromAccountId(rs.getLong("from_account_id"));
                transaction.setToAccountId(rs.getLong("to_account_id"));
                transaction.setFromAccountNumber(rs.getString("from_account_number"));
                transaction.setToAccountNumber(rs.getString("to_account_number"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                transaction.setStatus(rs.getString("status"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Search transactions with filters
     */
    public List<Transaction> searchTransactions(String userId, String amountMin, String amountMax, 
                                              String dateFrom, String dateTo, String description, String transactionType) {
        List<Transaction> transactions = new ArrayList<>();
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT t.* FROM transactions t ")
                     .append("JOIN accounts a1 ON t.from_account_id = a1.id ")
                     .append("JOIN accounts a2 ON t.to_account_id = a2.id ")
                     .append("WHERE (a1.user_id = ").append(userId).append(" OR a2.user_id = ").append(userId).append(") ");
            
            // Add filters
            if (amountMin != null && !amountMin.isEmpty()) {
                sqlBuilder.append("AND t.amount >= ").append(amountMin).append(" ");
            }
            if (amountMax != null && !amountMax.isEmpty()) {
                sqlBuilder.append("AND t.amount <= ").append(amountMax).append(" ");
            }
            if (dateFrom != null && !dateFrom.isEmpty()) {
                sqlBuilder.append("AND DATE(t.transaction_date) >= '").append(dateFrom).append("' ");
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                sqlBuilder.append("AND DATE(t.transaction_date) <= '").append(dateTo).append("' ");
            }
            if (description != null && !description.isEmpty()) {
                sqlBuilder.append("AND t.description LIKE '%").append(description).append("%' ");
            }
            if (transactionType != null && !transactionType.isEmpty() && !"ALL".equals(transactionType)) {
                sqlBuilder.append("AND t.transaction_type = '").append(transactionType).append("' ");
            }
            
            sqlBuilder.append("ORDER BY t.transaction_date DESC");
            
            String sql = sqlBuilder.toString();
            System.out.println("Executing SQL: " + sql);
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Transaction transaction = new Transaction();
                transaction.setId(rs.getLong("id"));
                transaction.setFromAccountId(rs.getLong("from_account_id"));
                transaction.setToAccountId(rs.getLong("to_account_id"));
                transaction.setFromAccountNumber(rs.getString("from_account_number"));
                transaction.setToAccountNumber(rs.getString("to_account_number"));
                transaction.setAmount(rs.getBigDecimal("amount"));
                transaction.setTransactionType(rs.getString("transaction_type"));
                transaction.setDescription(rs.getString("description"));
                transaction.setTransactionDate(rs.getTimestamp("transaction_date").toLocalDateTime());
                transaction.setStatus(rs.getString("status"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }
    
    /**
     * Create a deposit transaction
     */
    public boolean createDeposit(String accountId, String amount, String description) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            // Get account details
            String accountSql = "SELECT account_number FROM accounts WHERE id = " + accountId;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(accountSql);
            
            String accountNumber = "";
            if (rs.next()) {
                accountNumber = rs.getString("account_number");
            }
            
            // Update balance
            String updateSql = "UPDATE accounts SET balance = balance + " + amount + " WHERE id = " + accountId;
            System.out.println("Executing SQL: " + updateSql);
            stmt.executeUpdate(updateSql);
            
            // Log transaction
            String transactionSql = "INSERT INTO transactions (from_account_id, to_account_id, from_account_number, " +
                                  "to_account_number, amount, transaction_type, description, transaction_date, status) " +
                                  "VALUES (0, " + accountId + ", 'BANK', '" + accountNumber + "', " + amount + 
                                  ", 'DEPOSIT', '" + description + "', CURRENT_TIMESTAMP, 'COMPLETED')";
            
            System.out.println("Executing SQL: " + transactionSql);
            stmt.executeUpdate(transactionSql);
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create a withdrawal transaction
     */
    public boolean createWithdrawal(String accountId, String amount, String description) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            
            // Get account details
            String accountSql = "SELECT account_number FROM accounts WHERE id = " + accountId;
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(accountSql);
            
            String accountNumber = "";
            if (rs.next()) {
                accountNumber = rs.getString("account_number");
            }
            
            // Update balance
            String updateSql = "UPDATE accounts SET balance = balance - " + amount + " WHERE id = " + accountId;
            System.out.println("Executing SQL: " + updateSql);
            stmt.executeUpdate(updateSql);
            
            // Log transaction
            String transactionSql = "INSERT INTO transactions (from_account_id, to_account_id, from_account_number, " +
                                  "to_account_number, amount, transaction_type, description, transaction_date, status) " +
                                  "VALUES (" + accountId + ", 0, '" + accountNumber + "', 'BANK', " + amount + 
                                  ", 'WITHDRAWAL', '" + description + "', CURRENT_TIMESTAMP, 'COMPLETED')";
            
            System.out.println("Executing SQL: " + transactionSql);
            stmt.executeUpdate(transactionSql);
            
            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
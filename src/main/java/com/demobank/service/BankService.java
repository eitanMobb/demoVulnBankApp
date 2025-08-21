package com.demobank.service;

import com.demobank.entity.User;
import com.demobank.entity.Account;
import com.demobank.entity.CreditApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.*;
import java.math.BigDecimal;
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
            
            System.out.println("Executing SQL: ?");
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setInt(1, Math.round(Float.parseFloat(sql)));
            } catch (NumberFormatException e) {
                // MOBB: consider printing this message to logger: mobb-b3891ea9a75d203a33b7786aebff35ea: Failed to convert input to type integer

                // MOBB: using a default value for the SQL parameter in case the input is not convertible.
                // This is important for preventing users from causing a denial of service to this application by throwing an exception here.
                stmt.setInt(1, 0);
            }
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
            
            String debitSql = "UPDATE accounts SET balance = balance - " + amount + 
                             " WHERE id = " + fromAccountId;
            String creditSql = "UPDATE accounts SET balance = balance + " + amount + 
                              " WHERE id = " + toAccountId;
            
            System.out.println("Executing SQL: " + debitSql);
            System.out.println("Executing SQL: " + creditSql);
            
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(debitSql);
            stmt.executeUpdate(creditSql);
            
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
            
            System.out.println("Executing SQL: ?");
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            try {
                stmt.setInt(1, Math.round(Float.parseFloat(sql)));
            } catch (NumberFormatException e) {
                // MOBB: consider printing this message to logger: mobb-b3891ea9a75d203a33b7786aebff35ea: Failed to convert input to type integer

                // MOBB: using a default value for the SQL parameter in case the input is not convertible.
                // This is important for preventing users from causing a denial of service to this application by throwing an exception here.
                stmt.setInt(1, 0);
            }
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
}

package com.demobank.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Service
public class DataInitService implements CommandLineRunner {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public void run(String... args) throws Exception {
        initializeDatabase();
    }
    
    private void initializeDatabase() {
        try (Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            
            // Create and populate users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "username VARCHAR(50) UNIQUE NOT NULL, " +
                        "password VARCHAR(100) NOT NULL, " +
                        "first_name VARCHAR(50) NOT NULL, " +
                        "last_name VARCHAR(50) NOT NULL, " +
                        "email VARCHAR(100) NOT NULL)");
            
            // Insert sample users (passwords are intentionally simple for demo)
            stmt.execute("INSERT INTO users (username, password, first_name, last_name, email) VALUES " +
                        "('alice', 'password123', 'Alice', 'Johnson', 'alice@example.com'), " +
                        "('bob', 'pass456', 'Bob', 'Smith', 'bob@example.com'), " +
                        "('admin', 'admin', 'Admin', 'User', 'admin@demobank.com')");
            
            // Create and populate accounts table
            stmt.execute("CREATE TABLE IF NOT EXISTS accounts (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +
                        "account_type VARCHAR(20) NOT NULL, " +
                        "balance DECIMAL(15,2) NOT NULL, " +
                        "account_number VARCHAR(20) UNIQUE NOT NULL)");
            
            // Insert sample accounts
            stmt.execute("INSERT INTO accounts (user_id, account_type, balance, account_number) VALUES " +
                        "(1, 'CHECKING', 2500.00, 'CHK-001'), " +
                        "(1, 'SAVINGS', 15000.00, 'SAV-001'), " +
                        "(1, 'INVESTMENT', 8750.50, 'INV-001'), " +
                        "(2, 'CHECKING', 1200.75, 'CHK-002'), " +
                        "(2, 'SAVINGS', 5500.25, 'SAV-002'), " +
                        "(2, 'INVESTMENT', 12000.00, 'INV-002')");
            
            // Create credit applications table
            stmt.execute("CREATE TABLE IF NOT EXISTS credit_applications (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "user_id BIGINT NOT NULL, " +
                        "requested_limit DECIMAL(15,2) NOT NULL, " +
                        "annual_income DECIMAL(15,2) NOT NULL, " +
                        "employment_status VARCHAR(50) NOT NULL, " +
                        "status VARCHAR(20) NOT NULL, " +
                        "application_date TIMESTAMP NOT NULL, " +
                        "comments TEXT)");
            
            // Insert sample credit applications
            stmt.execute("INSERT INTO credit_applications (user_id, requested_limit, annual_income, " +
                        "employment_status, status, application_date, comments) VALUES " +
                        "(1, 5000.00, 75000.00, 'Full-time', 'APPROVED', CURRENT_TIMESTAMP, 'Good credit history'), " +
                        "(2, 3000.00, 45000.00, 'Part-time', 'PENDING', CURRENT_TIMESTAMP, 'Recent graduate')");
            
            // Create transactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "account_id BIGINT NOT NULL, " +
                        "transaction_type VARCHAR(20) NOT NULL, " +
                        "amount DECIMAL(15,2) NOT NULL, " +
                        "transaction_date TIMESTAMP NOT NULL, " +
                        "description VARCHAR(500), " +
                        "related_account_id BIGINT)");
            
            // Insert sample transactions
            stmt.execute("INSERT INTO transactions (account_id, transaction_type, amount, transaction_date, description, related_account_id) VALUES " +
                        "(1, 'DEPOSIT', 3200.00, TIMESTAMP '2024-01-14 09:00:00', 'Salary Deposit', NULL), " +
                        "(1, 'WITHDRAWAL', 45.67, TIMESTAMP '2024-01-15 14:30:00', 'Online Purchase', NULL), " +
                        "(2, 'DEPOSIT', 12.34, TIMESTAMP '2024-01-12 08:00:00', 'Interest Payment', NULL), " +
                        "(1, 'TRANSFER', 500.00, TIMESTAMP '2024-01-10 10:15:00', 'Transfer to Savings', 2), " +
                        "(2, 'TRANSFER', 500.00, TIMESTAMP '2024-01-10 10:15:00', 'Transfer from Checking', 1), " +
                        "(1, 'TRANSFER', 1000.00, TIMESTAMP '2024-01-08 11:20:00', 'Transfer to Investment', 3), " +
                        "(3, 'TRANSFER', 1000.00, TIMESTAMP '2024-01-08 11:20:00', 'Transfer from Savings', 2), " +
                        "(1, 'WITHDRAWAL', 250.00, TIMESTAMP '2024-01-05 16:45:00', 'ATM Withdrawal', NULL), " +
                        "(2, 'DEPOSIT', 1500.00, TIMESTAMP '2024-01-03 12:00:00', 'Bonus Deposit', NULL), " +
                        "(1, 'WITHDRAWAL', 89.99, TIMESTAMP '2024-01-01 13:30:00', 'Grocery Store Purchase', NULL)");
            
            System.out.println("Database initialized with sample data");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

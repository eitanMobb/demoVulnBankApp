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
                        "description VARCHAR(255) NOT NULL, " +
                        "transaction_date TIMESTAMP NOT NULL, " +
                        "reference_number VARCHAR(50), " +
                        "to_account_id BIGINT, " +
                        "status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED')");
            
            // Insert sample transactions
            stmt.execute("INSERT INTO transactions (account_id, transaction_type, amount, description, " +
                        "transaction_date, reference_number, to_account_id, status) VALUES " +
                        // Alice's transactions
                        "(1, 'DEPOSIT', 3200.00, 'Salary Deposit', '2024-01-14 09:00:00', 'DEP-20240114-001', NULL, 'COMPLETED'), " +
                        "(1, 'WITHDRAWAL', 45.67, 'Online Purchase', '2024-01-15 14:30:00', 'WTH-20240115-001', NULL, 'COMPLETED'), " +
                        "(1, 'TRANSFER', 500.00, 'Transfer to Savings', '2024-01-10 10:15:00', 'TRF-20240110-001', 2, 'COMPLETED'), " +
                        "(2, 'TRANSFER', 500.00, 'Transfer from Checking', '2024-01-10 10:15:00', 'TRF-20240110-001', 1, 'COMPLETED'), " +
                        "(2, 'DEPOSIT', 12.34, 'Interest Payment', '2024-01-12 00:01:00', 'INT-20240112-001', NULL, 'COMPLETED'), " +
                        "(1, 'WITHDRAWAL', 200.00, 'ATM Withdrawal', '2024-01-08 18:45:00', 'ATM-20240108-001', NULL, 'COMPLETED'), " +
                        "(3, 'DEPOSIT', 1000.00, 'Investment Dividend', '2024-01-05 12:00:00', 'DIV-20240105-001', NULL, 'COMPLETED'), " +
                        // Bob's transactions
                        "(4, 'DEPOSIT', 2800.00, 'Salary Deposit', '2024-01-14 09:00:00', 'DEP-20240114-002', NULL, 'COMPLETED'), " +
                        "(4, 'WITHDRAWAL', 150.00, 'Grocery Shopping', '2024-01-13 16:20:00', 'WTH-20240113-001', NULL, 'COMPLETED'), " +
                        "(4, 'TRANSFER', 300.00, 'Transfer to Savings', '2024-01-11 14:30:00', 'TRF-20240111-001', 5, 'COMPLETED'), " +
                        "(5, 'TRANSFER', 300.00, 'Transfer from Checking', '2024-01-11 14:30:00', 'TRF-20240111-001', 4, 'COMPLETED'), " +
                        "(5, 'DEPOSIT', 8.15, 'Interest Payment', '2024-01-12 00:01:00', 'INT-20240112-002', NULL, 'COMPLETED'), " +
                        "(4, 'WITHDRAWAL', 75.50, 'Gas Station', '2024-01-09 07:30:00', 'WTH-20240109-001', NULL, 'COMPLETED'), " +
                        "(6, 'DEPOSIT', 2500.00, 'Investment Contribution', '2024-01-07 15:00:00', 'INV-20240107-001', NULL, 'COMPLETED'), " +
                        // Cross-user transfer
                        "(1, 'TRANSFER', 100.00, 'Transfer to Bob', '2024-01-16 11:00:00', 'TRF-20240116-001', 4, 'COMPLETED'), " +
                        "(4, 'TRANSFER', 100.00, 'Transfer from Alice', '2024-01-16 11:00:00', 'TRF-20240116-001', 1, 'COMPLETED')");
            
            System.out.println("Database initialized with sample data");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

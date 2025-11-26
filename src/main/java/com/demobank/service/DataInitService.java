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
                        "reference_account_number VARCHAR(20), " +
                        "balance_after DECIMAL(15,2) NOT NULL)");
            
            // Insert sample transactions
            stmt.execute("INSERT INTO transactions (account_id, transaction_type, amount, transaction_date, " +
                        "description, reference_account_number, balance_after) VALUES " +
                        // Alice's transactions
                        "(1, 'DEPOSIT', 1000.00, '2024-11-01 09:00:00', 'Initial deposit', NULL, 2500.00), " +
                        "(1, 'TRANSFER_OUT', 200.00, '2024-11-05 14:30:00', 'Transfer to Bob', 'CHK-002', 2300.00), " +
                        "(1, 'DEPOSIT', 500.00, '2024-11-10 11:15:00', 'Salary deposit', NULL, 2800.00), " +
                        "(1, 'WITHDRAWAL', 150.00, '2024-11-15 16:45:00', 'ATM withdrawal', NULL, 2650.00), " +
                        "(1, 'TRANSFER_OUT', 150.00, '2024-11-20 10:30:00', 'Utility payment', 'CHK-002', 2500.00), " +
                        "(2, 'DEPOSIT', 10000.00, '2024-11-01 09:00:00', 'Initial savings deposit', NULL, 15000.00), " +
                        "(2, 'WITHDRAWAL', 500.00, '2024-11-12 13:20:00', 'Emergency fund withdrawal', NULL, 14500.00), " +
                        "(2, 'DEPOSIT', 500.00, '2024-11-25 09:00:00', 'Interest payment', NULL, 15000.00), " +
                        // Bob's transactions
                        "(4, 'DEPOSIT', 800.00, '2024-11-01 09:00:00', 'Initial deposit', NULL, 1200.75), " +
                        "(4, 'TRANSFER_IN', 200.00, '2024-11-05 14:30:00', 'Transfer from Alice', 'CHK-001', 1400.75), " +
                        "(4, 'WITHDRAWAL', 100.00, '2024-11-08 12:00:00', 'ATM withdrawal', NULL, 1300.75), " +
                        "(4, 'TRANSFER_IN', 150.00, '2024-11-20 10:30:00', 'Payment from Alice', 'CHK-001', 1450.75), " +
                        "(4, 'WITHDRAWAL', 250.00, '2024-11-22 15:30:00', 'Grocery shopping', NULL, 1200.75), " +
                        "(5, 'DEPOSIT', 5000.00, '2024-11-01 09:00:00', 'Initial savings deposit', NULL, 5500.25), " +
                        "(5, 'DEPOSIT', 500.25, '2024-11-15 10:00:00', 'Interest payment', NULL, 6000.50), " +
                        "(5, 'WITHDRAWAL', 500.25, '2024-11-18 14:45:00', 'Investment transfer', NULL, 5500.25)");
            
            System.out.println("Database initialized with sample data");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

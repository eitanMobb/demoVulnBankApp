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
                        "from_account_id BIGINT NOT NULL, " +
                        "to_account_id BIGINT NOT NULL, " +
                        "from_account_number VARCHAR(20) NOT NULL, " +
                        "to_account_number VARCHAR(20) NOT NULL, " +
                        "amount DECIMAL(15,2) NOT NULL, " +
                        "transaction_type VARCHAR(20) NOT NULL, " +
                        "description VARCHAR(500), " +
                        "transaction_date TIMESTAMP NOT NULL, " +
                        "status VARCHAR(20) NOT NULL)");
            
            // Insert sample transactions
            stmt.execute("INSERT INTO transactions (from_account_id, to_account_id, from_account_number, " +
                        "to_account_number, amount, transaction_type, description, transaction_date, status) VALUES " +
                        "(1, 2, 'CHK-001', 'SAV-001', 500.00, 'TRANSFER', 'Monthly savings transfer', " +
                        "DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'COMPLETED'), " +
                        "(0, 1, 'BANK', 'CHK-001', 1000.00, 'DEPOSIT', 'Salary deposit', " +
                        "DATEADD('DAY', -3, CURRENT_TIMESTAMP), 'COMPLETED'), " +
                        "(4, 0, 'CHK-002', 'BANK', 200.00, 'WITHDRAWAL', 'ATM withdrawal', " +
                        "DATEADD('DAY', -2, CURRENT_TIMESTAMP), 'COMPLETED'), " +
                        "(4, 1, 'CHK-002', 'CHK-001', 150.00, 'TRANSFER', 'Payment to Alice', " +
                        "DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'COMPLETED'), " +
                        "(0, 2, 'BANK', 'SAV-001', 2000.00, 'DEPOSIT', 'Tax refund', " +
                        "CURRENT_TIMESTAMP, 'COMPLETED')");
            
            System.out.println("Database initialized with sample data");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

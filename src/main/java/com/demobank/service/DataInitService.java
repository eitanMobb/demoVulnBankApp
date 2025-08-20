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
            
            System.out.println("Database initialized with sample data");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

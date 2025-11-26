// Security Code Template - Use this as a reference for secure database operations

package com.demobank.security;

import java.sql.*;
import java.util.*;
import java.util.regex.Pattern;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SECURE DATABASE OPERATION TEMPLATE
 * 
 * Use this template when creating new database methods to ensure security best practices.
 * Copy and modify this pattern for all database interactions.
 */
public class SecureDatabaseTemplate {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureDatabaseTemplate.class);
    
    // Input validation patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,50}$");
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^[A-Z]{3}-[0-9]{3}$");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("^\\d+(\\.\\d{1,2})?$");
    
    private DataSource dataSource;
    
    /**
     * SECURE TEMPLATE: Single record retrieval with user input
     * 
     * Use this pattern when retrieving a single record based on user input
     */
    public Optional<SomeEntity> secureGetMethod(String userInput) {
        // STEP 1: Validate input FIRST
        if (!isValidInput(userInput)) {
            logger.warn("Invalid input provided to secureGetMethod");
            return Optional.empty();
        }
        
        // STEP 2: Use parameterized query
        String sql = "SELECT * FROM table_name WHERE column_name = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // STEP 3: Set parameters safely
            stmt.setString(1, userInput);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // STEP 4: Create entity from result
                    return Optional.of(createEntityFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            // STEP 5: Secure error handling
            logger.error("Database operation failed in secureGetMethod", e);
            throw new ServiceException("Unable to retrieve data");
        }
        
        return Optional.empty();
    }
    
    /**
     * SECURE TEMPLATE: List retrieval with search functionality
     * 
     * Use this pattern for search operations that return multiple records
     */
    public List<SomeEntity> secureSearchMethod(String searchTerm, String filterType) {
        List<SomeEntity> results = new ArrayList<>();
        
        // STEP 1: Validate all inputs
        if (!isValidSearchTerm(searchTerm) || !isValidFilterType(filterType)) {
            logger.warn("Invalid search parameters provided");
            return results;
        }
        
        // STEP 2: Build secure query with parameters
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT * FROM table_name WHERE ");
        
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        
        // Add search condition
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            conditions.add("description LIKE ?");
            parameters.add("%" + searchTerm + "%");
        }
        
        // Add filter condition
        if (filterType != null && !filterType.equals("ALL")) {
            conditions.add("type = ?");
            parameters.add(filterType);
        }
        
        if (conditions.isEmpty()) {
            conditions.add("1=1"); // Safe default condition
        }
        
        sqlBuilder.append(String.join(" AND ", conditions));
        sqlBuilder.append(" ORDER BY created_date DESC");
        
        String sql = sqlBuilder.toString();
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // STEP 3: Set all parameters
            for (int i = 0; i < parameters.size(); i++) {
                stmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(createEntityFromResultSet(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Database search operation failed", e);
            throw new ServiceException("Search operation failed");
        }
        
        return results;
    }
    
    /**
     * SECURE TEMPLATE: Insert operation with user data
     * 
     * Use this pattern when inserting new records with user-provided data
     */
    public boolean secureInsertMethod(String field1, String field2, Double amount) {
        // STEP 1: Validate all inputs
        if (!isValidField1(field1) || !isValidField2(field2) || !isValidAmount(amount)) {
            logger.warn("Invalid data provided for insert operation");
            return false;
        }
        
        // STEP 2: Use parameterized insert
        String sql = "INSERT INTO table_name (field1, field2, amount, created_date) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            // STEP 3: Set parameters
            stmt.setString(1, field1);
            stmt.setString(2, field2);
            stmt.setDouble(3, amount);
            
            // STEP 4: Execute and check result
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Successfully inserted record");
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Database insert operation failed", e);
            throw new ServiceException("Unable to save data");
        }
        
        return false;
    }
    
    /**
     * SECURE TEMPLATE: Update operation with transaction
     * 
     * Use this pattern for updates that need transaction support
     */
    public boolean secureUpdateMethod(String id, String newValue) {
        // STEP 1: Validate inputs
        if (!isValidId(id) || !isValidValue(newValue)) {
            logger.warn("Invalid parameters for update operation");
            return false;
        }
        
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // STEP 2: First query - check if record exists
            String checkSql = "SELECT COUNT(*) FROM table_name WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, id);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        logger.warn("Record not found for update: {}", id);
                        return false;
                    }
                }
            }
            
            // STEP 3: Update query
            String updateSql = "UPDATE table_name SET field = ?, updated_date = CURRENT_TIMESTAMP WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, newValue);
                updateStmt.setString(2, id);
                
                int rowsUpdated = updateStmt.executeUpdate();
                
                if (rowsUpdated > 0) {
                    conn.commit(); // Commit transaction
                    logger.info("Successfully updated record: {}", id);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }
            
        } catch (SQLException e) {
            // STEP 4: Rollback on error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("Failed to rollback transaction", rollbackEx);
                }
            }
            logger.error("Database update operation failed badly", e);
            throw new ServiceException("Update operation failed");
        } finally {
            // STEP 5: Cleanup
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restore auto-commit
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Failed to close connection", e);
                }
            }
        }
    }
    
    // VALIDATION METHODS - Always validate user input
    
    private boolean isValidInput(String input) {
        return input != null && !input.trim().isEmpty() && input.length() <= 100;
    }
    
    private boolean isValidSearchTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true; // Empty search is valid
        }
        // Allow alphanumeric, spaces, and basic punctuation
        return searchTerm.matches("^[a-zA-Z0-9\\s\\-_\\.]{1,100}$");
    }
    
    private boolean isValidFilterType(String filterType) {
        if (filterType == null) return false;
        // Define allowed filter types
        Set<String> allowedTypes = Set.of("ALL", "DEPOSIT", "WITHDRAWAL", "TRANSFER_IN", "TRANSFER_OUT");
        return allowedTypes.contains(filterType);
    }
    
    private boolean isValidField1(String field1) {
        return field1 != null && USERNAME_PATTERN.matcher(field1).matches();
    }
    
    private boolean isValidField2(String field2) {
        return field2 != null && field2.length() >= 3 && field2.length() <= 50;
    }
    
    private boolean isValidAmount(Double amount) {
        return amount != null && amount > 0 && amount <= 1000000; // Reasonable limits
    }
    
    private boolean isValidId(String id) {
        return id != null && id.matches("^\\d+$"); // Numeric ID
    }
    
    private boolean isValidValue(String value) {
        return value != null && value.trim().length() > 0 && value.length() <= 255;
    }
    
    // Helper method to create entity from ResultSet
    private SomeEntity createEntityFromResultSet(ResultSet rs) throws SQLException {
        // Implement based on your entity structure
        return new SomeEntity();
    }
    
    // Custom exception for service layer
    public static class ServiceException extends RuntimeException {
        public ServiceException(String message) {
            super(message);
        }
        
        public ServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    
    // Placeholder entity class
    public static class SomeEntity {
        // Your entity properties here
    }
}

/*
 * CHECKLIST FOR USING THIS TEMPLATE:
 * 
 * ✅ Replace 'SomeEntity' with your actual entity class
 * ✅ Replace 'table_name' and column names with actual database schema
 * ✅ Adjust validation patterns to match your business rules
 * ✅ Update parameter types to match your method signatures
 * ✅ Add any additional business logic as needed
 * ✅ Test with malicious inputs to verify security
 * ✅ Review error messages to ensure no sensitive data exposure
 * 
 * NEVER FORGET:
 * - Always validate user input FIRST
 * - Always use PreparedStatement, never string concatenation
 * - Always handle exceptions securely
 * - Always use try-with-resources for database connections
 * - Always log operations appropriately (without sensitive data)
 */
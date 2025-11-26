# Security Development Guidelines for DemoBank Application

## 🔒 Critical Security Rules - MUST FOLLOW

### 1. SQL Injection Prevention - MANDATORY

**❌ NEVER DO THIS:**
```java
// VULNERABLE - String concatenation
String sql = "SELECT * FROM users WHERE username = '" + username + "'";
Statement stmt = conn.createStatement();
ResultSet rs = stmt.executeQuery(sql);
```

**✅ ALWAYS DO THIS:**
```java
// SECURE - Parameterized queries
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);
ResultSet rs = stmt.executeQuery();
```

**🎯 Key Rules:**
- **NEVER** concatenate user input directly into SQL strings
- **ALWAYS** use `PreparedStatement` instead of `Statement`
- **ALWAYS** use parameter placeholders (`?`) for dynamic values
- **VALIDATE** all user inputs before database operations

### 2. Input Validation - REQUIRED FOR ALL USER INPUTS

```java
// Example: Validate before using
public boolean validateInput(String input) {
    if (input == null || input.trim().isEmpty()) {
        return false;
    }
    // Add specific validation rules
    return input.matches("[a-zA-Z0-9\\s]+"); // Example pattern
}

// Use in methods
public User authenticateUser(String username, String password) {
    if (!validateInput(username) || !validateInput(password)) {
        throw new IllegalArgumentException("Invalid input parameters");
    }
    // ... secure database operations
}
```

### 3. Database Connection Best Practices

**✅ Secure Pattern:**
```java
public List<SomeEntity> secureMethod(String userInput) {
    List<SomeEntity> results = new ArrayList<>();
    
    // 1. Validate input first
    if (!isValidInput(userInput)) {
        return results; // or throw exception
    }
    
    try (Connection conn = dataSource.getConnection()) {
        // 2. Use parameterized queries
        String sql = "SELECT * FROM table WHERE column = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            // 3. Set parameters safely
            stmt.setString(1, userInput);
            
            try (ResultSet rs = stmt.executeQuery()) {
                // 4. Process results
                while (rs.next()) {
                    // ... create entities
                }
            }
        }
    } catch (SQLException e) {
        // 5. Log errors securely (don't expose sensitive data)
        logger.error("Database operation failed", e);
        throw new ServiceException("Operation failed");
    }
    
    return results;
}
```

### 4. Error Handling - Security Focused

**❌ NEVER expose sensitive information:**
```java
// BAD - Exposes database structure
catch (SQLException e) {
    return "Error: " + e.getMessage(); // Reveals SQL details
}
```

**✅ Secure error handling:**
```java
// GOOD - Generic error messages
catch (SQLException e) {
    logger.error("Database operation failed for user: {}", userId, e);
    throw new ServiceException("Operation failed. Please try again.");
}
```

### 5. Authentication Security

**Required Security Measures:**
```java
public User authenticateUser(String username, String password) {
    // 1. Input validation
    if (!isValidCredentials(username, password)) {
        return null;
    }
    
    // 2. Use prepared statements
    String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
    
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        
        stmt.setString(1, username);
        stmt.setString(2, password); // In production: use hashed passwords
        
        try (ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                // 3. Don't log sensitive data
                logger.info("Successful login for user: {}", username);
                return createUserFromResultSet(rs);
            }
        }
    } catch (SQLException e) {
        // 4. Secure error logging
        logger.error("Authentication failed", e);
    }
    
    return null;
}
```

## 🛡️ Code Review Checklist

Before committing any database-related code, ensure:

- [ ] **No string concatenation** in SQL queries
- [ ] **PreparedStatement** used instead of Statement
- [ ] **All user inputs validated** before database operations
- [ ] **Error messages don't expose** sensitive information
- [ ] **Logging is secure** (no passwords/sensitive data in logs)
- [ ] **Try-with-resources** used for proper resource management
- [ ] **Input sanitization** implemented where needed

## 🚨 High-Risk Areas - Extra Attention Required

### 1. User Input Points
- Login forms (`username`, `password`)
- Search functionality (`searchTerm`)
- Transfer operations (`fromAccount`, `toAccount`, `amount`)
- Any form fields or URL parameters

### 2. Database Operations
- Authentication queries
- Search queries with LIKE operations
- Dynamic WHERE clauses
- INSERT/UPDATE operations with user data

### 3. Error Messages
- Database connection errors
- SQL execution failures
- Validation failures
- Authentication failures

## 🔧 Secure Development Workflow

### Before Writing Code:
1. **Identify all user inputs** in your method
2. **Plan validation strategy** for each input
3. **Design SQL queries** with parameters in mind

### While Writing Code:
1. **Start with PreparedStatement** for any SQL
2. **Add input validation** before database calls
3. **Use try-with-resources** for all database connections
4. **Plan error handling** without information disclosure

### After Writing Code:
1. **Review all SQL queries** for injection vulnerabilities
2. **Test with malicious inputs** (e.g., `'; DROP TABLE users; --`)
3. **Verify error messages** don't reveal system details
4. **Check logging** doesn't expose sensitive data

## 📚 Additional Security Resources

### Common SQL Injection Payloads to Test Against:
```
' OR '1'='1
'; DROP TABLE users; --
' UNION SELECT * FROM users --
admin'--
' OR 1=1 #
```

### Validation Patterns:
```java
// Username: alphanumeric + underscore, 3-50 chars
private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]{3,50}$";

// Account number: letters + numbers + hyphens
private static final String ACCOUNT_PATTERN = "^[A-Z]{3}-[0-9]{3}$";

// Amount: positive decimal with up to 2 decimal places
private static final String AMOUNT_PATTERN = "^\\d+(\\.\\d{1,2})?$";
```

## ⚠️ REMEMBER: Security is NOT Optional

- Every database query is a potential attack vector
- User input should NEVER be trusted
- Security vulnerabilities can lead to data breaches, financial loss, and legal consequences
- When in doubt, use parameterized queries and validate inputs

**If you're unsure about security implications, ask for a code review before committing!**
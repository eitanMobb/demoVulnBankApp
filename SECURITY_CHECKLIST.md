# Pre-Commit Security Checklist

**🔒 MANDATORY SECURITY REVIEW - Complete before every commit**

Copy this checklist and paste it into your commit message or PR description:

---

## SQL Security Checklist ✅

### Database Queries
- [ ] **No string concatenation** in any SQL statements
- [ ] **PreparedStatement used** instead of Statement for all dynamic queries
- [ ] **All user inputs parameterized** using `stmt.setString()`, `stmt.setInt()`, etc.
- [ ] **No direct user input** in SQL WHERE clauses, ORDER BY, or any other SQL fragments

### Input Validation
- [ ] **All user inputs validated** before database operations
- [ ] **Input length limits** enforced (username, passwords, search terms, etc.)
- [ ] **Special characters handled** appropriately (regex patterns used where needed)
- [ ] **Null checks performed** before processing user input

### Error Handling
- [ ] **SQLException caught** and handled securely
- [ ] **No sensitive information** exposed in error messages to users
- [ ] **Errors logged appropriately** without revealing system details
- [ ] **Generic error messages** returned to end users

### Authentication & Authorization
- [ ] **User authentication** uses prepared statements
- [ ] **No passwords logged** in plain text
- [ ] **Session management** implemented securely
- [ ] **User can only access their own data** (proper authorization checks)

---

## Code Quality Checklist ✅

### Resource Management
- [ ] **Try-with-resources** used for all database connections
- [ ] **Connections properly closed** in finally blocks or try-with-resources
- [ ] **ResultSets closed** appropriately
- [ ] **No resource leaks** in exception scenarios

### Security Patterns
- [ ] **Input validation methods** used consistently
- [ ] **Secure coding template** followed for new database methods
- [ ] **No hardcoded credentials** in source code
- [ ] **Logging is secure** (no sensitive data in logs)

---

## Testing Checklist ✅

### Manual Security Testing
- [ ] **Tested with SQL injection payloads**: `' OR '1'='1`, `'; DROP TABLE users; --`
- [ ] **Tested with empty/null inputs**
- [ ] **Tested with oversized inputs** (very long strings)
- [ ] **Tested error scenarios** to verify no information disclosure

### Functional Testing
- [ ] **All new features work** as expected
- [ ] **Existing functionality unaffected** by changes
- [ ] **Edge cases handled** appropriately
- [ ] **User experience remains smooth** after security fixes

---

## Documentation & Communication ✅

- [ ] **Security changes documented** if adding new security measures
- [ ] **Breaking changes communicated** to team if any
- [ ] **Code comments added** for complex security logic
- [ ] **README updated** if new security requirements added

---

## Final Verification ✅

Before clicking "Commit":

1. **Run a final scan**: Use Mobb or other security tools to verify no new vulnerabilities
2. **Review the diff**: Double-check that only intended changes are included
3. **Test authentication**: Ensure login still works after security changes
4. **Test core functionality**: Verify transfers, searches, and other key features work

---

**🚨 STOP - If any checkbox is unchecked, DO NOT COMMIT**

**💡 Remember**: It's better to take extra time now than to fix a security breach later.

**❓ Unsure about something?** Ask for a code review before committing.

---

### Emergency Rollback Plan

If security issues are discovered after commit:

1. **Immediately revert** the problematic commit
2. **Notify the team** about the security issue  
3. **Apply fixes** using the secure templates
4. **Re-test thoroughly** before redeployment
5. **Document the incident** for future reference

---

**Commit approved by**: [Your name]  
**Security review date**: [Current date]  
**All security checks passed**: ✅
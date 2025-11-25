package com.demobank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long fromAccountId;
    
    @Column(nullable = false)
    private Long toAccountId;
    
    @Column(nullable = false)
    private String fromAccountNumber;
    
    @Column(nullable = false)
    private String toAccountNumber;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String transactionType; // "TRANSFER", "DEPOSIT", "WITHDRAWAL"
    
    @Column(length = 500)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(nullable = false)
    private String status; // "COMPLETED", "PENDING", "FAILED"
    
    // Default constructor
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    // Constructor
    public Transaction(Long fromAccountId, Long toAccountId, String fromAccountNumber, 
                      String toAccountNumber, BigDecimal amount, String transactionType, String description) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.transactionType = transactionType;
        this.description = description;
        this.transactionDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getFromAccountId() {
        return fromAccountId;
    }
    
    public void setFromAccountId(Long fromAccountId) {
        this.fromAccountId = fromAccountId;
    }
    
    public Long getToAccountId() {
        return toAccountId;
    }
    
    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }
    
    public String getFromAccountNumber() {
        return fromAccountNumber;
    }
    
    public void setFromAccountNumber(String fromAccountNumber) {
        this.fromAccountNumber = fromAccountNumber;
    }
    
    public String getToAccountNumber() {
        return toAccountNumber;
    }
    
    public void setToAccountNumber(String toAccountNumber) {
        this.toAccountNumber = toAccountNumber;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
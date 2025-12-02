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
    private Long accountId;
    
    @Column(nullable = false)
    private String transactionType; // "TRANSFER", "DEPOSIT", "WITHDRAWAL", "INTEREST", "FEE"
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column
    private String referenceNumber;
    
    @Column
    private Long toAccountId; // For transfers
    
    @Column
    private String status; // "COMPLETED", "PENDING", "FAILED"
    
    // Default constructor
    public Transaction() {}
    
    // Constructor
    public Transaction(Long accountId, String transactionType, BigDecimal amount, 
                      String description, LocalDateTime transactionDate, 
                      String referenceNumber, Long toAccountId, String status) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.transactionDate = transactionDate;
        this.referenceNumber = referenceNumber;
        this.toAccountId = toAccountId;
        this.status = status;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getAccountId() {
        return accountId;
    }
    
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    public String getTransactionType() {
        return transactionType;
    }
    
    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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
    
    public String getReferenceNumber() {
        return referenceNumber;
    }
    
    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
    
    public Long getToAccountId() {
        return toAccountId;
    }
    
    public void setToAccountId(Long toAccountId) {
        this.toAccountId = toAccountId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
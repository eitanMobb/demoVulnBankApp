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
    private String transactionType; // "TRANSFER", "DEPOSIT", "WITHDRAWAL"
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(length = 500)
    private String description;
    
    // For transfers, store the other account involved
    @Column
    private Long relatedAccountId;
    
    @Column
    private String relatedAccountNumber;
    
    @Column(nullable = false)
    private String status; // "COMPLETED", "PENDING", "FAILED"
    
    // Default constructor
    public Transaction() {
        this.transactionDate = LocalDateTime.now();
        this.status = "COMPLETED";
    }
    
    // Constructor
    public Transaction(Long accountId, String transactionType, BigDecimal amount, 
                      String description, Long relatedAccountId, String relatedAccountNumber) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.description = description;
        this.relatedAccountId = relatedAccountId;
        this.relatedAccountNumber = relatedAccountNumber;
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
    
    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
    
    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Long getRelatedAccountId() {
        return relatedAccountId;
    }
    
    public void setRelatedAccountId(Long relatedAccountId) {
        this.relatedAccountId = relatedAccountId;
    }
    
    public String getRelatedAccountNumber() {
        return relatedAccountNumber;
    }
    
    public void setRelatedAccountNumber(String relatedAccountNumber) {
        this.relatedAccountNumber = relatedAccountNumber;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}


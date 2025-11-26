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
    private String transactionType; // "DEPOSIT", "WITHDRAWAL", "TRANSFER_OUT", "TRANSFER_IN", "PAYMENT_ONCE"
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime transactionDate;
    
    @Column(length = 500)
    private String description;
    
    @Column
    private String referenceAccountNumber; // For transfers, the other account involved
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;
    
    // Default constructor
    public Transaction() {}
    
    // Constructor
    public Transaction(Long accountId, String transactionType, BigDecimal amount, 
                      LocalDateTime transactionDate, String description, 
                      String referenceAccountNumber, BigDecimal balanceAfter) {
        this.accountId = accountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.description = description;
        this.referenceAccountNumber = referenceAccountNumber;
        this.balanceAfter = balanceAfter;
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
    
    public String getReferenceAccountNumber() {
        return referenceAccountNumber;
    }
    
    public void setReferenceAccountNumber(String referenceAccountNumber) {
        this.referenceAccountNumber = referenceAccountNumber;
    }
    
    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }
    
    public void setBalanceAfter(BigDecimal balanceAfter) {
        this.balanceAfter = balanceAfter;
    }
}
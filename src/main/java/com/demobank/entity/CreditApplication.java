package com.demobank.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_applications")
public class CreditApplication {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long userId;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal requestedLimit;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal annualIncome;
    
    @Column(nullable = false)
    private String employmentStatus;
    
    @Column(nullable = false)
    private String status; // "PENDING", "APPROVED", "DENIED"
    
    @Column(nullable = false)
    private LocalDateTime applicationDate;
    
    private String comments;
    
    // Default constructor
    public CreditApplication() {
        this.applicationDate = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    // Constructor
    public CreditApplication(Long userId, BigDecimal requestedLimit, BigDecimal annualIncome, 
                           String employmentStatus, String comments) {
        this.userId = userId;
        this.requestedLimit = requestedLimit;
        this.annualIncome = annualIncome;
        this.employmentStatus = employmentStatus;
        this.comments = comments;
        this.applicationDate = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public BigDecimal getRequestedLimit() {
        return requestedLimit;
    }
    
    public void setRequestedLimit(BigDecimal requestedLimit) {
        this.requestedLimit = requestedLimit;
    }
    
    public BigDecimal getAnnualIncome() {
        return annualIncome;
    }
    
    public void setAnnualIncome(BigDecimal annualIncome) {
        this.annualIncome = annualIncome;
    }
    
    public String getEmploymentStatus() {
        return employmentStatus;
    }
    
    public void setEmploymentStatus(String employmentStatus) {
        this.employmentStatus = employmentStatus;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getApplicationDate() {
        return applicationDate;
    }
    
    public void setApplicationDate(LocalDateTime applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
}

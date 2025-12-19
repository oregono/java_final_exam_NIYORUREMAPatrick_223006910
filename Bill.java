package com.dao;

import java.math.BigDecimal;
import java.sql.Date;

public class Bill {
    private int id;
    private String billId;
    private String subscriber;
    private String services;
    private BigDecimal amount;
    private Date issueDate;
    private Date dueDate;
    private String status;
    private String reference;
    
    // Default constructor
    public Bill() {}
    
    // Full constructor
    public Bill(int id, String billId, String subscriber, String services, BigDecimal amount,
                Date issueDate, Date dueDate, String status, String reference) {
        this.id = id;
        this.billId = billId;
        this.subscriber = subscriber;
        this.services = services;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = status;
        this.reference = reference;
    }
    
    // Constructor without ID (for new bills)
    public Bill(String billId, String subscriber, String services, BigDecimal amount,
                Date issueDate, Date dueDate, String status, String reference) {
        this(0, billId, subscriber, services, amount, issueDate, dueDate, status, reference);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }
    
    public String getSubscriber() { return subscriber; }
    public void setSubscriber(String subscriber) { this.subscriber = subscriber; }
    
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }
    
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    // Utility methods
    public boolean isPaid() {
        return "Paid".equalsIgnoreCase(status);
    }
    
    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }
    
    public boolean isOverdue() {
        return "Overdue".equalsIgnoreCase(status);
    }
    
    public boolean isDueSoon() {
        if (dueDate == null) return false;
        long currentTime = System.currentTimeMillis();
        long dueTime = dueDate.getTime();
        long sevenDays = 7 * 24 * 60 * 60 * 1000L; // 7 days in milliseconds
        return dueTime > currentTime && (dueTime - currentTime) <= sevenDays;
    }
    
    public boolean isPastDue() {
        if (dueDate == null) return false;
        return dueDate.getTime() < System.currentTimeMillis() && isPending();
    }
    
    public int getDaysUntilDue() {
        if (dueDate == null) return -1;
        long currentTime = System.currentTimeMillis();
        long dueTime = dueDate.getTime();
        long diff = dueTime - currentTime;
        return (int) (diff / (24 * 60 * 60 * 1000));
    }
    
    // Validation methods
    public boolean isValid() {
        return billId != null && !billId.trim().isEmpty() &&
               subscriber != null && !subscriber.trim().isEmpty() &&
               services != null && !services.trim().isEmpty() &&
               amount != null && amount.compareTo(BigDecimal.ZERO) >= 0 &&
               issueDate != null && dueDate != null &&
               status != null && !status.trim().isEmpty() &&
               reference != null && !reference.trim().isEmpty();
    }
    
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (billId == null || billId.trim().isEmpty()) {
            errors.append("Bill ID is required. ");
        }
        
        if (subscriber == null || subscriber.trim().isEmpty()) {
            errors.append("Subscriber is required. ");
        }
        
        if (services == null || services.trim().isEmpty()) {
            errors.append("Services is required. ");
        }
        
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            errors.append("Amount must be non-negative. ");
        }
        
        if (issueDate == null) {
            errors.append("Issue date is required. ");
        }
        
        if (dueDate == null) {
            errors.append("Due date is required. ");
        }
        
        if (status == null || status.trim().isEmpty()) {
            errors.append("Status is required. ");
        }
        
        if (reference == null || reference.trim().isEmpty()) {
            errors.append("Reference is required. ");
        }
        
        if (issueDate != null && dueDate != null && dueDate.before(issueDate)) {
            errors.append("Due date cannot be before issue date. ");
        }
        
        return errors.toString().trim();
    }
    
    @Override
    public String toString() {
        return String.format(
            "Bill[id=%d, billId='%s', subscriber='%s', services='%s', amount=%.2f, " +
            "issueDate=%s, dueDate=%s, status='%s', reference='%s']",
            id, billId, subscriber, services, amount != null ? amount.doubleValue() : 0.0,
            issueDate, dueDate, status, reference
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Bill bill = (Bill) o;
        
        if (id != bill.id) return false;
        if (!billId.equals(bill.billId)) return false;
        if (!subscriber.equals(bill.subscriber)) return false;
        if (!services.equals(bill.services)) return false;
        if (!amount.equals(bill.amount)) return false;
        if (!issueDate.equals(bill.issueDate)) return false;
        if (!dueDate.equals(bill.dueDate)) return false;
        if (!status.equals(bill.status)) return false;
        return reference.equals(bill.reference);
    }
    
    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + billId.hashCode();
        result = 31 * result + subscriber.hashCode();
        result = 31 * result + services.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + issueDate.hashCode();
        result = 31 * result + dueDate.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + reference.hashCode();
        return result;
    }
    
    // Builder pattern for fluent creation
    public static class Builder {
        private int id;
        private String billId;
        private String subscriber;
        private String services;
        private BigDecimal amount;
        private Date issueDate;
        private Date dueDate;
        private String status = "Pending";
        private String reference;
        
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        
        public Builder billId(String billId) {
            this.billId = billId;
            return this;
        }
        
        public Builder subscriber(String subscriber) {
            this.subscriber = subscriber;
            return this;
        }
        
        public Builder services(String services) {
            this.services = services;
            return this;
        }
        
        public Builder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder amount(double amount) {
            this.amount = BigDecimal.valueOf(amount);
            return this;
        }
        
        public Builder issueDate(Date issueDate) {
            this.issueDate = issueDate;
            return this;
        }
        
        public Builder dueDate(Date dueDate) {
            this.dueDate = dueDate;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }
        
        public Bill build() {
            return new Bill(id, billId, subscriber, services, amount, issueDate, dueDate, status, reference);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Static factory methods
    public static Bill createNewBill(String billId, String subscriber, String services, 
                                   BigDecimal amount, Date issueDate, Date dueDate, String reference) {
        return new Builder()
            .billId(billId)
            .subscriber(subscriber)
            .services(services)
            .amount(amount)
            .issueDate(issueDate)
            .dueDate(dueDate)
            .reference(reference)
            .status("Pending")
            .build();
    }
    
    public static Bill createPaidBill(String billId, String subscriber, String services, 
                                    BigDecimal amount, Date issueDate, Date dueDate, String reference) {
        return new Builder()
            .billId(billId)
            .subscriber(subscriber)
            .services(services)
            .amount(amount)
            .issueDate(issueDate)
            .dueDate(dueDate)
            .reference(reference)
            .status("Paid")
            .build();
    }
}
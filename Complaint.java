package com.dao;

import java.sql.Timestamp;

public class Complaint {
    private String complaintId;
    private String subscriber;
    private String title;
    private String category;
    private String status;
    private String priority;
    private Timestamp createdDate;
    private String assignedTo;
    
    // Default constructor
    public Complaint() {}
    
    // Full constructor
    public Complaint(String complaintId, String subscriber, String title, String category, 
                    String status, String priority, Timestamp createdDate, String assignedTo) {
        this.complaintId = complaintId;
        this.subscriber = subscriber;
        this.title = title;
        this.category = category;
        this.status = status;
        this.priority = priority;
        this.createdDate = createdDate;
        this.assignedTo = assignedTo;
    }
    
    // Getters and Setters
    public String getComplaintId() { return complaintId; }
    public void setComplaintId(String complaintId) { this.complaintId = complaintId; }
    
    public String getSubscriber() { return subscriber; }
    public void setSubscriber(String subscriber) { this.subscriber = subscriber; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    
    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }
    
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    
    // Utility methods
    public boolean isOpen() {
        return "Open".equalsIgnoreCase(status);
    }
    
    public boolean isInProgress() {
        return "In Progress".equalsIgnoreCase(status);
    }
    
    public boolean isResolved() {
        return "Resolved".equalsIgnoreCase(status);
    }
    
    public boolean isClosed() {
        return "Closed".equalsIgnoreCase(status);
    }
    
    public boolean isUrgent() {
        return "Urgent".equalsIgnoreCase(priority);
    }
    
    public boolean isHighPriority() {
        return "High".equalsIgnoreCase(priority) || "Urgent".equalsIgnoreCase(priority);
    }
    
    // Validation methods
    public boolean isValid() {
        return complaintId != null && !complaintId.trim().isEmpty() &&
               subscriber != null && !subscriber.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               category != null && !category.trim().isEmpty() &&
               status != null && !status.trim().isEmpty() &&
               priority != null && !priority.trim().isEmpty() &&
               createdDate != null;
    }
    
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (complaintId == null || complaintId.trim().isEmpty()) {
            errors.append("Complaint ID is required. ");
        }
        
        if (subscriber == null || subscriber.trim().isEmpty()) {
            errors.append("Subscriber is required. ");
        }
        
        if (title == null || title.trim().isEmpty()) {
            errors.append("Title is required. ");
        }
        
        if (category == null || category.trim().isEmpty()) {
            errors.append("Category is required. ");
        }
        
        if (status == null || status.trim().isEmpty()) {
            errors.append("Status is required. ");
        }
        
        if (priority == null || priority.trim().isEmpty()) {
            errors.append("Priority is required. ");
        }
        
        if (createdDate == null) {
            errors.append("Created date is required. ");
        }
        
        return errors.toString().trim();
    }
    
    @Override
    public String toString() {
        return String.format(
            "Complaint[ID=%s, Subscriber=%s, Title=%s, Category=%s, Status=%s, Priority=%s, Created=%s]",
            complaintId, subscriber, title, category, status, priority, createdDate
        );
    }
    
    // Builder pattern
    public static class Builder {
        private String complaintId;
        private String subscriber;
        private String title;
        private String category;
        private String status = "Open";
        private String priority = "Medium";
        private Timestamp createdDate;
        private String assignedTo;
        
        public Builder complaintId(String complaintId) {
            this.complaintId = complaintId;
            return this;
        }
        
        public Builder subscriber(String subscriber) {
            this.subscriber = subscriber;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder category(String category) {
            this.category = category;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder priority(String priority) {
            this.priority = priority;
            return this;
        }
        
        public Builder createdDate(Timestamp createdDate) {
            this.createdDate = createdDate;
            return this;
        }
        
        public Builder assignedTo(String assignedTo) {
            this.assignedTo = assignedTo;
            return this;
        }
        
        public Complaint build() {
            return new Complaint(complaintId, subscriber, title, category, status, priority, createdDate, assignedTo);
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Static factory methods
    public static Complaint createNewComplaint(String complaintId, String subscriber, String title, String category, String priority) {
        return new Builder()
            .complaintId(complaintId)
            .subscriber(subscriber)
            .title(title)
            .category(category)
            .priority(priority)
            .createdDate(new Timestamp(System.currentTimeMillis()))
            .status("Open")
            .build();
    }
}
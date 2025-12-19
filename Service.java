package com.models;

import java.sql.Timestamp;

public class Service {
    private int serviceId;
    private String name;
    private String description;
    private String category;
    private double price;
    private String status;
    private Timestamp createdAt;
    
    public Service() {}
    
    public Service(int serviceId, String name, String description, String category, 
                  double price, String status, Timestamp createdAt) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public int getServiceId() { return serviceId; }
    public void setServiceId(int serviceId) { this.serviceId = serviceId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    
    // Utility methods
    public boolean isActive() {
        return "Active".equalsIgnoreCase(status);
    }
    
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               category != null && !category.trim().isEmpty() &&
               price >= 0 &&
               status != null && !status.trim().isEmpty();
    }
    
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (name == null || name.trim().isEmpty()) {
            errors.append("Service name is required. ");
        }
        
        if (category == null || category.trim().isEmpty()) {
            errors.append("Category is required. ");
        }
        
        if (price < 0) {
            errors.append("Price must be non-negative. ");
        }
        
        if (status == null || status.trim().isEmpty()) {
            errors.append("Status is required. ");
        }
        
        return errors.toString().trim();
    }
    
    @Override
    public String toString() {
        return String.format("Service{id=%d, name='%s', category='%s', price=%.2f, status='%s'}", 
            serviceId, name, category, price, status);
    }
}
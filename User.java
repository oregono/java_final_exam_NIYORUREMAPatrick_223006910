package com.models;

import java.sql.Timestamp;

public class User {
    private int subscriberID;
    private String username;
    private String passwordHash;
    private String email;
    private String fullName;
    private String role;
    private Timestamp createdAt;
    private Timestamp lastLogin;
    
    public User() {}
    
    // Full constructor
    public User(int subscriberID, String username, String passwordHash, String email, 
                String fullName, String role, Timestamp createdAt, Timestamp lastLogin) {
        this.subscriberID = subscriberID;
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.createdAt = createdAt;
        this.lastLogin = lastLogin;
    }
    
    // Constructor without timestamps
    public User(int subscriberID, String username, String passwordHash, String email, String fullName, String role) {
        this(subscriberID, username, passwordHash, email, fullName, role, null, null);
    }
    
    // Getters
    public int getSubscriberID() { return subscriberID; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getRole() { return role; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getLastLogin() { return lastLogin; }
    
    // Setters
    public void setSubscriberID(int subscriberID) { this.subscriberID = subscriberID; }
    public void setUsername(String username) { this.username = username; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setRole(String role) { this.role = role; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    
    public boolean isAdmin() {
        return "Admin".equalsIgnoreCase(role);
    }
    
    @Override
    public String toString() {
        return String.format("User[ID=%d, Username=%s, Name=%s, Role=%s]", 
            subscriberID, username, fullName, role);
    }
}
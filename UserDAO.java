package com.dao;

import com.models.User;
import com.utils.DB;
import java.sql.*;

public class UserDAO {
    
    public User authenticate(String username, String password) {
        User user = null;
        String sql = "SELECT SubscriberID, Username, PasswordHash, Email, FullName, Role FROM subscriber WHERE Username = ? AND PasswordHash = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = new User(
                    rs.getInt("SubscriberID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("Email"),
                    rs.getString("FullName"),
                    rs.getString("Role")
                );
                
                // Update last login timestamp
                updateLastLogin(user.getSubscriberID());
                
                System.out.println("✅ User authenticated: " + user.getUsername() + " | Role: " + user.getRole());
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Authentication error: " + e.getMessage());
        }
        
        return user;
    }
    
    private void updateLastLogin(int subscriberID) {
        String sql = "UPDATE subscriber SET LastLogin = CURRENT_TIMESTAMP WHERE SubscriberID = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, subscriberID);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating last login: " + e.getMessage());
        }
    }
    
    // Method to get user by ID (for future use)
    public User getUserById(int subscriberID) {
        User user = null;
        String sql = "SELECT SubscriberID, Username, PasswordHash, Email, FullName, Role FROM subscriber WHERE SubscriberID = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, subscriberID);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                user = new User(
                    rs.getInt("SubscriberID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("Email"),
                    rs.getString("FullName"),
                    rs.getString("Role")
                );
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting user by ID: " + e.getMessage());
        }
        
        return user;
    }
}
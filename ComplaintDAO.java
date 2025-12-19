package com.dao;

import com.dao.Complaint;
import java.sql.*;
import java.util.List;

public class ComplaintDAO extends BaseDAO {
    
    public boolean createComplaint(Complaint complaint) {
        if (!complaint.isValid()) {
            throw new IllegalArgumentException("Invalid complaint: " + complaint.getValidationErrors());
        }
        
        String sql = "INSERT INTO complaint (ComplaintID, Subscriber, Title, Category, Status, Priority) " +
                    "VALUES (?, ?, ?, ?, 'Open', ?)";
        
        int rowsAffected = executeUpdate(sql,
            complaint.getComplaintId(),
            complaint.getSubscriber(),
            complaint.getTitle(),
            complaint.getCategory(),
            complaint.getPriority()
        );
        
        return rowsAffected > 0;
    }
    
    public List<Complaint> getComplaintsBySubscriber(String subscriber) {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE Subscriber = ? ORDER BY CreatedDate DESC";
        
        return executeQuery(sql, this::mapComplaint, subscriber);
    }
    
    public List<Complaint> getAllComplaints() {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint ORDER BY CreatedDate DESC";
        
        return executeQuery(sql, this::mapComplaint);
    }
    
    public Complaint getComplaintById(String complaintId) {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE ComplaintID = ?";
        
        return executeQuerySingle(sql, this::mapComplaint, complaintId);
    }
    
    public boolean updateComplaintStatus(String complaintId, String status) {
        String sql = "UPDATE complaint SET Status = ? WHERE ComplaintID = ?";
        int rowsAffected = executeUpdate(sql, status, complaintId);
        return rowsAffected > 0;
    }
    
    public boolean assignComplaint(String complaintId, String assignedTo) {
        String sql = "UPDATE complaint SET AssignedTo = ? WHERE ComplaintID = ?";
        int rowsAffected = executeUpdate(sql, assignedTo, complaintId);
        return rowsAffected > 0;
    }
    
    public boolean updateComplaintPriority(String complaintId, String priority) {
        String sql = "UPDATE complaint SET Priority = ? WHERE ComplaintID = ?";
        int rowsAffected = executeUpdate(sql, priority, complaintId);
        return rowsAffected > 0;
    }
    
    public List<Complaint> getComplaintsByStatus(String status) {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE Status = ? ORDER BY CreatedDate DESC";
        
        return executeQuery(sql, this::mapComplaint, status);
    }
    
    public List<Complaint> getComplaintsByPriority(String priority) {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE Priority = ? ORDER BY CreatedDate DESC";
        
        return executeQuery(sql, this::mapComplaint, priority);
    }
    
    public List<Complaint> getOpenComplaints() {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE Status IN ('Open', 'In Progress') ORDER BY CreatedDate DESC";
        
        return executeQuery(sql, this::mapComplaint);
    }
    
    public List<Complaint> getUrgentComplaints() {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE Priority = 'Urgent' AND Status IN ('Open', 'In Progress') " +
                    "ORDER BY CreatedDate DESC";
        
        return executeQuery(sql, this::mapComplaint);
    }
    
    public List<Complaint> searchComplaints(String searchTerm) {
        String sql = "SELECT ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate, AssignedTo " +
                    "FROM complaint WHERE ComplaintID LIKE ? OR Subscriber LIKE ? OR Title LIKE ? OR Category LIKE ? " +
                    "ORDER BY CreatedDate DESC";
        
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(sql, this::mapComplaint, searchPattern, searchPattern, searchPattern, searchPattern);
    }
    
    public int getComplaintCountBySubscriber(String subscriber) {
        String sql = "SELECT COUNT(*) as count FROM complaint WHERE Subscriber = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, subscriber);
        return count != null ? count : 0;
    }
    
    public int getComplaintCountByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM complaint WHERE Status = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, status);
        return count != null ? count : 0;
    }
    
    public List<String> getComplaintSubscribers() {
        String sql = "SELECT DISTINCT Subscriber FROM complaint ORDER BY Subscriber";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Subscriber");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    private Complaint mapComplaint(ResultSet rs) {
        try {
            return new Complaint(
                rs.getString("ComplaintID"),
                rs.getString("Subscriber"),
                rs.getString("Title"),
                rs.getString("Category"),
                rs.getString("Status"),
                rs.getString("Priority"),
                rs.getTimestamp("CreatedDate"),
                rs.getString("AssignedTo")
            );
        } catch (SQLException e) {
            System.err.println("❌ Error mapping complaint from ResultSet: " + e.getMessage());
            return null;
        }
    }
}
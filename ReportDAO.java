package com.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportDAO extends BaseDAO {
    
    public Map<String, Object> getFinancialReport(Date startDate, Date endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Total revenue
        String revenueSql = "SELECT COALESCE(SUM(Amount), 0) as total FROM payment " +
                          "WHERE Status = 'Completed' AND Date BETWEEN ? AND ?";
        Double totalRevenue = executeQuerySingle(revenueSql, rs -> {
            try {
                return rs.getDouble("total");
            } catch (SQLException e) {
                return 0.0;
            }
        }, startDate, endDate);
        report.put("totalRevenue", totalRevenue != null ? totalRevenue : 0.0);
        
        // Payment methods distribution
        String methodsSql = "SELECT Method, COUNT(*) as count, SUM(Amount) as amount " +
                           "FROM payment WHERE Status = 'Completed' AND Date BETWEEN ? AND ? " +
                           "GROUP BY Method";
        List<Map<String, Object>> paymentMethods = executeQuery(methodsSql, rs -> {
            try {
                Map<String, Object> method = new HashMap<>();
                method.put("method", rs.getString("Method"));
                method.put("count", rs.getInt("count"));
                method.put("amount", rs.getDouble("amount"));
                return method;
            } catch (SQLException e) {
                return null;
            }
        }, startDate, endDate);
        report.put("paymentMethods", paymentMethods != null ? paymentMethods : new ArrayList<>());
        
        // Revenue by service
        String serviceSql = "SELECT b.Services, COUNT(*) as bill_count, SUM(p.Amount) as revenue " +
                           "FROM payment p JOIN bill b ON p.BillID = b.BillID " +
                           "WHERE p.Status = 'Completed' AND p.Date BETWEEN ? AND ? " +
                           "GROUP BY b.Services";
        List<Map<String, Object>> serviceRevenue = executeQuery(serviceSql, rs -> {
            try {
                Map<String, Object> service = new HashMap<>();
                service.put("service", rs.getString("Services"));
                service.put("billCount", rs.getInt("bill_count"));
                service.put("revenue", rs.getDouble("revenue"));
                return service;
            } catch (SQLException e) {
                return null;
            }
        }, startDate, endDate);
        report.put("serviceRevenue", serviceRevenue != null ? serviceRevenue : new ArrayList<>());
        
        return report;
    }
    
    public List<Map<String, Object>> getUsageReport(String serviceType, Date startDate, Date endDate) {
        String sql = "SELECT m.Subscriber, AVG(m.Consumption) as avg_consumption, " +
                    "MAX(m.Reading) as max_reading, COUNT(*) as reading_count " +
                    "FROM meter m WHERE m.Service = ? AND m.Date BETWEEN ? AND ? " +
                    "GROUP BY m.Subscriber ORDER BY avg_consumption DESC";
        
        return executeQuery(sql, rs -> {
            try {
                Map<String, Object> usage = new HashMap<>();
                usage.put("subscriber", rs.getString("Subscriber"));
                usage.put("avgConsumption", rs.getDouble("avg_consumption"));
                usage.put("maxReading", rs.getDouble("max_reading"));
                usage.put("readingCount", rs.getInt("reading_count"));
                return usage;
            } catch (SQLException e) {
                return null;
            }
        }, serviceType, startDate, endDate);
    }
    
    public Map<String, Object> getComplaintReport(Date startDate, Date endDate) {
        Map<String, Object> report = new HashMap<>();
        
        // Complaints by category
        String categorySql = "SELECT Category, COUNT(*) as count, " +
                            "SUM(CASE WHEN Status = 'Resolved' THEN 1 ELSE 0 END) as resolved " +
                            "FROM complaint WHERE CreatedDate BETWEEN ? AND ? " +
                            "GROUP BY Category";
        List<Map<String, Object>> complaintsByCategory = executeQuery(categorySql, rs -> {
            try {
                Map<String, Object> category = new HashMap<>();
                category.put("category", rs.getString("Category"));
                category.put("total", rs.getInt("count"));
                category.put("resolved", rs.getInt("resolved"));
                int total = rs.getInt("count");
                int resolved = rs.getInt("resolved");
                double resolutionRate = total > 0 ? (double) resolved / total * 100 : 0.0;
                category.put("resolutionRate", resolutionRate);
                return category;
            } catch (SQLException e) {
                return null;
            }
        }, startDate, endDate);
        report.put("complaintsByCategory", complaintsByCategory != null ? complaintsByCategory : new ArrayList<>());
        
        // Complaints by status
        String statusSql = "SELECT Status, COUNT(*) as count FROM complaint " +
                          "WHERE CreatedDate BETWEEN ? AND ? GROUP BY Status";
        List<Map<String, Object>> complaintsByStatus = executeQuery(statusSql, rs -> {
            try {
                Map<String, Object> status = new HashMap<>();
                status.put("status", rs.getString("Status"));
                status.put("count", rs.getInt("count"));
                return status;
            } catch (SQLException e) {
                return null;
            }
        }, startDate, endDate);
        report.put("complaintsByStatus", complaintsByStatus != null ? complaintsByStatus : new ArrayList<>());
        
        // Average resolution time
        String resolutionSql = "SELECT AVG(TIMESTAMPDIFF(HOUR, CreatedDate, AssignedTo)) as avg_hours " +
                              "FROM complaint WHERE Status = 'Resolved' AND AssignedTo IS NOT NULL " +
                              "AND CreatedDate BETWEEN ? AND ?";
        Double avgResolutionHours = executeQuerySingle(resolutionSql, rs -> {
            try {
                return rs.getDouble("avg_hours");
            } catch (SQLException e) {
                return 0.0;
            }
        }, startDate, endDate);
        report.put("avgResolutionHours", avgResolutionHours != null ? avgResolutionHours : 0.0);
        
        return report;
    }

    // Additional report methods
    
    public Map<String, Object> getSubscriberReport() {
        Map<String, Object> report = new HashMap<>();
        
        // Total subscribers
        String totalSql = "SELECT COUNT(*) as total FROM subscriber WHERE Role = 'Subscriber'";
        Integer totalSubscribers = executeQuerySingle(totalSql, rs -> {
            try {
                return rs.getInt("total");
            } catch (SQLException e) {
                return 0;
            }
        });
        report.put("totalSubscribers", totalSubscribers != null ? totalSubscribers : 0);
        
        // New subscribers this month
        String newSql = "SELECT COUNT(*) as new_count FROM subscriber " +
                       "WHERE Role = 'Subscriber' AND MONTH(CreatedAt) = MONTH(CURRENT_DATE()) " +
                       "AND YEAR(CreatedAt) = YEAR(CURRENT_DATE())";
        Integer newSubscribers = executeQuerySingle(newSql, rs -> {
            try {
                return rs.getInt("new_count");
            } catch (SQLException e) {
                return 0;
            }
        });
        report.put("newSubscribersThisMonth", newSubscribers != null ? newSubscribers : 0);
        
        // Subscribers by service usage
        String serviceSql = "SELECT m.Service, COUNT(DISTINCT m.Subscriber) as subscriber_count " +
                           "FROM meter m GROUP BY m.Service";
        List<Map<String, Object>> serviceSubscribers = executeQuery(serviceSql, rs -> {
            try {
                Map<String, Object> service = new HashMap<>();
                service.put("service", rs.getString("Service"));
                service.put("subscriberCount", rs.getInt("subscriber_count"));
                return service;
            } catch (SQLException e) {
                return null;
            }
        });
        report.put("serviceSubscribers", serviceSubscribers != null ? serviceSubscribers : new ArrayList<>());
        
        return report;
    }
    
    public List<Map<String, Object>> getBillingReport(Date startDate, Date endDate) {
        String sql = "SELECT b.BillID, b.Subscriber, b.Services, b.Amount, b.IssueDate, " +
                    "b.DueDate, b.Status, b.Reference, " +
                    "CASE WHEN p.PaymentID IS NOT NULL THEN 'Paid' ELSE 'Unpaid' END as payment_status " +
                    "FROM bill b LEFT JOIN payment p ON b.BillID = p.BillID AND p.Status = 'Completed' " +
                    "WHERE b.IssueDate BETWEEN ? AND ? ORDER BY b.IssueDate DESC";
        
        return executeQuery(sql, rs -> {
            try {
                Map<String, Object> bill = new HashMap<>();
                bill.put("billId", rs.getString("BillID"));
                bill.put("subscriber", rs.getString("Subscriber"));
                bill.put("services", rs.getString("Services"));
                bill.put("amount", rs.getDouble("Amount"));
                bill.put("issueDate", rs.getDate("IssueDate"));
                bill.put("dueDate", rs.getDate("DueDate"));
                bill.put("status", rs.getString("Status"));
                bill.put("reference", rs.getString("Reference"));
                bill.put("paymentStatus", rs.getString("payment_status"));
                return bill;
            } catch (SQLException e) {
                return null;
            }
        }, startDate, endDate);
    }
}
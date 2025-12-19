package com.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class StatsDAO extends BaseDAO {
    
    public StatsDAO() {
        // Constructor doesn't need to do anything that throws SQLException
    }
    
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Load all stats with null safety
            stats.put("totalSubscribers", getTotalSubscribers());
            stats.put("pendingBills", getPendingBillsCount());
            stats.put("totalRevenue", getTotalRevenue());
            stats.put("openComplaints", getOpenComplaintsCount());
            stats.put("newSubscribersThisMonth", getNewSubscribersThisMonth());
            stats.put("resolvedComplaints", getResolvedComplaintsCount());
            stats.put("monthlyRevenue", getMonthlyRevenue());
            stats.put("totalAdmins", getTotalAdmins());
            stats.put("totalServices", getTotalServices());
            stats.put("totalPayments", getTotalPayments());
            stats.put("unverifiedReadings", getUnverifiedMeterReadings());
            stats.put("overdueBills", getOverdueBillsCount());
            
            // Add real trend data
            stats.putAll(getTrendData());
            
        } catch (Exception e) {
            System.err.println("❌ Error in getDashboardStats: " + e.getMessage());
            // Set default values in case of error
            setDefaultStats(stats);
        }
        
        return stats;
    }
    
    private Map<String, Object> getTrendData() {
        Map<String, Object> trends = new HashMap<>();
        
        try {
            // Subscriber trends
            int currentSubscribers = getTotalSubscribers();
            int lastMonthSubscribers = getTotalSubscribersLastMonth();
            double subscriberGrowth = calculateGrowthRate(currentSubscribers, lastMonthSubscribers);
            trends.put("subscriberGrowth", subscriberGrowth);
            trends.put("subscriberTrend", getTrendDirection(subscriberGrowth));
            
            // Revenue trends
            double currentRevenue = getMonthlyRevenue();
            double lastMonthRevenue = getLastMonthRevenue();
            double revenueGrowth = calculateGrowthRate(currentRevenue, lastMonthRevenue);
            trends.put("revenueGrowth", revenueGrowth);
            trends.put("revenueTrend", getTrendDirection(revenueGrowth));
            
            // Complaint trends
            int currentComplaints = getOpenComplaintsCount();
            int lastMonthComplaints = getLastMonthComplaints();
            double complaintChange = calculateGrowthRate(currentComplaints, lastMonthComplaints);
            trends.put("complaintTrend", getTrendDirection(-complaintChange));
            
            // New subscribers today
            int newSubscribersToday = getNewSubscribersToday();
            trends.put("newSubscribersToday", newSubscribersToday);
            
            // Payment success rate - FIXED: Handle null safely
            double paymentSuccessRate = getPaymentSuccessRate();
            trends.put("paymentSuccessRate", paymentSuccessRate);
            
        } catch (Exception e) {
            System.err.println("❌ Error in getTrendData: " + e.getMessage());
            // Set safe default values
            trends.put("subscriberGrowth", 0.0);
            trends.put("revenueGrowth", 0.0);
            trends.put("paymentSuccessRate", 100.0);
            trends.put("newSubscribersToday", 0);
        }
        
        return trends;
    }
    
    // FIXED: Safe null handling in getPaymentSuccessRate
    private double getPaymentSuccessRate() {
        String sql = "SELECT " +
                    "COUNT(*) as total, " +
                    "SUM(CASE WHEN Status = 'Completed' THEN 1 ELSE 0 END) as successful " +
                    "FROM payment WHERE Date >= CURDATE() - INTERVAL 30 DAY";
        
        try {
            List<Map<String, Object>> results = executeSimpleQuery(sql);
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                
                // FIXED: Safe null handling
                Object totalObj = row.get("total");
                Object successfulObj = row.get("successful");
                
                // Convert to numbers safely
                int total = 0;
                int successful = 0;
                
                if (totalObj instanceof Number) {
                    total = ((Number) totalObj).intValue();
                } else if (totalObj != null) {
                    total = Integer.parseInt(totalObj.toString());
                }
                
                if (successfulObj instanceof Number) {
                    successful = ((Number) successfulObj).intValue();
                } else if (successfulObj != null) {
                    successful = Integer.parseInt(successfulObj.toString());
                }
                
                if (total > 0) {
                    return (successful * 100.0) / total;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getPaymentSuccessRate: " + e.getMessage());
        }
        
        return 100.0; // Default to 100% if no data or error
    }
    
    // FIXED: Added null-safe helper methods
    private int getTotalSubscribersLastMonth() {
        String sql = "SELECT COUNT(*) as count FROM subscriber WHERE Role = 'Subscriber' " +
                    "AND MONTH(CreatedAt) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
                    "AND YEAR(CreatedAt) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)";
        return getSafeInt(sql, "count");
    }
    
    private double getLastMonthRevenue() {
        String sql = "SELECT COALESCE(SUM(Amount), 0) as total FROM bill " +
                    "WHERE Status = 'Paid' AND MONTH(IssueDate) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
                    "AND YEAR(IssueDate) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)";
        return getSafeDouble(sql, "total");
    }
    
    private int getLastMonthComplaints() {
        String sql = "SELECT COUNT(*) as count FROM complaint " +
                    "WHERE MONTH(CreatedDate) = MONTH(CURRENT_DATE - INTERVAL 1 MONTH) " +
                    "AND YEAR(CreatedDate) = YEAR(CURRENT_DATE - INTERVAL 1 MONTH)";
        return getSafeInt(sql, "count");
    }
    
    private int getNewSubscribersToday() {
        String sql = "SELECT COUNT(*) as count FROM subscriber WHERE Role = 'Subscriber' " +
                    "AND DATE(CreatedAt) = CURDATE()";
        return getSafeInt(sql, "count");
    }
    
    // Helper methods for safe data retrieval
    private int getSafeInt(String sql, String column) {
        try {
            Integer result = executeQuerySingle(sql, rs -> {
                try {
                    return rs.getInt(column);
                } catch (SQLException e) {
                    return 0;
                }
            });
            return result != null ? result : 0;
        } catch (Exception e) {
            System.err.println("❌ Error in getSafeInt: " + e.getMessage());
            return 0;
        }
    }
    
    private double getSafeDouble(String sql, String column) {
        try {
            Double result = executeQuerySingle(sql, rs -> {
                try {
                    return rs.getDouble(column);
                } catch (SQLException e) {
                    return 0.0;
                }
            });
            return result != null ? result : 0.0;
        } catch (Exception e) {
            System.err.println("❌ Error in getSafeDouble: " + e.getMessage());
            return 0.0;
        }
    }
    
    // FIXED: Original methods with null safety
    private int getTotalSubscribers() {
        return getSafeInt("SELECT COUNT(*) as count FROM subscriber WHERE Role = 'Subscriber'", "count");
    }
    
    private int getPendingBillsCount() {
        return getSafeInt("SELECT COUNT(*) as count FROM bill WHERE Status = 'Pending'", "count");
    }
    
    private double getTotalRevenue() {
        return getSafeDouble("SELECT COALESCE(SUM(Amount), 0) as total FROM bill WHERE Status = 'Paid'", "total");
    }
    
    private int getOpenComplaintsCount() {
        return getSafeInt("SELECT COUNT(*) as count FROM complaint WHERE Status IN ('Open', 'In Progress')", "count");
    }
    
    private int getNewSubscribersThisMonth() {
        String sql = "SELECT COUNT(*) as count FROM subscriber WHERE Role = 'Subscriber' " +
                    "AND MONTH(CreatedAt) = MONTH(CURRENT_DATE()) AND YEAR(CreatedAt) = YEAR(CURRENT_DATE())";
        return getSafeInt(sql, "count");
    }
    
    private int getResolvedComplaintsCount() {
        return getSafeInt("SELECT COUNT(*) as count FROM complaint WHERE Status = 'Resolved'", "count");
    }
    
    private double getMonthlyRevenue() {
        String sql = "SELECT COALESCE(SUM(Amount), 0) as total FROM bill " +
                    "WHERE Status = 'Paid' AND MONTH(IssueDate) = MONTH(CURRENT_DATE()) " +
                    "AND YEAR(IssueDate) = YEAR(CURRENT_DATE())";
        return getSafeDouble(sql, "total");
    }
    
    private int getTotalAdmins() {
        return getSafeInt("SELECT COUNT(*) as count FROM subscriber WHERE Role = 'Admin'", "count");
    }
    
    private int getTotalServices() {
        return getSafeInt("SELECT COUNT(*) as count FROM service WHERE Status = 'Active'", "count");
    }
    
    private int getTotalPayments() {
        return getSafeInt("SELECT COUNT(*) as count FROM payment WHERE Status = 'Completed'", "count");
    }
    
    private int getUnverifiedMeterReadings() {
        return getSafeInt("SELECT COUNT(*) as count FROM meter WHERE Status = 'Pending'", "count");
    }
    
    private int getOverdueBillsCount() {
        return getSafeInt("SELECT COUNT(*) as count FROM bill WHERE Status = 'Overdue'", "count");
    }
    
    // Helper methods for trend calculations
    private double calculateGrowthRate(double current, double previous) {
        if (previous == 0) return current > 0 ? 100.0 : 0.0;
        return ((current - previous) / previous) * 100.0;
    }
    
    private String getTrendDirection(double growthRate) {
        if (growthRate > 5) return "🚀 Excellent";
        if (growthRate > 2) return "📈 Good";
        if (growthRate > -2) return "➡️ Stable";
        if (growthRate > -5) return "📉 Slow";
        return "⚠️ Needs attention";
    }
    
    // FIXED: Additional statistics methods with null safety
    public Map<String, Object> getBillingStats() {
        Map<String, Object> billingStats = new HashMap<>();
        
        String sql = "SELECT " +
                    "COUNT(*) as totalBills, " +
                    "COALESCE(SUM(CASE WHEN Status = 'Paid' THEN Amount ELSE 0 END), 0) as paidAmount, " +
                    "COALESCE(SUM(CASE WHEN Status = 'Pending' THEN Amount ELSE 0 END), 0) as pendingAmount, " +
                    "COALESCE(SUM(CASE WHEN Status = 'Overdue' THEN Amount ELSE 0 END), 0) as overdueAmount " +
                    "FROM bill";
        
        try {
            List<Map<String, Object>> results = executeSimpleQuery(sql);
            if (!results.isEmpty()) {
                Map<String, Object> row = results.get(0);
                billingStats.put("totalBills", getSafeValue(row.get("totalBills"), 0));
                billingStats.put("paidAmount", getSafeValue(row.get("paidAmount"), 0.0));
                billingStats.put("pendingAmount", getSafeValue(row.get("pendingAmount"), 0.0));
                billingStats.put("overdueAmount", getSafeValue(row.get("overdueAmount"), 0.0));
            } else {
                billingStats.put("totalBills", 0);
                billingStats.put("paidAmount", 0.0);
                billingStats.put("pendingAmount", 0.0);
                billingStats.put("overdueAmount", 0.0);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getBillingStats: " + e.getMessage());
            billingStats.put("totalBills", 0);
            billingStats.put("paidAmount", 0.0);
            billingStats.put("pendingAmount", 0.0);
            billingStats.put("overdueAmount", 0.0);
        }
        
        return billingStats;
    }
    
    // FIXED: Safe value extraction helper
    private Object getSafeValue(Object value, Object defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        
        if (defaultValue instanceof Integer && value instanceof Number) {
            return ((Number) value).intValue();
        } else if (defaultValue instanceof Double && value instanceof Number) {
            return ((Number) value).doubleValue();
        } else if (defaultValue instanceof String) {
            return value.toString();
        }
        
        return value;
    }
    
    public Map<String, Integer> getComplaintStats() {
        Map<String, Integer> complaintStats = new HashMap<>();
        
        // Initialize with all possible statuses
        complaintStats.put("Open", 0);
        complaintStats.put("In Progress", 0);
        complaintStats.put("Resolved", 0);
        complaintStats.put("Closed", 0);
        
        try {
            String sql = "SELECT Status, COUNT(*) as count FROM complaint GROUP BY Status";
            
            List<Map<String, Object>> results = executeSimpleQuery(sql);
            for (Map<String, Object> row : results) {
                String status = (String) row.get("Status");
                Integer count = ((Number) row.get("count")).intValue();
                complaintStats.put(status, count);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getComplaintStats: " + e.getMessage());
        }
        
        return complaintStats;
    }
    
    public Map<String, Integer> getServiceStats() {
        Map<String, Integer> serviceStats = new HashMap<>();
        
        try {
            String sql = "SELECT Services, COUNT(*) as count FROM bill GROUP BY Services";
            
            List<Map<String, Object>> results = executeSimpleQuery(sql);
            for (Map<String, Object> row : results) {
                String service = (String) row.get("Services");
                Integer count = ((Number) row.get("count")).intValue();
                serviceStats.put(service, count);
            }
        } catch (Exception e) {
            System.err.println("❌ Error in getServiceStats: " + e.getMessage());
        }
        
        return serviceStats;
    }
    
    // Simple helper method for direct SQL execution
    private List<Map<String, Object>> executeSimpleQuery(String sql) {
        try {
            return executeQuery(sql, rs -> {
                try {
                    Map<String, Object> row = new HashMap<>();
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();
                    
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = metaData.getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    return row;
                } catch (SQLException e) {
                    System.err.println("❌ Error mapping row from ResultSet: " + e.getMessage());
                    return null;
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Error in executeSimpleQuery: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Set default stats in case of errors
    private void setDefaultStats(Map<String, Object> stats) {
        stats.put("totalSubscribers", 0);
        stats.put("pendingBills", 0);
        stats.put("totalRevenue", 0.0);
        stats.put("openComplaints", 0);
        stats.put("newSubscribersThisMonth", 0);
        stats.put("resolvedComplaints", 0);
        stats.put("monthlyRevenue", 0.0);
        stats.put("totalAdmins", 1);
        stats.put("totalServices", 4);
        stats.put("totalPayments", 0);
        stats.put("unverifiedReadings", 0);
        stats.put("overdueBills", 0);
        stats.put("subscriberGrowth", 0.0);
        stats.put("revenueGrowth", 0.0);
        stats.put("paymentSuccessRate", 100.0);
        stats.put("newSubscribersToday", 0);
    }
}
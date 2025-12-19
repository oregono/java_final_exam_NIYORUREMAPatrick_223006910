package com.dao;

import com.models.Service;
import java.sql.*;
import java.util.List;

public class ServiceDAO extends BaseDAO {
    
    public List<Service> getAllServices() {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service WHERE Status = 'Active' ORDER BY Name";
        
        return executeQuery(sql, this::mapService);
    }
    
    public List<Service> getAllServicesIncludingInactive() {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service ORDER BY Status, Name";
        
        return executeQuery(sql, this::mapService);
    }
    
    public Service getServiceById(int serviceId) {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service WHERE ServiceID = ?";
        
        return executeQuerySingle(sql, this::mapService, serviceId);
    }
    
    public Service getServiceByName(String name) {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service WHERE Name = ?";
        
        return executeQuerySingle(sql, this::mapService, name);
    }
    
    public List<Service> getServicesByCategory(String category) {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service WHERE Category = ? AND Status = 'Active' ORDER BY Name";
        
        return executeQuery(sql, this::mapService, category);
    }
    
    public List<Service> searchServices(String searchTerm) {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service WHERE (Name LIKE ? OR Description LIKE ? OR Category LIKE ?) " +
                    "AND Status = 'Active' ORDER BY Name";
        
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(sql, this::mapService, searchPattern, searchPattern, searchPattern);
    }
    
    public boolean createService(Service service) {
        if (!service.isValid()) {
            throw new IllegalArgumentException("Invalid service: " + service.getValidationErrors());
        }
        
        String sql = "INSERT INTO service (Name, Description, Category, Price, Status) VALUES (?, ?, ?, ?, ?)";
        
        int rowsAffected = executeUpdate(sql,
            service.getName(),
            service.getDescription(),
            service.getCategory(),
            service.getPrice(),
            service.getStatus()
        );
        
        return rowsAffected > 0;
    }
    
    public boolean updateService(Service service) {
        if (!service.isValid()) {
            throw new IllegalArgumentException("Invalid service: " + service.getValidationErrors());
        }
        
        String sql = "UPDATE service SET Name = ?, Description = ?, Category = ?, Price = ?, Status = ? " +
                    "WHERE ServiceID = ?";
        
        int rowsAffected = executeUpdate(sql,
            service.getName(),
            service.getDescription(),
            service.getCategory(),
            service.getPrice(),
            service.getStatus(),
            service.getServiceId()
        );
        
        return rowsAffected > 0;
    }
    
    public boolean deactivateService(int serviceId) {
        String sql = "UPDATE service SET Status = 'Inactive' WHERE ServiceID = ?";
        int rowsAffected = executeUpdate(sql, serviceId);
        return rowsAffected > 0;
    }
    
    public boolean activateService(int serviceId) {
        String sql = "UPDATE service SET Status = 'Active' WHERE ServiceID = ?";
        int rowsAffected = executeUpdate(sql, serviceId);
        return rowsAffected > 0;
    }
    
    public boolean deleteService(int serviceId) {
        String sql = "DELETE FROM service WHERE ServiceID = ?";
        int rowsAffected = executeUpdate(sql, serviceId);
        return rowsAffected > 0;
    }
    
    public List<String> getServiceCategories() {
        String sql = "SELECT DISTINCT Category FROM service WHERE Status = 'Active' ORDER BY Category";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Category");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<String> getAllServiceCategories() {
        String sql = "SELECT DISTINCT Category FROM service ORDER BY Category";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Category");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public int getServiceCount() {
        String sql = "SELECT COUNT(*) as count FROM service WHERE Status = 'Active'";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        });
        return count != null ? count : 0;
    }
    
    public int getServiceCountByCategory(String category) {
        String sql = "SELECT COUNT(*) as count FROM service WHERE Category = ? AND Status = 'Active'";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, category);
        return count != null ? count : 0;
    }
    
    public boolean isServiceNameExists(String name) {
        String sql = "SELECT COUNT(*) as count FROM service WHERE Name = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, name);
        return count != null && count > 0;
    }
    
    public boolean isServiceNameExists(String name, int excludeServiceId) {
        String sql = "SELECT COUNT(*) as count FROM service WHERE Name = ? AND ServiceID != ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, name, excludeServiceId);
        return count != null && count > 0;
    }
    
    public List<Service> getServicesWithPagination(int offset, int limit) {
        String sql = "SELECT ServiceID, Name, Description, Category, Price, Status, CreatedAt " +
                    "FROM service ORDER BY Name LIMIT ? OFFSET ?";
        
        return executeQuery(sql, this::mapService, limit, offset);
    }
    
    public int getTotalServiceCount() {
        String sql = "SELECT COUNT(*) as count FROM service";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        });
        return count != null ? count : 0;
    }
    
    public List<ServiceStats> getServiceStatistics() {
        String sql = "SELECT Category, Status, COUNT(*) as count, AVG(Price) as avgPrice " +
                    "FROM service GROUP BY Category, Status ORDER BY Category, Status";
        
        return executeQuery(sql, rs -> {
            try {
                return new ServiceStats(
                    rs.getString("Category"),
                    rs.getString("Status"),
                    rs.getInt("count"),
                    rs.getDouble("avgPrice")
                );
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    private Service mapService(ResultSet rs) {
        try {
            return new Service(
                rs.getInt("ServiceID"),
                rs.getString("Name"),
                rs.getString("Description"),
                rs.getString("Category"),
                rs.getDouble("Price"),
                rs.getString("Status"),
                rs.getTimestamp("CreatedAt")
            );
        } catch (SQLException e) {
            System.err.println("❌ Error mapping service from ResultSet: " + e.getMessage());
            return null;
        }
    }
    
    // Statistics class for service analytics
    public static class ServiceStats {
        private String category;
        private String status;
        private int count;
        private double avgPrice;
        
        public ServiceStats(String category, String status, int count, double avgPrice) {
            this.category = category;
            this.status = status;
            this.count = count;
            this.avgPrice = avgPrice;
        }
        
        // Getters
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public int getCount() { return count; }
        public double getAvgPrice() { return avgPrice; }
        
        @Override
        public String toString() {
            return String.format("ServiceStats{category='%s', status='%s', count=%d, avgPrice=%.2f}", 
                category, status, count, avgPrice);
        }
    }
}
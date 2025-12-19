package com.dao;

import com.dao.MeterReading;
import java.sql.*;
import java.util.List;

public class MeterDAO extends BaseDAO {
    
    public boolean addMeterReading(MeterReading reading) {
        if (!reading.isValid()) {
            throw new IllegalArgumentException("Invalid meter reading: " + reading.getValidationErrors());
        }
        
        String sql = "INSERT INTO meter (Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = executeUpdate(sql,
            reading.getSubscriber(),
            reading.getService(),
            reading.getReading(),
            reading.getUnit(),
            reading.getDate(),
            reading.getType(),
            reading.getStatus(),
            reading.getConsumption()
        );
        
        return rowsAffected > 0;
    }
    
    public List<MeterReading> getMeterReadingsBySubscriber(String subscriber) {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Subscriber = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapMeterReading, subscriber);
    }
    
    public List<MeterReading> getAllMeterReadings() {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapMeterReading);
    }
    
    public MeterReading getLatestReading(String subscriber, String service) {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Subscriber = ? AND Service = ? ORDER BY Date DESC LIMIT 1";
        
        List<MeterReading> readings = executeQuery(sql, this::mapMeterReading, subscriber, service);
        return readings.isEmpty() ? null : readings.get(0);
    }
    
    public boolean verifyMeterReading(int meterId) {
        String sql = "UPDATE meter SET Status = 'Verified' WHERE id = ?";
        int rowsAffected = executeUpdate(sql, meterId);
        return rowsAffected > 0;
    }
    
    public boolean deleteMeterReading(int meterId) {
        String sql = "DELETE FROM meter WHERE id = ?";
        int rowsAffected = executeUpdate(sql, meterId);
        return rowsAffected > 0;
    }
    
    public List<MeterReading> getUnverifiedReadings() {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Status = 'Pending' ORDER BY Date ASC";
        
        return executeQuery(sql, this::mapMeterReading);
    }
    
    public List<MeterReading> getMeterReadingsByService(String service) {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Service = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapMeterReading, service);
    }
    
    public List<MeterReading> getMeterReadingsByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Date BETWEEN ? AND ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapMeterReading, startDate, endDate);
    }
    
    public int getMeterReadingCount(String subscriber) {
        String sql = "SELECT COUNT(*) as count FROM meter WHERE Subscriber = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, subscriber);
        return count != null ? count : 0;
    }
    
    public double getTotalConsumption(String subscriber, String service) {
        String sql = "SELECT SUM(Consumption) as total FROM meter WHERE Subscriber = ? AND Service = ?";
        
        Double total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getDouble("total");
            } catch (SQLException e) {
                return 0.0;
            }
        }, subscriber, service);
        return total != null ? total : 0.0;
    }
    
    public boolean updateMeterReading(MeterReading reading) {
        if (!reading.isValid()) {
            throw new IllegalArgumentException("Invalid meter reading: " + reading.getValidationErrors());
        }
        
        String sql = "UPDATE meter SET Service = ?, Reading = ?, Unit = ?, Date = ?, " +
                    "Type = ?, Status = ?, Consumption = ? WHERE id = ?";
        
        int rowsAffected = executeUpdate(sql,
            reading.getService(),
            reading.getReading(),
            reading.getUnit(),
            reading.getDate(),
            reading.getType(),
            reading.getStatus(),
            reading.getConsumption(),
            reading.getId()
        );
        
        return rowsAffected > 0;
    }
    
    public MeterReading getMeterReadingById(int meterId) {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE id = ?";
        
        return executeQuerySingle(sql, this::mapMeterReading, meterId);
    }
    
    public List<String> getSubscribersWithReadings() {
        String sql = "SELECT DISTINCT Subscriber FROM meter ORDER BY Subscriber";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Subscriber");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<String> getServicesWithReadings() {
        String sql = "SELECT DISTINCT Service FROM meter ORDER BY Service";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Service");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<MeterReading> getOverdueReadings() {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Status = 'Overdue' ORDER BY Date ASC";
        
        return executeQuery(sql, this::mapMeterReading);
    }
    
    public boolean markAsOverdue(int meterId) {
        String sql = "UPDATE meter SET Status = 'Overdue' WHERE id = ?";
        int rowsAffected = executeUpdate(sql, meterId);
        return rowsAffected > 0;
    }
    
    public List<MeterReading> getReadingsByStatus(String status) {
        String sql = "SELECT id, Subscriber, Service, Reading, Unit, Date, Type, Status, Consumption " +
                    "FROM meter WHERE Status = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapMeterReading, status);
    }
    
    // Calculate consumption between two readings
    public Double calculateConsumption(String subscriber, String service, Date startDate, Date endDate) {
        String sql = "SELECT SUM(Consumption) as total FROM meter " +
                    "WHERE Subscriber = ? AND Service = ? AND Date BETWEEN ? AND ?";
        
        return executeQuerySingle(sql, rs -> {
            try {
                return rs.getDouble("total");
            } catch (SQLException e) {
                return 0.0;
            }
        }, subscriber, service, startDate, endDate);
    }
    
    private MeterReading mapMeterReading(ResultSet rs) {
        try {
            return MeterReading.builder()
                .id(rs.getInt("id"))
                .subscriber(rs.getString("Subscriber"))
                .service(rs.getString("Service"))
                .reading(rs.getDouble("Reading"))
                .unit(rs.getString("Unit"))
                .date(rs.getTimestamp("Date"))
                .type(rs.getString("Type"))
                .status(rs.getString("Status"))
                .consumption(rs.getInt("Consumption"))
                .build();
        } catch (SQLException e) {
            System.err.println("❌ Error mapping meter reading from ResultSet: " + e.getMessage());
            return null;
        }
    }
}
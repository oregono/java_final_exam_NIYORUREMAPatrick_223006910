package com.dao;

import com.dao.Payment;
import java.sql.*;
import java.util.List;

public class PaymentDAO extends BaseDAO {
    
    public boolean createPayment(Payment payment) {
        if (!payment.isValid()) {
            throw new IllegalArgumentException("Invalid payment: " + payment.getValidationErrors());
        }
        
        String sql = "INSERT INTO payment (BillID, Amount, Date, Method, Reference, Status, Subscriber) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = executeUpdate(sql,
            payment.getBillId(),
            payment.getAmount(),
            payment.getDate(),
            payment.getMethod(),
            payment.getReference(),
            payment.getStatus(),
            payment.getSubscriber()
        );
        
        return rowsAffected > 0;
    }
    
    public List<Payment> getPaymentsBySubscriber(String subscriber) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Subscriber = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment, subscriber);
    }
    
    public List<Payment> getAllPayments() {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment);
    }
    
    public Payment getPaymentById(int paymentId) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE PaymentID = ?";
        
        return executeQuerySingle(sql, this::mapPayment, paymentId);
    }
    
    public Payment getPaymentByReference(String reference) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Reference = ?";
        
        return executeQuerySingle(sql, this::mapPayment, reference);
    }
    
    public List<Payment> getPaymentsByBillId(String billId) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE BillID = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment, billId);
    }
    
    public boolean updatePaymentStatus(int paymentId, String status) {
        String sql = "UPDATE payment SET Status = ? WHERE PaymentID = ?";
        int rowsAffected = executeUpdate(sql, status, paymentId);
        return rowsAffected > 0;
    }
    
    public boolean markPaymentAsCompleted(int paymentId) {
        return updatePaymentStatus(paymentId, "Completed");
    }
    
    public boolean markPaymentAsFailed(int paymentId) {
        return updatePaymentStatus(paymentId, "Failed");
    }
    
    public List<Payment> getPaymentsByStatus(String status) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Status = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment, status);
    }
    
    public List<Payment> getPaymentsByMethod(String method) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Method = ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment, method);
    }
    
    public List<Payment> getPaymentsByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Date BETWEEN ? AND ? ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment, startDate, endDate);
    }
    
    public List<Payment> getCompletedPayments() {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Status = 'Completed' ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment);
    }
    
    public List<Payment> getFailedPayments() {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE Status = 'Failed' ORDER BY Date DESC";
        
        return executeQuery(sql, this::mapPayment);
    }
    
    public int getPaymentCountBySubscriber(String subscriber) {
        String sql = "SELECT COUNT(*) as count FROM payment WHERE Subscriber = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, subscriber);
        return count != null ? count : 0;
    }
    
    public int getPaymentCountByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM payment WHERE Status = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, status);
        return count != null ? count : 0;
    }
    
    public double getTotalPaymentsBySubscriber(String subscriber) {
        String sql = "SELECT SUM(Amount) as total FROM payment WHERE Subscriber = ? AND Status = 'Completed'";
        
        Double total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getDouble("total");
            } catch (SQLException e) {
                return 0.0;
            }
        }, subscriber);
        return total != null ? total : 0.0;
    }
    
    public double getTotalRevenue() {
        String sql = "SELECT SUM(Amount) as total FROM payment WHERE Status = 'Completed'";
        
        Double total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getDouble("total");
            } catch (SQLException e) {
                return 0.0;
            }
        });
        return total != null ? total : 0.0;
    }
    
    public double getTotalRevenueByMethod(String method) {
        String sql = "SELECT SUM(Amount) as total FROM payment WHERE Method = ? AND Status = 'Completed'";
        
        Double total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getDouble("total");
            } catch (SQLException e) {
                return 0.0;
            }
        }, method);
        return total != null ? total : 0.0;
    }
    
    public List<Payment> searchPayments(String searchTerm) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment WHERE PaymentID LIKE ? OR BillID LIKE ? OR Reference LIKE ? OR Subscriber LIKE ? " +
                    "ORDER BY Date DESC";
        
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(sql, this::mapPayment, searchPattern, searchPattern, searchPattern, searchPattern);
    }
    
    public List<String> getPaymentSubscribers() {
        String sql = "SELECT DISTINCT Subscriber FROM payment ORDER BY Subscriber";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Subscriber");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<PaymentStats> getPaymentStatistics() {
        String sql = "SELECT Method, Status, COUNT(*) as count, SUM(Amount) as total " +
                    "FROM payment GROUP BY Method, Status ORDER BY Method, Status";
        
        return executeQuery(sql, rs -> {
            try {
                return new PaymentStats(
                    rs.getString("Method"),
                    rs.getString("Status"),
                    rs.getInt("count"),
                    rs.getDouble("total")
                );
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<Payment> getPaymentsWithPagination(int offset, int limit) {
        String sql = "SELECT PaymentID, BillID, Amount, Date, Method, Reference, Status, Subscriber " +
                    "FROM payment ORDER BY Date DESC LIMIT ? OFFSET ?";
        
        return executeQuery(sql, this::mapPayment, limit, offset);
    }
    
    public int getTotalPaymentCount() {
        String sql = "SELECT COUNT(*) as count FROM payment";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        });
        return count != null ? count : 0;
    }
    
    // Fixed: Remove 'private' and handle SQLException internally
    Payment mapPayment(ResultSet rs) {
        try {
            return Payment.builder()
                .paymentId(rs.getInt("PaymentID"))
                .billId(rs.getString("BillID"))
                .amount(rs.getDouble("Amount"))
                .date(rs.getTimestamp("Date"))
                .method(rs.getString("Method"))
                .reference(rs.getString("Reference"))
                .status(rs.getString("Status"))
                .subscriber(rs.getString("Subscriber"))
                .build();
        } catch (SQLException e) {
            System.err.println("❌ Error mapping payment from ResultSet: " + e.getMessage());
            return null;
        }
    }
    
    // Statistics class for payment analytics
    public static class PaymentStats {
        private String method;
        private String status;
        private int count;
        private double total;
        
        public PaymentStats(String method, String status, int count, double total) {
            this.method = method;
            this.status = status;
            this.count = count;
            this.total = total;
        }
        
        // Getters
        public String getMethod() { return method; }
        public String getStatus() { return status; }
        public int getCount() { return count; }
        public double getTotal() { return total; }
        
        @Override
        public String toString() {
            return String.format("PaymentStats{method='%s', status='%s', count=%d, total=%.2f}", 
                method, status, count, total);
        }
    }
}
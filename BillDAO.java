package com.dao;

import com.dao.Bill;
import java.math.BigDecimal;
import java.sql.*;
import java.util.List;

public class BillDAO extends BaseDAO {
    
    public boolean addBill(Bill bill) {
        if (!bill.isValid()) {
            throw new IllegalArgumentException("Invalid bill: " + bill.getValidationErrors());
        }
        
        String sql = "INSERT INTO bill (BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        int rowsAffected = executeUpdate(sql,
            bill.getBillId(),
            bill.getSubscriber(),
            bill.getServices(),
            bill.getAmount(),
            bill.getIssueDate(),
            bill.getDueDate(),
            bill.getStatus(),
            bill.getReference()
        );
        
        return rowsAffected > 0;
    }
    
    public List<Bill> getBillsBySubscriber(String subscriber) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Subscriber = ? ORDER BY IssueDate DESC";
        
        return executeQuery(sql, this::mapBill, subscriber);
    }
    
    public List<Bill> getAllBills() {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill ORDER BY IssueDate DESC";
        
        return executeQuery(sql, this::mapBill);
    }
    
    public Bill getBillById(String billId) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE BillID = ?";
        
        return executeQuerySingle(sql, this::mapBill, billId);
    }
    
    public Bill getBillByReference(String reference) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Reference = ?";
        
        return executeQuerySingle(sql, this::mapBill, reference);
    }
    
    public boolean updateBillStatus(String billId, String status) {
        String sql = "UPDATE bill SET Status = ? WHERE BillID = ?";
        int rowsAffected = executeUpdate(sql, status, billId);
        return rowsAffected > 0;
    }
    
    public boolean markBillAsPaid(String billId) {
        return updateBillStatus(billId, "Paid");
    }
    
    public boolean markBillAsOverdue(String billId) {
        return updateBillStatus(billId, "Overdue");
    }
    
    public boolean deleteBill(String billId) {
        String sql = "DELETE FROM bill WHERE BillID = ?";
        int rowsAffected = executeUpdate(sql, billId);
        return rowsAffected > 0;
    }
    
    public List<Bill> getBillsByStatus(String status) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Status = ? ORDER BY IssueDate DESC";
        
        return executeQuery(sql, this::mapBill, status);
    }
    
    public List<Bill> getBillsByService(String service) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Services = ? ORDER BY IssueDate DESC";
        
        return executeQuery(sql, this::mapBill, service);
    }
    
    public List<Bill> getBillsByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE IssueDate BETWEEN ? AND ? ORDER BY IssueDate DESC";
        
        return executeQuery(sql, this::mapBill, startDate, endDate);
    }
    
    public List<Bill> getOverdueBills() {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Status = 'Overdue' ORDER BY DueDate ASC";
        
        return executeQuery(sql, this::mapBill);
    }
    
    public List<Bill> getPendingBills() {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Status = 'Pending' ORDER BY DueDate ASC";
        
        return executeQuery(sql, this::mapBill);
    }
    
    public List<Bill> getDueSoonBills() {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE Status = 'Pending' AND DueDate BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) " +
                    "ORDER BY DueDate ASC";
        
        return executeQuery(sql, this::mapBill);
    }
    
    public int getBillCountBySubscriber(String subscriber) {
        String sql = "SELECT COUNT(*) as count FROM bill WHERE Subscriber = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, subscriber);
        return count != null ? count : 0;
    }
    
    public int getBillCountByStatus(String status) {
        String sql = "SELECT COUNT(*) as count FROM bill WHERE Status = ?";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        }, status);
        return count != null ? count : 0;
    }
    
    public BigDecimal getTotalAmountBySubscriber(String subscriber) {
        String sql = "SELECT SUM(Amount) as total FROM bill WHERE Subscriber = ?";
        
        BigDecimal total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getBigDecimal("total");
            } catch (SQLException e) {
                return BigDecimal.ZERO;
            }
        }, subscriber);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalPendingAmountBySubscriber(String subscriber) {
        String sql = "SELECT SUM(Amount) as total FROM bill WHERE Subscriber = ? AND Status = 'Pending'";
        
        BigDecimal total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getBigDecimal("total");
            } catch (SQLException e) {
                return BigDecimal.ZERO;
            }
        }, subscriber);
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalRevenue() {
        String sql = "SELECT SUM(Amount) as total FROM bill WHERE Status = 'Paid'";
        
        BigDecimal total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getBigDecimal("total");
            } catch (SQLException e) {
                return BigDecimal.ZERO;
            }
        });
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public BigDecimal getTotalPendingRevenue() {
        String sql = "SELECT SUM(Amount) as total FROM bill WHERE Status = 'Pending'";
        
        BigDecimal total = executeQuerySingle(sql, rs -> {
            try {
                return rs.getBigDecimal("total");
            } catch (SQLException e) {
                return BigDecimal.ZERO;
            }
        });
        return total != null ? total : BigDecimal.ZERO;
    }
    
    public boolean updateBill(Bill bill) {
        if (!bill.isValid()) {
            throw new IllegalArgumentException("Invalid bill: " + bill.getValidationErrors());
        }
        
        String sql = "UPDATE bill SET Subscriber = ?, Services = ?, Amount = ?, IssueDate = ?, " +
                    "DueDate = ?, Status = ?, Reference = ? WHERE BillID = ?";
        
        int rowsAffected = executeUpdate(sql,
            bill.getSubscriber(),
            bill.getServices(),
            bill.getAmount(),
            bill.getIssueDate(),
            bill.getDueDate(),
            bill.getStatus(),
            bill.getReference(),
            bill.getBillId()
        );
        
        return rowsAffected > 0;
    }
    
    public List<String> getBillSubscribers() {
        String sql = "SELECT DISTINCT Subscriber FROM bill ORDER BY Subscriber";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Subscriber");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<String> getBillServices() {
        String sql = "SELECT DISTINCT Services FROM bill ORDER BY Services";
        
        return executeQuery(sql, rs -> {
            try {
                return rs.getString("Services");
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<Bill> searchBills(String searchTerm) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE BillID LIKE ? OR Subscriber LIKE ? OR Services LIKE ? OR Reference LIKE ? " +
                    "ORDER BY IssueDate DESC";
        
        String searchPattern = "%" + searchTerm + "%";
        return executeQuery(sql, this::mapBill, searchPattern, searchPattern, searchPattern, searchPattern);
    }
    
    public List<BillStats> getBillStatistics() {
        String sql = "SELECT Status, COUNT(*) as count, SUM(Amount) as total " +
                    "FROM bill GROUP BY Status ORDER BY Status";
        
        return executeQuery(sql, rs -> {
            try {
                return new BillStats(
                    rs.getString("Status"),
                    rs.getInt("count"),
                    rs.getBigDecimal("total")
                );
            } catch (SQLException e) {
                return null;
            }
        });
    }
    
    public List<Bill> getBillsDueBefore(Date date) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill WHERE DueDate < ? AND Status = 'Pending' ORDER BY DueDate ASC";
        
        return executeQuery(sql, this::mapBill, date);
    }
    
    // Method to generate next bill ID
    public String generateNextBillId() {
        String sql = "SELECT MAX(BillID) as maxId FROM bill WHERE BillID LIKE 'B-%'";
        
        String maxId = executeQuerySingle(sql, rs -> {
            try {
                return rs.getString("maxId");
            } catch (SQLException e) {
                return null;
            }
        });
        
        if (maxId == null) {
            return "B-001";
        }
        
        try {
            int lastNumber = Integer.parseInt(maxId.substring(2));
            return String.format("B-%03d", lastNumber + 1);
        } catch (NumberFormatException e) {
            return "B-001";
        }
    }
    
    // Method to get bills with pagination
    public List<Bill> getBillsWithPagination(int offset, int limit) {
        String sql = "SELECT id, BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                    "FROM bill ORDER BY IssueDate DESC LIMIT ? OFFSET ?";
        
        return executeQuery(sql, this::mapBill, limit, offset);
    }
    
    // Method to get total bill count for pagination
    public int getTotalBillCount() {
        String sql = "SELECT COUNT(*) as count FROM bill";
        
        Integer count = executeQuerySingle(sql, rs -> {
            try {
                return rs.getInt("count");
            } catch (SQLException e) {
                return 0;
            }
        });
        return count != null ? count : 0;
    }
    
    private Bill mapBill(ResultSet rs) {
        try {
            return Bill.builder()
                .id(rs.getInt("id"))
                .billId(rs.getString("BillID"))
                .subscriber(rs.getString("Subscriber"))
                .services(rs.getString("Services"))
                .amount(rs.getBigDecimal("Amount"))
                .issueDate(rs.getDate("IssueDate"))
                .dueDate(rs.getDate("DueDate"))
                .status(rs.getString("Status"))
                .reference(rs.getString("Reference"))
                .build();
        } catch (SQLException e) {
            System.err.println("❌ Error mapping bill from ResultSet: " + e.getMessage());
            return null;
        }
    }
    
    // Statistics class
    public static class BillStats {
        private String status;
        private int count;
        private BigDecimal total;
        
        public BillStats(String status, int count, BigDecimal total) {
            this.status = status;
            this.count = count;
            this.total = total;
        }
        
        // Getters
        public String getStatus() { return status; }
        public int getCount() { return count; }
        public BigDecimal getTotal() { return total; }
        
        @Override
        public String toString() {
            return String.format("BillStats{status='%s', count=%d, total=%.2f}", status, count, total);
        }
    }
}
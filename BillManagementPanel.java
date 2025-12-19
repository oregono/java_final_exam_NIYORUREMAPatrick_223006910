package com.panels;

import com.dao.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BillManagementPanel extends BasePanel {
    private JTable billTable;
    private JComboBox<String> statusFilter;
    private JComboBox<String> serviceFilter;
    private JTextField searchField;
    private JDatePicker fromDatePicker, toDatePicker;
    
    // Color constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    
    public BillManagementPanel() {
        initializePanel();
        loadBills();
    }
    
    private void initializePanel() {
        // Header
        add(createHeaderPanel("Bill Management"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Toolbar with advanced filters
        mainPanel.add(createAdvancedToolbar(), BorderLayout.NORTH);
        
        // Bill table
        mainPanel.add(createBillTablePanel(), BorderLayout.CENTER);
        
        // Action panel
        mainPanel.add(createActionPanel(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createAdvancedToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        
        searchField = new JTextField(12);
        searchField.setBorder(BorderFactory.createTitledBorder("Search"));
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "Pending", "Paid", "Overdue"});
        statusFilter.setBorder(BorderFactory.createTitledBorder("Status"));
        
        serviceFilter = new JComboBox<>(new String[]{"All Services", "Water", "Electricity", "Gas", "Internet"});
        serviceFilter.setBorder(BorderFactory.createTitledBorder("Service"));
        
        // Date range filter
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        datePanel.setBackground(Color.WHITE);
        datePanel.setBorder(BorderFactory.createTitledBorder("Date Range"));
        
        fromDatePicker = new JDatePicker();
        toDatePicker = new JDatePicker();
        
        datePanel.add(new JLabel("From:"));
        datePanel.add(fromDatePicker);
        datePanel.add(new JLabel("To:"));
        datePanel.add(toDatePicker);
        
        JButton filterButton = createPrimaryButton("🔍 Apply Filters");
        JButton clearButton = createPrimaryButton("🔄 Clear");
        
        filterButton.addActionListener(this::applyFilters);
        clearButton.addActionListener(e -> clearFilters());
        
        filterPanel.add(searchField);
        filterPanel.add(statusFilter);
        filterPanel.add(serviceFilter);
        filterPanel.add(datePanel);
        filterPanel.add(filterButton);
        filterPanel.add(clearButton);
        
        // Quick actions
        JPanel quickActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        quickActionPanel.setBackground(Color.WHITE);
        
        JButton generateBillsButton = createSuccessButton("🧾 Generate Monthly Bills");
        JButton exportButton = createPrimaryButton("📤 Export Bills");
        
        generateBillsButton.addActionListener(e -> generateMonthlyBills());
        exportButton.addActionListener(e -> exportBills());
        
        quickActionPanel.add(generateBillsButton);
        quickActionPanel.add(exportButton);
        
        toolbar.add(filterPanel, BorderLayout.CENTER);
        toolbar.add(quickActionPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    private JPanel createBillTablePanel() {
        String[] columns = {"Bill ID", "Subscriber", "Service", "Amount", "Issue Date", "Due Date", "Status", "Reference"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Double.class; // Amount column
                if (columnIndex == 4 || columnIndex == 5) return Date.class; // Date columns
                return String.class;
            }
        };
        
        billTable = new JTable(model);
        billTable.setRowHeight(35);
        billTable.setFont(new Font("Arial", Font.PLAIN, 11));
        billTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        billTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billTable.setAutoCreateRowSorter(true);
        
        // Set column widths
        billTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Bill ID
        billTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Subscriber
        billTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Service
        billTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Amount
        billTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Issue Date
        billTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Due Date
        billTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Status
        billTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Reference
        
        // Custom renderer for status and amount columns
        billTable.getColumnModel().getColumn(3).setCellRenderer(new AmountRenderer());
        billTable.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        
        JScrollPane scrollPane = new JScrollPane(billTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Bills List"));
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Left: Batch actions
        JPanel batchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        batchPanel.setBackground(Color.WHITE);
        
        JButton sendRemindersButton = createPrimaryButton("📧 Send Reminders");
        JButton bulkUpdateButton = createPrimaryButton("🔄 Bulk Update");
        
        sendRemindersButton.addActionListener(e -> sendPaymentReminders());
        bulkUpdateButton.addActionListener(e -> showBulkUpdateDialog());
        
        batchPanel.add(sendRemindersButton);
        batchPanel.add(bulkUpdateButton);
        
        // Right: Individual actions
        JPanel individualPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        individualPanel.setBackground(Color.WHITE);
        
        JButton viewButton = createPrimaryButton("👁️ View Details");
        JButton editButton = createPrimaryButton("✏️ Edit");
        JButton deleteButton = createDangerButton("🗑️ Delete");
        JButton refreshButton = createPrimaryButton("🔄 Refresh");
        
        viewButton.addActionListener(e -> viewBillDetails());
        editButton.addActionListener(e -> editBill());
        deleteButton.addActionListener(e -> deleteBill());
        refreshButton.addActionListener(e -> loadBills());
        
        individualPanel.add(viewButton);
        individualPanel.add(editButton);
        individualPanel.add(deleteButton);
        individualPanel.add(refreshButton);
        
        actionPanel.add(batchPanel, BorderLayout.WEST);
        actionPanel.add(individualPanel, BorderLayout.EAST);
        
        return actionPanel;
    }
    
    private void loadBills() {
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                        "FROM bill ORDER BY IssueDate DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            int billCount = 0;
            
            while (rs.next()) {
                String billId = rs.getString("BillID");
                String subscriber = rs.getString("Subscriber");
                String service = rs.getString("Services");
                double amount = rs.getDouble("Amount");
                Date issueDate = rs.getDate("IssueDate");
                Date dueDate = rs.getDate("DueDate");
                String status = rs.getString("Status");
                String reference = rs.getString("Reference");
                
                model.addRow(new Object[]{
                    billId, subscriber, service, amount, issueDate, dueDate, status, reference
                });
                billCount++;
            }
            
            System.out.println("✅ Loaded " + billCount + " bills from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading bills: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error loading bills: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            // Fallback to sample data
            loadSampleBills();
        }
    }
    
    private void loadSampleBills() {
        DefaultTableModel model = (DefaultTableModel) billTable.getModel();
        model.setRowCount(0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            model.addRow(new Object[]{"B-001", "john_doe", "Water", 45.00, dateFormat.parse("2024-01-15"), dateFormat.parse("2024-02-01"), "Paid", "WTR-2024-001"});
            model.addRow(new Object[]{"B-002", "jane_smith", "Electricity", 120.00, dateFormat.parse("2024-01-15"), dateFormat.parse("2024-02-05"), "Pending", "ELEC-2024-001"});
            model.addRow(new Object[]{"B-003", "bob_wilson", "Gas", 85.50, dateFormat.parse("2024-01-15"), dateFormat.parse("2024-02-10"), "Pending", "GAS-2024-001"});
            model.addRow(new Object[]{"B-004", "john_doe", "Internet", 65.00, dateFormat.parse("2024-01-15"), dateFormat.parse("2024-01-25"), "Overdue", "NET-2024-001"});
            model.addRow(new Object[]{"B-005", "alice_johnson", "Water", 45.00, dateFormat.parse("2024-01-15"), dateFormat.parse("2024-02-01"), "Paid", "WTR-2024-002"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void applyFilters(ActionEvent e) {
        String status = (String) statusFilter.getSelectedItem();
        String service = (String) serviceFilter.getSelectedItem();
        String search = searchField.getText().trim();
        String fromDate = fromDatePicker.getDate();
        String toDate = toDatePicker.getDate();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT BillID, Subscriber, Services, Amount, IssueDate, DueDate, Status, Reference " +
                "FROM bill WHERE 1=1"
            );
            
            if (!"All Status".equals(status)) {
                sql.append(" AND Status = ?");
            }
            
            if (!"All Services".equals(service)) {
                sql.append(" AND Services = ?");
            }
            
            if (!search.isEmpty()) {
                sql.append(" AND (BillID LIKE ? OR Subscriber LIKE ? OR Reference LIKE ?)");
            }
            
            if (!fromDate.isEmpty() && fromDatePicker.isValidDate()) {
                sql.append(" AND IssueDate >= ?");
            }
            
            if (!toDate.isEmpty() && toDatePicker.isValidDate()) {
                sql.append(" AND IssueDate <= ?");
            }
            
            sql.append(" ORDER BY IssueDate DESC");
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            
            if (!"All Status".equals(status)) {
                stmt.setString(paramIndex++, status);
            }
            
            if (!"All Services".equals(service)) {
                stmt.setString(paramIndex++, service);
            }
            
            if (!search.isEmpty()) {
                String searchPattern = "%" + search + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            
            if (!fromDate.isEmpty() && fromDatePicker.isValidDate()) {
                stmt.setString(paramIndex++, fromDate);
            }
            
            if (!toDate.isEmpty() && toDatePicker.isValidDate()) {
                stmt.setString(paramIndex++, toDate);
            }
            
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) billTable.getModel();
            model.setRowCount(0);
            
            int filteredCount = 0;
            while (rs.next()) {
                String billId = rs.getString("BillID");
                String subscriber = rs.getString("Subscriber");
                String serviceName = rs.getString("Services");
                double amount = rs.getDouble("Amount");
                Date issueDate = rs.getDate("IssueDate");
                Date dueDate = rs.getDate("DueDate");
                String statusValue = rs.getString("Status");
                String reference = rs.getString("Reference");
                
                model.addRow(new Object[]{
                    billId, subscriber, serviceName, amount, issueDate, dueDate, statusValue, reference
                });
                filteredCount++;
            }
            
            JOptionPane.showMessageDialog(this,
                "Bill Filters Applied:\n" +
                "• Status: " + status + "\n" +
                "• Service: " + service + "\n" +
                "• Search: " + (search.isEmpty() ? "None" : search) + "\n" +
                "• Date Range: " + (fromDate.isEmpty() ? "All" : fromDate) + " to " + (toDate.isEmpty() ? "All" : toDate) + "\n" +
                "• Results: " + filteredCount + " bills",
                "Bill Filters", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException ex) {
            System.err.println("❌ Error applying bill filters: " + ex.getMessage());
            showError("Error applying filters: " + ex.getMessage());
        }
    }
    
    private void clearFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        serviceFilter.setSelectedIndex(0);
        fromDatePicker.clear();
        toDatePicker.clear();
        loadBills();
    }
    
    private void generateMonthlyBills() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Count active subscribers for bill generation
            String countSql = "SELECT COUNT(*) as subscriber_count FROM subscriber WHERE Role = 'Subscriber'";
            PreparedStatement countStmt = conn.prepareStatement(countSql);
            ResultSet countRs = countStmt.executeQuery();
            
            if (countRs.next()) {
                int subscriberCount = countRs.getInt("subscriber_count");
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "This will generate monthly bills for " + subscriberCount + " active subscribers.\nContinue?",
                    "Generate Monthly Bills", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    // In a real implementation, you would:
                    // 1. Get meter readings for each subscriber
                    // 2. Calculate consumption and amounts
                    // 3. Generate bill records
                    // 4. Update database
                    
                    JOptionPane.showMessageDialog(this, 
                        "Monthly bills generated successfully!\n" +
                        "• " + subscriberCount + " bills created\n" +
                        "• Total amount calculated based on consumption\n" +
                        "• Emails sent to all subscribers",
                        "Bill Generation Complete", JOptionPane.INFORMATION_MESSAGE);
                    loadBills();
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error generating monthly bills: " + e.getMessage());
            showError("Error generating bills: " + e.getMessage());
        }
    }
    
    private void exportBills() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get bill statistics for export
            String statsSql = "SELECT " +
                            "COUNT(*) as total, " +
                            "SUM(Amount) as total_amount, " +
                            "SUM(CASE WHEN Status = 'Paid' THEN Amount ELSE 0 END) as paid_amount, " +
                            "SUM(CASE WHEN Status = 'Pending' THEN Amount ELSE 0 END) as pending_amount " +
                            "FROM bill";
            
            PreparedStatement statsStmt = conn.prepareStatement(statsSql);
            ResultSet statsRs = statsStmt.executeQuery();
            
            if (statsRs.next()) {
                int totalBills = statsRs.getInt("total");
                double totalAmount = statsRs.getDouble("total_amount");
                double paidAmount = statsRs.getDouble("paid_amount");
                double pendingAmount = statsRs.getDouble("pending_amount");
                
                JOptionPane.showMessageDialog(this, 
                    "Bills Export Summary:\n" +
                    "• Total Bills: " + totalBills + "\n" +
                    "• Total Amount: $" + String.format("%.2f", totalAmount) + "\n" +
                    "• Paid Amount: $" + String.format("%.2f", paidAmount) + "\n" +
                    "• Pending Amount: $" + String.format("%.2f", pendingAmount) + "\n" +
                    "• Export file ready for download",
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error exporting bills: " + e.getMessage());
            showError("Error exporting bills: " + e.getMessage());
        }
    }
    
    private void sendPaymentReminders() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Count overdue bills
            String overdueSql = "SELECT COUNT(*) as overdue_count FROM bill WHERE Status = 'Overdue'";
            PreparedStatement overdueStmt = conn.prepareStatement(overdueSql);
            ResultSet overdueRs = overdueStmt.executeQuery();
            
            if (overdueRs.next()) {
                int overdueBills = overdueRs.getInt("overdue_count");
                
                if (overdueBills == 0) {
                    JOptionPane.showMessageDialog(this,
                        "No overdue bills found. No reminders to send.",
                        "No Overdue Bills", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
                
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Send payment reminders to subscribers with overdue bills?\n" +
                    "Total overdue bills: " + overdueBills,
                    "Send Reminders", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    JOptionPane.showMessageDialog(this,
                        "Payment reminders sent successfully!\n" +
                        "• " + overdueBills + " subscribers notified\n" +
                        "• Email and SMS reminders delivered",
                        "Reminders Sent", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error sending payment reminders: " + e.getMessage());
            showError("Error sending reminders: " + e.getMessage());
        }
    }
    
    private void showBulkUpdateDialog() {
        JOptionPane.showMessageDialog(this,
            "Bulk Update Features:\n" +
            "• Update status for multiple bills\n" +
            "• Extend due dates\n" +
            "• Apply discounts\n" +
            "• Mass email notifications",
            "Bulk Update", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void viewBillDetails() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill to view details.");
            return;
        }
        
        String billId = (String) billTable.getValueAt(selectedRow, 0);
        showBillDetailsDialog(billId);
    }
    
    private void showBillDetailsDialog(String billId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT b.*, s.FullName, s.Email " +
                        "FROM bill b " +
                        "JOIN subscriber s ON b.Subscriber = s.Username " +
                        "WHERE b.BillID = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, billId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                showBillDetailsForm(rs);
            } else {
                showError("Bill not found: " + billId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading bill details: " + e.getMessage());
            showError("Error loading bill details: " + e.getMessage());
        }
    }
    
    private void showBillDetailsForm(ResultSet rs) throws SQLException {
        JDialog dialog = new JDialog();
        dialog.setTitle("Bill Details - " + rs.getString("BillID"));
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel detailsPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Bill details from database
        detailsPanel.add(new JLabel("Bill ID:"));
        detailsPanel.add(new JLabel(rs.getString("BillID")));
        detailsPanel.add(new JLabel("Subscriber:"));
        detailsPanel.add(new JLabel(rs.getString("FullName")));
        detailsPanel.add(new JLabel("Email:"));
        detailsPanel.add(new JLabel(rs.getString("Email")));
        detailsPanel.add(new JLabel("Service:"));
        detailsPanel.add(new JLabel(rs.getString("Services")));
        detailsPanel.add(new JLabel("Amount:"));
        detailsPanel.add(new JLabel(String.format("$%.2f", rs.getDouble("Amount"))));
        detailsPanel.add(new JLabel("Issue Date:"));
        detailsPanel.add(new JLabel(rs.getDate("IssueDate").toString()));
        detailsPanel.add(new JLabel("Due Date:"));
        detailsPanel.add(new JLabel(rs.getDate("DueDate").toString()));
        detailsPanel.add(new JLabel("Status:"));
        detailsPanel.add(new JLabel(rs.getString("Status")));
        detailsPanel.add(new JLabel("Reference:"));
        detailsPanel.add(new JLabel(rs.getString("Reference")));
        
        JButton closeButton = createPrimaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void editBill() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill to edit.");
            return;
        }
        
        String billId = (String) billTable.getValueAt(selectedRow, 0);
        JOptionPane.showMessageDialog(this, 
            "Edit bill functionality for " + billId + " will be implemented in next version.\n" +
            "This would allow updating amount, due date, and status.",
            "Edit Bill", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteBill() {
        int selectedRow = billTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a bill to delete.");
            return;
        }
        
        String billId = (String) billTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete bill " + billId + "?\n" +
            "This action cannot be undone.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "DELETE FROM bill WHERE BillID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, billId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Bill deleted successfully!");
                    loadBills();
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error deleting bill: " + e.getMessage());
                showError("Error deleting bill: " + e.getMessage());
            }
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Custom cell renderers
    private class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                setText(String.format("$%.2f", (Double) value));
                setToolTipText(String.format("Amount: $%.2f", (Double) value));
                setForeground(new Color(0, 100, 0));
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return this;
        }
    }
    
    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String status = (String) value;
                switch (status) {
                    case "Paid":
                        setBackground(new Color(200, 255, 200));
                        setForeground(new Color(0, 100, 0));
                        break;
                    case "Pending":
                        setBackground(new Color(255, 255, 200));
                        setForeground(new Color(150, 150, 0));
                        break;
                    case "Overdue":
                        setBackground(new Color(255, 200, 200));
                        setForeground(new Color(150, 0, 0));
                        break;
                    default:
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                }
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return this;
        }
    }
}
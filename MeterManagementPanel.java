package com.panels;

import com.dao.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;

public class MeterManagementPanel extends BasePanel {
    private JTable meterTable;
    private JComboBox<String> statusFilter;
    private JComboBox<String> serviceFilter;
    private JTextField searchField;
    
    // Color constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    
    public MeterManagementPanel() {
        initializePanel();
        loadMeterReadings();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("Meter Readings Management"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Toolbar with filters
        mainPanel.add(createToolbarPanel(), BorderLayout.NORTH);
        
        // Meter table
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        // Action panel
        mainPanel.add(createActionPanel(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createToolbarPanel() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        
        searchField = new JTextField(15);
        searchField.setBorder(BorderFactory.createTitledBorder("Search"));
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "Pending", "Verified", "Overdue"});
        statusFilter.setBorder(BorderFactory.createTitledBorder("Status"));
        
        serviceFilter = new JComboBox<>(new String[]{"All Services", "Water", "Electricity", "Gas", "Internet"});
        serviceFilter.setBorder(BorderFactory.createTitledBorder("Service"));
        
        JButton filterButton = createPrimaryButton("🔍 Apply Filters");
        JButton clearButton = createPrimaryButton("🔄 Clear");
        
        filterButton.addActionListener(this::applyFilters);
        clearButton.addActionListener(e -> clearFilters());
        
        filterPanel.add(searchField);
        filterPanel.add(statusFilter);
        filterPanel.add(serviceFilter);
        filterPanel.add(filterButton);
        filterPanel.add(clearButton);
        
        toolbar.add(filterPanel, BorderLayout.CENTER);
        
        return toolbar;
    }
    
    private JPanel createTablePanel() {
        String[] columns = {"ID", "Subscriber", "Service", "Reading", "Unit", "Consumption", "Date", "Type", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0 || columnIndex == 5) return Integer.class;
                if (columnIndex == 3) return Double.class;
                return String.class;
            }
        };
        
        meterTable = new JTable(model);
        meterTable.setRowHeight(35);
        meterTable.setFont(new Font("Arial", Font.PLAIN, 11));
        meterTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        meterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        meterTable.setAutoCreateRowSorter(true);
        meterTable.setShowGrid(true);
        meterTable.setGridColor(new Color(240, 240, 240));
        
        // Set column widths
        meterTable.getColumnModel().getColumn(0).setPreferredWidth(60);  // ID
        meterTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Subscriber
        meterTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Service
        meterTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Reading
        meterTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Unit
        meterTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Consumption
        meterTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Date
        meterTable.getColumnModel().getColumn(7).setPreferredWidth(80);  // Type
        meterTable.getColumnModel().getColumn(8).setPreferredWidth(80);  // Status
        
        // Custom renderers
        meterTable.getColumnModel().getColumn(3).setCellRenderer(new ReadingRenderer());
        meterTable.getColumnModel().getColumn(8).setCellRenderer(new StatusRenderer());
        
        JScrollPane scrollPane = new JScrollPane(meterTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Meter Readings"));
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createActionPanel() {
        JPanel actionPanel = new JPanel(new BorderLayout());
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Left: Batch actions
        JPanel batchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        batchPanel.setBackground(Color.WHITE);
        
        JButton verifyButton = createSuccessButton("✅ Verify Reading");
        JButton bulkUpdateButton = createPrimaryButton("🔄 Bulk Update");
        
        verifyButton.addActionListener(e -> verifySelectedReading());
        bulkUpdateButton.addActionListener(e -> showBulkUpdateDialog());
        
        batchPanel.add(verifyButton);
        batchPanel.add(bulkUpdateButton);
        
        // Right: Individual actions
        JPanel individualPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        individualPanel.setBackground(Color.WHITE);
        
        JButton viewButton = createPrimaryButton("👁️ View Details");
        JButton editButton = createPrimaryButton("✏️ Edit");
        JButton deleteButton = createDangerButton("🗑️ Delete");
        JButton refreshButton = createPrimaryButton("🔄 Refresh");
        
        viewButton.addActionListener(e -> viewMeterDetails());
        editButton.addActionListener(e -> editMeterReading());
        deleteButton.addActionListener(e -> deleteMeterReading());
        refreshButton.addActionListener(e -> loadMeterReadings());
        
        individualPanel.add(viewButton);
        individualPanel.add(editButton);
        individualPanel.add(deleteButton);
        individualPanel.add(refreshButton);
        
        actionPanel.add(batchPanel, BorderLayout.WEST);
        actionPanel.add(individualPanel, BorderLayout.EAST);
        
        return actionPanel;
    }
    
    private void loadMeterReadings() {
        System.out.println("🔄 Loading meter readings from database...");
        
        DefaultTableModel model = (DefaultTableModel) meterTable.getModel();
        model.setRowCount(0);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT id, Subscriber, Service, Reading, Unit, Consumption, Date, Type, Status " +
                        "FROM meter ORDER BY Date DESC";
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            int count = 0;
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String subscriber = rs.getString("Subscriber");
                String service = rs.getString("Service");
                double reading = rs.getDouble("Reading");
                String unit = rs.getString("Unit");
                int consumption = rs.getInt("Consumption");
                Timestamp date = rs.getTimestamp("Date");
                String type = rs.getString("Type");
                String status = rs.getString("Status");
                
                String dateStr = (date != null) ? dateFormat.format(date) : "N/A";
                String consumptionStr = (consumption > 0) ? String.valueOf(consumption) : "N/A";
                
                model.addRow(new Object[]{
                    id, subscriber, service, reading, unit, consumptionStr, dateStr, type, status
                });
                count++;
            }
            
            System.out.println("✅ Successfully loaded " + count + " meter readings from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Database error loading meter readings: " + e.getMessage());
            showErrorDialog("Database Error", "Error loading meter readings: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    private void applyFilters(ActionEvent e) {
        String status = (String) statusFilter.getSelectedItem();
        String service = (String) serviceFilter.getSelectedItem();
        String search = searchField.getText().trim();
        
        DefaultTableModel model = (DefaultTableModel) meterTable.getModel();
        model.setRowCount(0);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT id, Subscriber, Service, Reading, Unit, Consumption, Date, Type, Status " +
                "FROM meter WHERE 1=1"
            );
            
            if (!"All Status".equals(status)) {
                sql.append(" AND Status = ?");
            }
            
            if (!"All Services".equals(service)) {
                sql.append(" AND Service = ?");
            }
            
            if (!search.isEmpty()) {
                sql.append(" AND (Subscriber LIKE ? OR Service LIKE ?)");
            }
            
            sql.append(" ORDER BY Date DESC");
            
            stmt = conn.prepareStatement(sql.toString());
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
            }
            
            rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            int count = 0;
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String subscriber = rs.getString("Subscriber");
                String serviceName = rs.getString("Service");
                double reading = rs.getDouble("Reading");
                String unit = rs.getString("Unit");
                int consumption = rs.getInt("Consumption");
                Timestamp date = rs.getTimestamp("Date");
                String type = rs.getString("Type");
                String statusValue = rs.getString("Status");
                
                String dateStr = (date != null) ? dateFormat.format(date) : "N/A";
                String consumptionStr = (consumption > 0) ? String.valueOf(consumption) : "N/A";
                
                model.addRow(new Object[]{
                    id, subscriber, serviceName, reading, unit, consumptionStr, dateStr, type, statusValue
                });
                count++;
            }
            
            System.out.println("🔍 Found " + count + " meter readings matching filters");
            
        } catch (SQLException ex) {
            System.err.println("❌ Error applying filters: " + ex.getMessage());
            showErrorDialog("Filter Error", "Error applying filters: " + ex.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    private void clearFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        serviceFilter.setSelectedIndex(0);
        loadMeterReadings();
    }
    
    private void verifySelectedReading() {
        int selectedRow = meterTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a meter reading to verify.");
            return;
        }
        
        int modelRow = meterTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) meterTable.getModel();
        
        int meterId = (Integer) model.getValueAt(modelRow, 0);
        String subscriber = (String) model.getValueAt(modelRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Verify meter reading for subscriber: " + subscriber + "?",
            "Verify Reading",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            verifyMeterReading(meterId);
        }
    }
    
    private void verifyMeterReading(int meterId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "UPDATE meter SET Status = 'Verified' WHERE id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, meterId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Meter reading verified successfully!");
                loadMeterReadings();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error verifying meter reading: " + e.getMessage());
            showErrorDialog("Verification Error", "Error verifying meter reading: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private void viewMeterDetails() {
        int selectedRow = meterTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a meter reading to view details.");
            return;
        }
        
        int modelRow = meterTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) meterTable.getModel();
        
        int meterId = (Integer) model.getValueAt(modelRow, 0);
        showMeterDetailsDialog(meterId);
    }
    
    private void showMeterDetailsDialog(int meterId) {
        // Implementation for showing detailed meter reading information
        JOptionPane.showMessageDialog(this,
            "Meter Reading Details for ID: " + meterId + "\n\n" +
            "This would show comprehensive details including:\n" +
            "• Subscriber information\n" +
            "• Previous readings\n" +
            "• Consumption history\n" +
            "• Billing calculations",
            "Meter Reading Details",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void editMeterReading() {
        int selectedRow = meterTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a meter reading to edit.");
            return;
        }
        
        int modelRow = meterTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) meterTable.getModel();
        
        int meterId = (Integer) model.getValueAt(modelRow, 0);
        JOptionPane.showMessageDialog(this,
            "Edit functionality for meter reading ID: " + meterId + "\n\n" +
            "This would open an edit form to modify:\n" +
            "• Reading value\n" +
            "• Consumption\n" +
            "• Status\n" +
            "• Other details",
            "Edit Meter Reading",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deleteMeterReading() {
        int selectedRow = meterTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a meter reading to delete.");
            return;
        }
        
        int modelRow = meterTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) meterTable.getModel();
        
        int meterId = (Integer) model.getValueAt(modelRow, 0);
        String subscriber = (String) model.getValueAt(modelRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete meter reading for:\n" +
            "Subscriber: " + subscriber + "\n" +
            "Meter ID: " + meterId + "\n\n" +
            "This action cannot be undone.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            deleteMeterReading(meterId);
        }
    }
    
    private void deleteMeterReading(int meterId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "DELETE FROM meter WHERE id = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, meterId);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Meter reading deleted successfully!");
                loadMeterReadings();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting meter reading: " + e.getMessage());
            showErrorDialog("Delete Error", "Error deleting meter reading: " + e.getMessage());
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private void showBulkUpdateDialog() {
        JOptionPane.showMessageDialog(this,
            "Bulk Update Features:\n" +
            "• Update status for multiple readings\n" +
            "• Mass verification\n" +
            "• Import readings from file\n" +
            "• Generate consumption reports",
            "Bulk Update",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Database connection
    private Connection getConnection() throws SQLException {
        try {
            return DatabaseConnection.getConnection();
        } catch (Exception e) {
            // Fallback direct connection
            String url = "jdbc:mysql://localhost:3306/utilities_platform";
            String username = "root";
            String password = "newpassword";
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("MySQL JDBC Driver not found", ex);
            }
            
            return DriverManager.getConnection(url, username, password);
        }
    }
    
    // Utility methods
    private void closeResources(ResultSet rs, Statement stmt, Connection conn) {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Error closing database resources: " + e.getMessage());
        }
    }
    
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    // Custom cell renderers
    private class ReadingRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                setText(String.format("%.2f", (Double) value));
                setForeground(new Color(0, 100, 0));
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setHorizontalAlignment(SwingConstants.RIGHT);
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
                    case "Verified":
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
            
            setHorizontalAlignment(SwingConstants.CENTER);
            return this;
        }
    }
}
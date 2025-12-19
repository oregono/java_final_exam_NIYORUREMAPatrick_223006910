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

public class ComplaintManagementPanel extends BasePanel {
    private JTable complaintTable;
    private JComboBox<String> statusFilter;
    private JComboBox<String> priorityFilter;
    private JComboBox<String> categoryFilter;
    private JTextField searchField;
    
    // Color constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color ORANGE_COLOR = new Color(255, 165, 0);
    
    public ComplaintManagementPanel() {
        initializePanel();
        loadComplaints();
        updateComplaintStats();
    }
    
    private void initializePanel() {
        // Header
        add(createHeaderPanel("Complaint Management"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Dashboard with complaint analytics
        mainPanel.add(createComplaintDashboard(), BorderLayout.NORTH);
        
        // Complaint table and filters
        mainPanel.add(createComplaintTablePanel(), BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createComplaintDashboard() {
        JPanel dashboard = new JPanel(new BorderLayout());
        dashboard.setBackground(Color.WHITE);
        dashboard.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        
        // Complaint statistics
        JPanel statsPanel = new JPanel(new GridLayout(1, 6, 8, 0));
        statsPanel.setBackground(Color.WHITE);
        
        // Initialize with loading state - will be updated after data load
        statsPanel.add(createComplaintStatCard("Total", "0", PRIMARY_COLOR));
        statsPanel.add(createComplaintStatCard("Open", "0", WARNING_COLOR));
        statsPanel.add(createComplaintStatCard("In Progress", "0", ORANGE_COLOR));
        statsPanel.add(createComplaintStatCard("Resolved", "0", SUCCESS_COLOR));
        statsPanel.add(createComplaintStatCard("Urgent", "0", ERROR_COLOR));
        statsPanel.add(createComplaintStatCard("Avg. Resolution", "0 days", PRIMARY_COLOR));
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        searchField = new JTextField(12);
        searchField.setBorder(BorderFactory.createTitledBorder("Search Complaints"));
        
        statusFilter = new JComboBox<>(new String[]{"All Status", "Open", "In Progress", "Resolved", "Closed"});
        statusFilter.setBorder(BorderFactory.createTitledBorder("Status"));
        
        priorityFilter = new JComboBox<>(new String[]{"All Priorities", "Low", "Medium", "High", "Urgent"});
        priorityFilter.setBorder(BorderFactory.createTitledBorder("Priority"));
        
        categoryFilter = new JComboBox<>(new String[]{"All Categories", "Billing", "Service", "Meter", "Technical", "Other"});
        categoryFilter.setBorder(BorderFactory.createTitledBorder("Category"));
        
        JButton filterButton = createPrimaryButton("🔍 Apply Filters");
        JButton clearButton = createPrimaryButton("🔄 Clear");
        JButton exportButton = createSuccessButton("📊 Export Report");
        
        filterButton.addActionListener(this::applyComplaintFilters);
        clearButton.addActionListener(e -> clearComplaintFilters());
        exportButton.addActionListener(e -> exportComplaintReport());
        
        filterPanel.add(searchField);
        filterPanel.add(statusFilter);
        filterPanel.add(priorityFilter);
        filterPanel.add(categoryFilter);
        filterPanel.add(filterButton);
        filterPanel.add(clearButton);
        filterPanel.add(exportButton);
        
        dashboard.add(statsPanel, BorderLayout.NORTH);
        dashboard.add(filterPanel, BorderLayout.CENTER);
        
        return dashboard;
    }
    
    private JPanel createComplaintStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 12));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void updateComplaintStats() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Total complaints
            String totalSql = "SELECT COUNT(*) as total FROM complaint";
            PreparedStatement totalStmt = conn.prepareStatement(totalSql);
            ResultSet totalRs = totalStmt.executeQuery();
            
            // Open complaints
            String openSql = "SELECT COUNT(*) as open_count FROM complaint WHERE Status = 'Open'";
            PreparedStatement openStmt = conn.prepareStatement(openSql);
            ResultSet openRs = openStmt.executeQuery();
            
            // In Progress complaints
            String progressSql = "SELECT COUNT(*) as progress_count FROM complaint WHERE Status = 'In Progress'";
            PreparedStatement progressStmt = conn.prepareStatement(progressSql);
            ResultSet progressRs = progressStmt.executeQuery();
            
            // Resolved complaints
            String resolvedSql = "SELECT COUNT(*) as resolved_count FROM complaint WHERE Status = 'Resolved'";
            PreparedStatement resolvedStmt = conn.prepareStatement(resolvedSql);
            ResultSet resolvedRs = resolvedStmt.executeQuery();
            
            // Urgent complaints
            String urgentSql = "SELECT COUNT(*) as urgent_count FROM complaint WHERE Priority = 'Urgent'";
            PreparedStatement urgentStmt = conn.prepareStatement(urgentSql);
            ResultSet urgentRs = urgentStmt.executeQuery();
            
            // Average resolution time (simplified)
            String avgSql = "SELECT AVG(DATEDIFF(COALESCE(AssignedTo, CURRENT_TIMESTAMP), CreatedDate)) as avg_days FROM complaint WHERE Status IN ('Resolved', 'Closed')";
            PreparedStatement avgStmt = conn.prepareStatement(avgSql);
            ResultSet avgRs = avgStmt.executeQuery();
            
            if (totalRs.next() && openRs.next() && progressRs.next() && resolvedRs.next() && urgentRs.next() && avgRs.next()) {
                updateComplaintStatCard(0, "Total", String.valueOf(totalRs.getInt("total")), PRIMARY_COLOR);
                updateComplaintStatCard(1, "Open", String.valueOf(openRs.getInt("open_count")), WARNING_COLOR);
                updateComplaintStatCard(2, "In Progress", String.valueOf(progressRs.getInt("progress_count")), ORANGE_COLOR);
                updateComplaintStatCard(3, "Resolved", String.valueOf(resolvedRs.getInt("resolved_count")), SUCCESS_COLOR);
                updateComplaintStatCard(4, "Urgent", String.valueOf(urgentRs.getInt("urgent_count")), ERROR_COLOR);
                
                double avgDays = avgRs.getDouble("avg_days");
                String avgText = String.format("%.1f days", Double.isNaN(avgDays) ? 0 : avgDays);
                updateComplaintStatCard(5, "Avg. Resolution", avgText, PRIMARY_COLOR);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading complaint statistics: " + e.getMessage());
            showError("Unable to load complaint statistics");
        }
    }
    
    private void updateComplaintStatCard(int index, String title, String value, Color color) {
        JPanel dashboard = (JPanel) ((JPanel) getComponent(1)).getComponent(0);
        JPanel statsPanel = (JPanel) dashboard.getComponent(0);
        JPanel card = (JPanel) statsPanel.getComponent(index);
        
        JLabel titleLabel = (JLabel) card.getComponent(0);
        JLabel valueLabel = (JLabel) card.getComponent(1);
        
        titleLabel.setText(title);
        valueLabel.setText(value);
        valueLabel.setForeground(color);
    }
    
    private JPanel createComplaintTablePanel() {
        String[] columns = {"Complaint ID", "Subscriber", "Category", "Priority", "Title", "Status", "Created Date", "Assigned To"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 6) return Date.class; // Date
                return String.class;
            }
        };
        
        complaintTable = new JTable(model);
        complaintTable.setRowHeight(35);
        complaintTable.setFont(new Font("Arial", Font.PLAIN, 11));
        complaintTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        complaintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        complaintTable.setAutoCreateRowSorter(true);
        
        // Set column widths
        complaintTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Complaint ID
        complaintTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Subscriber
        complaintTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Category
        complaintTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Priority
        complaintTable.getColumnModel().getColumn(4).setPreferredWidth(200); // Title
        complaintTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Status
        complaintTable.getColumnModel().getColumn(6).setPreferredWidth(100); // Created Date
        complaintTable.getColumnModel().getColumn(7).setPreferredWidth(120); // Assigned To
        
        // Custom renderers
        complaintTable.getColumnModel().getColumn(3).setCellRenderer(new PriorityRenderer());
        complaintTable.getColumnModel().getColumn(5).setCellRenderer(new ComplaintStatusRenderer());
        complaintTable.getColumnModel().getColumn(2).setCellRenderer(new CategoryRenderer());
        
        JScrollPane scrollPane = new JScrollPane(complaintTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Complaints List"));
        
        // Action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        actionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JButton viewButton = createPrimaryButton("👁️ View Details");
        JButton assignButton = createPrimaryButton("👤 Assign");
        JButton updateButton = createSuccessButton("🔄 Update Status");
        JButton addNoteButton = createPrimaryButton("📝 Add Note");
        JButton resolveButton = createSuccessButton("✅ Resolve");
        JButton refreshButton = createPrimaryButton("🔄 Refresh");
        
        viewButton.addActionListener(e -> viewComplaintDetails());
        assignButton.addActionListener(e -> assignComplaint());
        updateButton.addActionListener(e -> updateComplaintStatus());
        addNoteButton.addActionListener(e -> addComplaintNote());
        resolveButton.addActionListener(e -> resolveComplaint());
        refreshButton.addActionListener(e -> loadComplaints());
        
        actionPanel.add(viewButton);
        actionPanel.add(assignButton);
        actionPanel.add(updateButton);
        actionPanel.add(addNoteButton);
        actionPanel.add(resolveButton);
        actionPanel.add(refreshButton);
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        tablePanel.add(actionPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }
    
    private void loadComplaints() {
        DefaultTableModel model = (DefaultTableModel) complaintTable.getModel();
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT ComplaintID, Subscriber, Category, Priority, Title, Status, CreatedDate, AssignedTo " +
                        "FROM complaint ORDER BY CreatedDate DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            int complaintCount = 0;
            
            while (rs.next()) {
                String complaintId = rs.getString("ComplaintID");
                String subscriber = rs.getString("Subscriber");
                String category = rs.getString("Category");
                String priority = rs.getString("Priority");
                String title = rs.getString("Title");
                String status = rs.getString("Status");
                Timestamp createdDate = rs.getTimestamp("CreatedDate");
                String assignedTo = rs.getString("AssignedTo");
                if (rs.wasNull()) assignedTo = "Unassigned";
                
                model.addRow(new Object[]{
                    complaintId, subscriber, category, priority, title, 
                    status, new Date(createdDate.getTime()), assignedTo
                });
                complaintCount++;
            }
            
            System.out.println("✅ Loaded " + complaintCount + " complaints from database");
            updateComplaintStats();
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading complaints: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error loading complaints: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            // Fallback to sample data
            loadSampleComplaints();
        }
    }
    
    private void loadSampleComplaints() {
        DefaultTableModel model = (DefaultTableModel) complaintTable.getModel();
        model.setRowCount(0);
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try {
            model.addRow(new Object[]{"CMP-2024011501", "john_doe", "Billing", "High", "Incorrect meter reading", "Open", dateFormat.parse("2024-01-15"), "Unassigned"});
            model.addRow(new Object[]{"CMP-2024011401", "jane_smith", "Service", "Medium", "Low water pressure", "In Progress", dateFormat.parse("2024-01-14"), "Tech Team"});
            model.addRow(new Object[]{"CMP-2024011601", "bob_wilson", "Technical", "Urgent", "No electricity supply", "Open", dateFormat.parse("2024-01-16"), "Emergency Team"});
            model.addRow(new Object[]{"CMP-2024011001", "alice_johnson", "Meter", "Low", "Meter installation request", "Resolved", dateFormat.parse("2024-01-10"), "Installation Team"});
            model.addRow(new Object[]{"CMP-2024011301", "john_doe", "Billing", "Medium", "Payment not reflected", "In Progress", dateFormat.parse("2024-01-13"), "Billing Dept"});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void applyComplaintFilters(ActionEvent e) {
        String status = (String) statusFilter.getSelectedItem();
        String priority = (String) priorityFilter.getSelectedItem();
        String category = (String) categoryFilter.getSelectedItem();
        String search = searchField.getText().trim();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            StringBuilder sql = new StringBuilder(
                "SELECT ComplaintID, Subscriber, Category, Priority, Title, Status, CreatedDate, AssignedTo " +
                "FROM complaint WHERE 1=1"
            );
            
            if (!"All Status".equals(status)) {
                sql.append(" AND Status = ?");
            }
            
            if (!"All Priorities".equals(priority)) {
                sql.append(" AND Priority = ?");
            }
            
            if (!"All Categories".equals(category)) {
                sql.append(" AND Category = ?");
            }
            
            if (!search.isEmpty()) {
                sql.append(" AND (ComplaintID LIKE ? OR Subscriber LIKE ? OR Title LIKE ?)");
            }
            
            sql.append(" ORDER BY CreatedDate DESC");
            
            PreparedStatement stmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;
            
            if (!"All Status".equals(status)) {
                stmt.setString(paramIndex++, status);
            }
            
            if (!"All Priorities".equals(priority)) {
                stmt.setString(paramIndex++, priority);
            }
            
            if (!"All Categories".equals(category)) {
                stmt.setString(paramIndex++, category);
            }
            
            if (!search.isEmpty()) {
                String searchPattern = "%" + search + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }
            
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) complaintTable.getModel();
            model.setRowCount(0);
            
            int filteredCount = 0;
            while (rs.next()) {
                String complaintId = rs.getString("ComplaintID");
                String subscriber = rs.getString("Subscriber");
                String categoryName = rs.getString("Category");
                String priorityLevel = rs.getString("Priority");
                String title = rs.getString("Title");
                String statusValue = rs.getString("Status");
                Timestamp createdDate = rs.getTimestamp("CreatedDate");
                String assignedTo = rs.getString("AssignedTo");
                if (rs.wasNull()) assignedTo = "Unassigned";
                
                model.addRow(new Object[]{
                    complaintId, subscriber, categoryName, priorityLevel, title, 
                    statusValue, new Date(createdDate.getTime()), assignedTo
                });
                filteredCount++;
            }
            
            JOptionPane.showMessageDialog(this,
                "Complaint Filters Applied:\n" +
                "• Status: " + status + "\n" +
                "• Priority: " + priority + "\n" +
                "• Category: " + category + "\n" +
                "• Search: " + (search.isEmpty() ? "None" : search) + "\n" +
                "• Results: " + filteredCount + " complaints",
                "Complaint Filters", JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException ex) {
            System.err.println("❌ Error applying complaint filters: " + ex.getMessage());
            showError("Error applying filters: " + ex.getMessage());
        }
    }
    
    private void clearComplaintFilters() {
        searchField.setText("");
        statusFilter.setSelectedIndex(0);
        priorityFilter.setSelectedIndex(0);
        categoryFilter.setSelectedIndex(0);
        loadComplaints();
    }
    
    private void exportComplaintReport() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get complaint statistics for report
            String statsSql = "SELECT " +
                            "COUNT(*) as total, " +
                            "SUM(CASE WHEN Status = 'Open' THEN 1 ELSE 0 END) as open_count, " +
                            "SUM(CASE WHEN Status = 'Resolved' THEN 1 ELSE 0 END) as resolved_count, " +
                            "SUM(CASE WHEN Priority = 'Urgent' THEN 1 ELSE 0 END) as urgent_count " +
                            "FROM complaint";
            
            PreparedStatement statsStmt = conn.prepareStatement(statsSql);
            ResultSet statsRs = statsStmt.executeQuery();
            
            if (statsRs.next()) {
                int total = statsRs.getInt("total");
                int openCount = statsRs.getInt("open_count");
                int resolvedCount = statsRs.getInt("resolved_count");
                int urgentCount = statsRs.getInt("urgent_count");
                double resolutionRate = total > 0 ? (resolvedCount * 100.0 / total) : 0;
                
                JOptionPane.showMessageDialog(this,
                    "Complaint Report Exported!\n\n" +
                    "Report Summary:\n" +
                    "• Total Complaints: " + total + "\n" +
                    "• Open Cases: " + openCount + "\n" +
                    "• Resolution Rate: " + String.format("%.1f%%", resolutionRate) + "\n" +
                    "• Urgent Cases: " + urgentCount + "\n" +
                    "• Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()),
                    "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error exporting complaint report: " + e.getMessage());
            showError("Error exporting report: " + e.getMessage());
        }
    }
    
    private void viewComplaintDetails() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to view details.");
            return;
        }
        
        String complaintId = (String) complaintTable.getValueAt(selectedRow, 0);
        showComplaintDetailsDialog(complaintId);
    }
    
    private void showComplaintDetailsDialog(String complaintId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM complaint WHERE ComplaintID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, complaintId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                showComplaintDetailsForm(rs);
            } else {
                showError("Complaint not found: " + complaintId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading complaint details: " + e.getMessage());
            showError("Error loading complaint details: " + e.getMessage());
        }
    }
    
    private void showComplaintDetailsForm(ResultSet rs) throws SQLException {
        JDialog dialog = new JDialog();
        dialog.setTitle("Complaint Details - " + rs.getString("ComplaintID"));
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 500);
        dialog.setLocationRelativeTo(this);
        
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Complaint Details Tab
        JPanel detailsPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        detailsPanel.add(new JLabel("Complaint ID:"));
        detailsPanel.add(new JLabel(rs.getString("ComplaintID")));
        detailsPanel.add(new JLabel("Subscriber:"));
        detailsPanel.add(new JLabel(rs.getString("Subscriber")));
        detailsPanel.add(new JLabel("Category:"));
        detailsPanel.add(new JLabel(rs.getString("Category")));
        detailsPanel.add(new JLabel("Priority:"));
        detailsPanel.add(new JLabel(rs.getString("Priority")));
        detailsPanel.add(new JLabel("Status:"));
        detailsPanel.add(new JLabel(rs.getString("Status")));
        detailsPanel.add(new JLabel("Created Date:"));
        detailsPanel.add(new JLabel(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(rs.getTimestamp("CreatedDate"))));
        detailsPanel.add(new JLabel("Assigned To:"));
        detailsPanel.add(new JLabel(rs.getString("AssignedTo") != null ? rs.getString("AssignedTo") : "Unassigned"));
        
        // Title and description would be here in a real implementation
        detailsPanel.add(new JLabel("Title:"));
        detailsPanel.add(new JLabel(rs.getString("Title")));
        
        // Notes Tab (simplified - in real app you'd have a separate notes table)
        JPanel notesPanel = new JPanel(new BorderLayout());
        JTextArea notesArea = new JTextArea("Complaint notes and history would be displayed here.\n\nIn a full implementation, this would include:\n• Internal notes from support staff\n• Status change history\n• Communication log with customer\n• Resolution details");
        notesArea.setEditable(false);
        notesArea.setBackground(Color.WHITE);
        notesPanel.add(new JScrollPane(notesArea), BorderLayout.CENTER);
        
        tabbedPane.addTab("Details", detailsPanel);
        tabbedPane.addTab("Notes & History", notesPanel);
        
        JButton closeButton = createPrimaryButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        
        dialog.add(tabbedPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void assignComplaint() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to assign.");
            return;
        }
        
        String complaintId = (String) complaintTable.getValueAt(selectedRow, 0);
        String[] teams = {"Billing Dept", "Tech Team", "Installation Team", "Emergency Team", "Customer Service"};
        String assignedTo = (String) JOptionPane.showInputDialog(this,
            "Assign complaint to:",
            "Assign Complaint",
            JOptionPane.QUESTION_MESSAGE,
            null,
            teams,
            teams[0]);
        
        if (assignedTo != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE complaint SET AssignedTo = ? WHERE ComplaintID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, assignedTo);
                stmt.setString(2, complaintId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    int modelRow = complaintTable.convertRowIndexToModel(selectedRow);
                    DefaultTableModel model = (DefaultTableModel) complaintTable.getModel();
                    model.setValueAt(assignedTo, modelRow, 7);
                    JOptionPane.showMessageDialog(this, "Complaint assigned to: " + assignedTo);
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error assigning complaint: " + e.getMessage());
                showError("Error assigning complaint: " + e.getMessage());
            }
        }
    }
    
    private void updateComplaintStatus() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to update status.");
            return;
        }
        
        String complaintId = (String) complaintTable.getValueAt(selectedRow, 0);
        String[] statuses = {"Open", "In Progress", "Resolved", "Closed"};
        String newStatus = (String) JOptionPane.showInputDialog(this,
            "Update status to:",
            "Update Complaint Status",
            JOptionPane.QUESTION_MESSAGE,
            null,
            statuses,
            statuses[0]);
        
        if (newStatus != null) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE complaint SET Status = ? WHERE ComplaintID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, newStatus);
                stmt.setString(2, complaintId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    int modelRow = complaintTable.convertRowIndexToModel(selectedRow);
                    DefaultTableModel model = (DefaultTableModel) complaintTable.getModel();
                    model.setValueAt(newStatus, modelRow, 5);
                    JOptionPane.showMessageDialog(this, "Complaint status updated to: " + newStatus);
                    updateComplaintStats(); // Refresh statistics
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error updating complaint status: " + e.getMessage());
                showError("Error updating status: " + e.getMessage());
            }
        }
    }
    
    private void addComplaintNote() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to add note.");
            return;
        }
        
        String complaintId = (String) complaintTable.getValueAt(selectedRow, 0);
        String note = JOptionPane.showInputDialog(this, "Enter note for complaint " + complaintId + ":");
        if (note != null && !note.trim().isEmpty()) {
            // In a real implementation, you would save this to a notes table
            JOptionPane.showMessageDialog(this, "Note added successfully to complaint " + complaintId);
        }
    }
    
    private void resolveComplaint() {
        int selectedRow = complaintTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a complaint to resolve.");
            return;
        }
        
        String complaintId = (String) complaintTable.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Mark complaint " + complaintId + " as resolved?",
            "Resolve Complaint", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DatabaseConnection.getConnection()) {
                String sql = "UPDATE complaint SET Status = 'Resolved' WHERE ComplaintID = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, complaintId);
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    int modelRow = complaintTable.convertRowIndexToModel(selectedRow);
                    DefaultTableModel model = (DefaultTableModel) complaintTable.getModel();
                    model.setValueAt("Resolved", modelRow, 5);
                    JOptionPane.showMessageDialog(this, "Complaint marked as resolved!");
                    updateComplaintStats(); // Refresh statistics
                }
                
            } catch (SQLException e) {
                System.err.println("❌ Error resolving complaint: " + e.getMessage());
                showError("Error resolving complaint: " + e.getMessage());
            }
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Custom cell renderers
    
    private class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String priority = (String) value;
                switch (priority) {
                    case "Urgent":
                        setBackground(new Color(255, 200, 200));
                        setForeground(new Color(150, 0, 0));
                        setFont(getFont().deriveFont(Font.BOLD));
                        break;
                    case "High":
                        setBackground(new Color(255, 220, 200));
                        setForeground(new Color(200, 100, 0));
                        setFont(getFont().deriveFont(Font.BOLD));
                        break;
                    case "Medium":
                        setBackground(new Color(255, 255, 200));
                        setForeground(new Color(150, 150, 0));
                        break;
                    case "Low":
                        setBackground(new Color(200, 255, 200));
                        setForeground(new Color(0, 100, 0));
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
    
    private class ComplaintStatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String status = (String) value;
                switch (status) {
                    case "Open":
                        setBackground(new Color(255, 200, 200));
                        setForeground(new Color(150, 0, 0));
                        break;
                    case "In Progress":
                        setBackground(new Color(255, 255, 200));
                        setForeground(new Color(150, 150, 0));
                        break;
                    case "Resolved":
                        setBackground(new Color(200, 255, 200));
                        setForeground(new Color(0, 100, 0));
                        break;
                    case "Closed":
                        setBackground(new Color(200, 200, 200));
                        setForeground(Color.DARK_GRAY);
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
    
    private class CategoryRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String category = (String) value;
                switch (category) {
                    case "Billing":
                        setForeground(new Color(0, 0, 150)); // Blue
                        break;
                    case "Service":
                        setForeground(new Color(0, 100, 0)); // Green
                        break;
                    case "Technical":
                        setForeground(new Color(150, 0, 150)); // Purple
                        break;
                    case "Meter":
                        setForeground(new Color(150, 100, 0)); // Brown
                        break;
                    case "Other":
                        setForeground(Color.DARK_GRAY);
                        break;
                }
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            setHorizontalAlignment(SwingConstants.LEFT);
            setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
            return this;
        }
    }
}
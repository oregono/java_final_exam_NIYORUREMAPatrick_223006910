package com.panels;

import com.models.User;
import com.dao.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileComplaintPanel - Professional complaint management interface
 * Provides users with a comprehensive system to file and track complaints
 */
public class FileComplaintPanel extends BasePanel {
    private User currentUser;
    private JComboBox<String> categoryCombo;
    private JComboBox<String> priorityCombo;
    private JComboBox<String> serviceCombo;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox urgentCheckbox;
    private JTable recentComplaintsTable;
    
    // Color constants for consistent styling
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    
    public FileComplaintPanel(User user) {
        this.currentUser = user;
        initializePanel();
        loadRecentComplaints();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("File a Complaint"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Complaint form and info
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            createComplaintFormPanel(), createComplaintInfoPanel());
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(0.7);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createComplaintFormPanel() {
        JPanel formPanel = createCardPanel("File New Complaint", createComplaintForm());
        return formPanel;
    }
    
    private JPanel createComplaintForm() {
        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        fieldsPanel.setBackground(Color.WHITE);
        
        categoryCombo = new JComboBox<>(new String[]{"Billing", "Service", "Meter", "Technical", "Other"});
        priorityCombo = new JComboBox<>(new String[]{"Low", "Medium", "High", "Urgent"});
        serviceCombo = new JComboBox<>(new String[]{"Water", "Electricity", "Gas", "Internet", "All Services"});
        titleField = new JTextField();
        urgentCheckbox = new JCheckBox("Mark as Urgent (24-hour response)");
        
        // Style components
        styleComboBox(categoryCombo);
        styleComboBox(priorityCombo);
        styleComboBox(serviceCombo);
        titleField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        fieldsPanel.add(new JLabel("Category:*"));
        fieldsPanel.add(categoryCombo);
        fieldsPanel.add(new JLabel("Priority:*"));
        fieldsPanel.add(priorityCombo);
        fieldsPanel.add(new JLabel("Service:*"));
        fieldsPanel.add(serviceCombo);
        fieldsPanel.add(new JLabel("Title:*"));
        fieldsPanel.add(titleField);
        fieldsPanel.add(new JLabel(""));
        fieldsPanel.add(urgentCheckbox);
        
        // Update priority when urgent checkbox is checked
        urgentCheckbox.addActionListener(e -> {
            if (urgentCheckbox.isSelected()) {
                priorityCombo.setSelectedItem("Urgent");
                priorityCombo.setEnabled(false);
            } else {
                priorityCombo.setEnabled(true);
            }
        });
        
        // Description area
        JPanel descriptionPanel = new JPanel(new BorderLayout());
        descriptionPanel.setBackground(Color.WHITE);
        descriptionPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel descLabel = new JLabel("Description:*");
        descLabel.setFont(new Font("Arial", Font.BOLD, 12));
        descriptionPanel.add(descLabel, BorderLayout.NORTH);
        
        descriptionArea = new JTextArea(6, 30);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        descriptionPanel.add(descriptionScroll, BorderLayout.CENTER);
        
        // Character counter
        JLabel charCountLabel = new JLabel("0/1000 characters");
        charCountLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        charCountLabel.setForeground(Color.GRAY);
        descriptionPanel.add(charCountLabel, BorderLayout.SOUTH);
        
        // Update character count
        descriptionArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
            
            private void updateCount() {
                SwingUtilities.invokeLater(() -> {
                    int length = descriptionArea.getText().length();
                    charCountLabel.setText(length + "/1000 characters");
                    if (length > 1000) {
                        charCountLabel.setForeground(ERROR_COLOR);
                        charCountLabel.setFont(charCountLabel.getFont().deriveFont(Font.BOLD));
                    } else if (length > 800) {
                        charCountLabel.setForeground(WARNING_COLOR);
                    } else {
                        charCountLabel.setForeground(Color.GRAY);
                        charCountLabel.setFont(charCountLabel.getFont().deriveFont(Font.PLAIN));
                    }
                });
            }
        });
        
        // Submit button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        JButton submitButton = createSuccessButton("📢 Submit Complaint");
        JButton clearButton = createPrimaryButton("🔄 Clear Form");
        
        submitButton.addActionListener(this::submitComplaint);
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);
        
        form.add(fieldsPanel, BorderLayout.NORTH);
        form.add(descriptionPanel, BorderLayout.CENTER);
        form.add(buttonPanel, BorderLayout.SOUTH);
        
        return form;
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
    }
    
    private JPanel createComplaintInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        // Complaint guidelines
        JPanel guidelinesPanel = createCardPanel("Complaint Guidelines", createGuidelines());
        
        // Contact information
        JPanel contactPanel = createCardPanel("Contact Support", createContactInfo());
        contactPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        // Recent complaints
        JPanel recentPanel = createCardPanel("My Recent Complaints", createRecentComplaints());
        recentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        
        infoPanel.add(guidelinesPanel, BorderLayout.NORTH);
        infoPanel.add(contactPanel, BorderLayout.CENTER);
        infoPanel.add(recentPanel, BorderLayout.SOUTH);
        
        return infoPanel;
    }
    
    private JPanel createGuidelines() {
        JPanel guidelines = new JPanel(new BorderLayout());
        guidelines.setBackground(Color.WHITE);
        
        JTextArea guidelinesText = new JTextArea(
            "Before submitting a complaint, please:\n\n" +
            "• Check our FAQ section for common issues\n" +
            "• Ensure your contact information is up to date\n" +
            "• Provide detailed description of the issue\n" +
            "• Include relevant bill numbers or references\n" +
            "• Attach supporting documents if available\n\n" +
            "Response Times:\n" +
            "• Urgent: Within 24 hours\n" +
            "• High: 1-2 business days\n" +
            "• Medium: 3-5 business days\n" +
            "• Low: 5-7 business days"
        );
        guidelinesText.setEditable(false);
        guidelinesText.setFont(new Font("Arial", Font.PLAIN, 11));
        guidelinesText.setBackground(new Color(248, 249, 250));
        guidelinesText.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        guidelinesText.setLineWrap(true);
        guidelinesText.setWrapStyleWord(true);
        
        guidelines.add(new JScrollPane(guidelinesText), BorderLayout.CENTER);
        return guidelines;
    }
    
    private JPanel createContactInfo() {
        JPanel contact = new JPanel(new GridLayout(4, 1, 5, 5));
        contact.setBackground(Color.WHITE);
        
        contact.add(createContactItem("📞 Phone", "1-800-UTILITY", PRIMARY_COLOR));
        contact.add(createContactItem("📧 Email", "support@utilities.com", PRIMARY_COLOR));
        contact.add(createContactItem("💬 Live Chat", "Available 24/7", SUCCESS_COLOR));
        contact.add(createContactItem("🏢 Office", "Mon-Fri 8AM-6PM", PRIMARY_COLOR));
        
        return contact;
    }
    
    private JPanel createContactItem(String method, String details, Color color) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        
        JLabel methodLabel = new JLabel(method);
        methodLabel.setFont(new Font("Arial", Font.BOLD, 11));
        methodLabel.setForeground(color);
        
        JLabel detailsLabel = new JLabel(details);
        detailsLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        
        item.add(methodLabel, BorderLayout.NORTH);
        item.add(detailsLabel, BorderLayout.SOUTH);
        
        return item;
    }
    
    private JScrollPane createRecentComplaints() {
        String[] columns = {"Complaint ID", "Date", "Category", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        recentComplaintsTable = new JTable(model);
        recentComplaintsTable.setRowHeight(30);
        recentComplaintsTable.setFont(new Font("Arial", Font.PLAIN, 11));
        recentComplaintsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        recentComplaintsTable.setEnabled(false);
        recentComplaintsTable.setShowGrid(true);
        recentComplaintsTable.setGridColor(new Color(240, 240, 240));
        
        // Status renderer
        recentComplaintsTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
                
                setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(recentComplaintsTable);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        return scrollPane;
    }
    
    private void loadRecentComplaints() {
        if (currentUser == null) return;
        if (recentComplaintsTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) recentComplaintsTable.getModel();
        model.setRowCount(0);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT ComplaintID, CreatedDate, Category, Status " +
                        "FROM complaint WHERE Subscriber = ? " +
                        "ORDER BY CreatedDate DESC LIMIT 5";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser.getUsername());
            rs = stmt.executeQuery();
            
            SimpleDateFormat displayFormat = new SimpleDateFormat("yyyy-MM-dd");
            
            while (rs.next()) {
                String complaintId = rs.getString("ComplaintID");
                Date createdDate = rs.getTimestamp("CreatedDate");
                String category = rs.getString("Category");
                String status = rs.getString("Status");
                
                model.addRow(new Object[]{
                    complaintId,
                    displayFormat.format(createdDate),
                    category,
                    status
                });
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading recent complaints: " + e.getMessage());
            loadSampleRecentComplaints();
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    private void loadSampleRecentComplaints() {
        if (recentComplaintsTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) recentComplaintsTable.getModel();
        model.setRowCount(0);
        
        model.addRow(new Object[]{"CMP-2024011501", "2024-01-15", "Billing", "Open"});
        model.addRow(new Object[]{"CMP-2024011001", "2024-01-10", "Service", "Resolved"});
        model.addRow(new Object[]{"CMP-2023122001", "2023-12-20", "Technical", "Closed"});
    }
    
    private void submitComplaint(ActionEvent e) {
        String category = (String) categoryCombo.getSelectedItem();
        String priority = (String) priorityCombo.getSelectedItem();
        String service = (String) serviceCombo.getSelectedItem();
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean urgent = urgentCheckbox.isSelected();
        
        // Validation
        if (title.isEmpty()) {
            showValidationError("Please enter a complaint title.", titleField);
            return;
        }
        
        if (description.isEmpty()) {
            showValidationError("Please describe your complaint.", descriptionArea);
            return;
        }
        
        if (description.length() > 1000) {
            showValidationError("Description must be 1000 characters or less.", descriptionArea);
            return;
        }
        
        // Generate complaint ID
        String complaintId = generateComplaintId();
        String summary = generateComplaintSummary(complaintId, category, priority, service, title, description, urgent);
        
        // Show confirmation dialog
        int confirm = JOptionPane.showConfirmDialog(this,
            createConfirmationPanel(summary),
            "Confirm Complaint Submission", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = saveComplaintToDatabase(complaintId, category, priority, service, title, description, urgent);
            
            if (success) {
                showSuccessMessage(complaintId, category, priority, urgent);
                clearForm();
                loadRecentComplaints();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to submit complaint. Please try again or contact support.",
                    "Submission Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String generateComplaintId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String datePart = dateFormat.format(new Date());
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT COUNT(*) as count FROM complaint WHERE ComplaintID LIKE ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "CMP-" + datePart + "%");
            rs = stmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt("count") + 1;
                return String.format("CMP-%s%02d", datePart, count);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error generating complaint ID: " + e.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
        
        // Fallback ID generation
        return "CMP-" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    }
    
    private boolean saveComplaintToDatabase(String complaintId, String category, String priority, 
                                          String service, String title, String description, boolean urgent) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "INSERT INTO complaint (ComplaintID, Subscriber, Title, Category, Status, Priority, CreatedDate) " +
                        "VALUES (?, ?, ?, ?, 'Open', ?, CURRENT_TIMESTAMP)";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, complaintId);
            stmt.setString(2, currentUser != null ? currentUser.getUsername() : "Unknown");
            stmt.setString(3, title);
            stmt.setString(4, category);
            stmt.setString(5, priority);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Complaint saved to database: " + complaintId);
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving complaint to database: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Database error: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            closeResources(null, stmt, conn);
        }
        
        return false;
    }
    
    private JPanel createConfirmationPanel(String summary) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextArea summaryArea = new JTextArea(summary);
        summaryArea.setEditable(false);
        summaryArea.setBackground(new Color(248, 249, 250));
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        summaryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel warningLabel = new JLabel("⚠️ Please review your complaint before submitting:");
        warningLabel.setFont(new Font("Arial", Font.BOLD, 12));
        warningLabel.setForeground(WARNING_COLOR);
        
        panel.add(warningLabel, BorderLayout.NORTH);
        panel.add(new JScrollPane(summaryArea), BorderLayout.CENTER);
        
        return panel;
    }
    
    private void showSuccessMessage(String complaintId, String category, String priority, boolean urgent) {
        String responseTime = getExpectedResponse(priority, urgent);
        
        JOptionPane.showMessageDialog(this,
            "<html><div style='width: 300px;'>" +
            "<h3>Complaint Submitted Successfully!</h3>" +
            "<p><b>Complaint ID:</b> " + complaintId + "</p>" +
            "<p><b>Category:</b> " + category + "</p>" +
            "<p><b>Priority:</b> " + priority + "</p>" +
            "<p><b>Expected Response:</b> " + responseTime + "</p>" +
            "<p>You will receive updates via email and can track the status in your complaints history.</p>" +
            "</div></html>",
            "Complaint Submitted", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private String generateComplaintSummary(String id, String category, String priority, String service, 
                                          String title, String description, boolean urgent) {
        return String.format(
            "COMPLAINT SUMMARY\n" +
            "================\n" +
            "ID: %s\n" +
            "Category: %s\n" +
            "Priority: %s%s\n" +
            "Service: %s\n" +
            "Title: %s\n" +
            "Description: %s\n" +
            "Date: %s\n" +
            "User: %s",
            id, category, priority, urgent ? " (URGENT)" : "", service, title,
            description.length() > 100 ? description.substring(0, 100) + "..." : description,
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
            currentUser != null ? currentUser.getUsername() : "Unknown"
        );
    }
    
    private String getExpectedResponse(String priority, boolean urgent) {
        if (urgent) return "Within 24 hours";
        switch (priority) {
            case "Urgent": return "Within 24 hours";
            case "High": return "1-2 business days";
            case "Medium": return "3-5 business days";
            case "Low": return "5-7 business days";
            default: return "3-5 business days";
        }
    }
    
    private void clearForm() {
        categoryCombo.setSelectedIndex(0);
        priorityCombo.setSelectedIndex(1); // Medium
        priorityCombo.setEnabled(true);
        serviceCombo.setSelectedIndex(0);
        titleField.setText("");
        descriptionArea.setText("");
        urgentCheckbox.setSelected(false);
        titleField.requestFocus();
    }
    
    // Database connection method
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
    
    private void showValidationError(String message, JComponent component) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        component.requestFocus();
        if (component instanceof JTextComponent) {
            ((JTextComponent) component).selectAll();
        }
    }
    
    private JPanel createCardPanel(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    // Button creation methods
    protected JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }
    
    protected JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR);
    }
    
    protected JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker()),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Add hover effects
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
}
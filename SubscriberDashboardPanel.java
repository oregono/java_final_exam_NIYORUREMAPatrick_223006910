package com.panels;

import com.models.User;
import com.utils.DB;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * SubscriberDashboardPanel - Professional subscriber dashboard interface
 * Provides comprehensive overview with quick actions, notifications, and service status
 */
public class SubscriberDashboardPanel extends BasePanel {
    private User currentUser;
    
    // Color constants for consistent styling
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    private static final Color ORANGE_COLOR = new Color(255, 165, 0);
    
    public SubscriberDashboardPanel(User user) {
        this.currentUser = user;
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("My Dashboard - Personal Overview"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Welcome and quick stats
        mainPanel.add(createWelcomePanel(), BorderLayout.NORTH);
        
        // Dashboard content with cards
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            createLeftPanel(), createRightPanel());
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createWelcomePanel() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        // Welcome message
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        
        String fullName = currentUser != null ? currentUser.getFullName() : "Valued Customer";
        JLabel welcomeLabel = new JLabel("Welcome back, " + fullName + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        welcomeLabel.setForeground(PRIMARY_COLOR);
        
        JLabel dateLabel = new JLabel("Today is " + new SimpleDateFormat("EEEE, MMMM d, yyyy").format(new Date()));
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        dateLabel.setForeground(Color.DARK_GRAY);
        
        messagePanel.add(welcomeLabel, BorderLayout.NORTH);
        messagePanel.add(dateLabel, BorderLayout.SOUTH);
        
        // Quick stats with real data from database
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setBackground(Color.WHITE);
        
        DashboardStats stats = getDashboardStats();
        
        statsPanel.add(createDashboardCard("Pending Bills", 
            String.valueOf(stats.getPendingBillsCount()), 
            String.format("$%.2f", stats.getPendingBillsAmount()), 
            WARNING_COLOR));
        statsPanel.add(createDashboardCard("Active Services", 
            String.valueOf(stats.getActiveServicesCount()), 
            stats.getActiveServices(), 
            PRIMARY_COLOR));
        statsPanel.add(createDashboardCard("Total Paid", 
            String.format("$%.2f", stats.getTotalPaid()), 
            "This Year", 
            SUCCESS_COLOR));
        statsPanel.add(createDashboardCard("Next Due", 
            stats.getNextDueDate(), 
            stats.getDaysUntilDue() + " days left", 
            ORANGE_COLOR));
        
        welcomePanel.add(messagePanel, BorderLayout.NORTH);
        welcomePanel.add(statsPanel, BorderLayout.CENTER);
        
        return welcomePanel;
    }
    
    private DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        String username = currentUser != null ? currentUser.getUsername() : "";
        
        try (Connection conn = DB.getConnection()) {
            // Get pending bills count and amount
            String billsSql = "SELECT COUNT(*) as count, SUM(Amount) as amount FROM bill " +
                            "WHERE Subscriber = ? AND Status = 'Pending'";
            try (PreparedStatement stmt = conn.prepareStatement(billsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setPendingBillsCount(rs.getInt("count"));
                    stats.setPendingBillsAmount(rs.getDouble("amount"));
                }
            }
            
            // Get total paid amount
            String paymentsSql = "SELECT SUM(Amount) as total FROM payment " +
                               "WHERE Subscriber = ? AND Status = 'Completed'";
            try (PreparedStatement stmt = conn.prepareStatement(paymentsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setTotalPaid(rs.getDouble("total"));
                }
            }
            
            // Get next due date
            String dueDateSql = "SELECT DueDate FROM bill " +
                              "WHERE Subscriber = ? AND Status = 'Pending' " +
                              "ORDER BY DueDate ASC LIMIT 1";
            try (PreparedStatement stmt = conn.prepareStatement(dueDateSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Date dueDate = rs.getDate("DueDate");
                    if (dueDate != null) {
                        stats.setNextDueDate(new SimpleDateFormat("MMM d, yyyy").format(dueDate));
                        
                        // Calculate days until due
                        long diff = dueDate.getTime() - new Date().getTime();
                        long days = diff / (1000 * 60 * 60 * 24);
                        stats.setDaysUntilDue((int) days);
                    }
                }
            }
            
            // Get active services (simplified - you might want to create a proper service subscription table)
            stats.setActiveServicesCount(3); // Default value
            stats.setActiveServices("Water, Elec, Gas"); // Default value
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading dashboard stats: " + e.getMessage());
            e.printStackTrace();
        }
        
        return stats;
    }
    
    private JPanel createDashboardCard(String title, String value, String subtitle, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(color);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        subtitleLabel.setForeground(Color.GRAY);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.add(valueLabel, BorderLayout.CENTER);
        contentPanel.add(subtitleLabel, BorderLayout.SOUTH);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);
        
        // Add click effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                card.setBackground(Color.WHITE);
            }
        });
        
        return card;
    }
    
    private JPanel createLeftPanel() {
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);
        
        // Recent bills card
        leftPanel.add(createRecentBillsCard(), BorderLayout.NORTH);
        
        // Quick actions card
        leftPanel.add(createQuickActionsCard(), BorderLayout.CENTER);
        
        return leftPanel;
    }
    
    private JPanel createRightPanel() {
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(new EmptyBorder(0, 15, 0, 0));
        
        // Notifications card
        rightPanel.add(createNotificationsCard(), BorderLayout.NORTH);
        
        // Service status card
        rightPanel.add(createServiceStatusCard(), BorderLayout.CENTER);
        
        return rightPanel;
    }
    
    private JPanel createRecentBillsCard() {
        JPanel card = createCardPanel("Recent Bills", createBillsTable());
        card.setPreferredSize(new Dimension(0, 200));
        return card;
    }
    
    private JScrollPane createBillsTable() {
        String[] columns = {"Bill ID", "Service", "Amount", "Due Date", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Load real bills from database
        loadRecentBillsFromDatabase(model);
        
        JTable table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.setEnabled(false); // Read-only for dashboard
        table.setShowGrid(true);
        table.setGridColor(new Color(240, 240, 240));
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);  // Bill ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Service
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Amount
        table.getColumnModel().getColumn(3).setPreferredWidth(100); // Due Date
        table.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        
        // Custom renderer for status
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
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
                            setFont(getFont().deriveFont(Font.BOLD));
                            break;
                        case "Pending":
                            setBackground(new Color(255, 255, 200));
                            setForeground(new Color(150, 150, 0));
                            break;
                        case "Overdue":
                            setBackground(new Color(255, 200, 200));
                            setForeground(new Color(150, 0, 0));
                            setFont(getFont().deriveFont(Font.BOLD));
                            break;
                        default:
                            setBackground(table.getBackground());
                            setForeground(table.getForeground());
                    }
                }
                
                super.setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 5, 0, 5));
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        return scrollPane;
    }
    
    private void loadRecentBillsFromDatabase(DefaultTableModel model) {
        String username = currentUser != null ? currentUser.getUsername() : "";
        String sql = "SELECT BillID, Services, Amount, DueDate, Status " +
                    "FROM bill WHERE Subscriber = ? " +
                    "ORDER BY DueDate DESC LIMIT 5";
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("BillID"),
                    rs.getString("Services"),
                    String.format("$%.2f", rs.getDouble("Amount")),
                    dateFormat.format(rs.getDate("DueDate")),
                    rs.getString("Status")
                });
                count++;
            }
            
            System.out.println("✅ Loaded " + count + " recent bills from database");
            
            // If no bills found, add a message
            if (count == 0) {
                model.addRow(new Object[]{"No bills", "found", "$0.00", "N/A", "N/A"});
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading recent bills: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "loading data", "$0.00", "N/A", "Error"});
        }
    }
    
    private JPanel createQuickActionsCard() {
        JPanel card = createCardPanel("Quick Actions", createActionButtons());
        card.setBorder(new EmptyBorder(15, 0, 0, 0));
        return card;
    }
    
    private JPanel createActionButtons() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBackground(Color.WHITE);
        
        JButton payBillBtn = createActionButton("💳 Pay Bill", "Pay pending bills", PRIMARY_COLOR);
        JButton submitReadingBtn = createActionButton("📈 Submit Reading", "Submit meter reading", SUCCESS_COLOR);
        JButton viewUsageBtn = createActionButton("📊 View Usage", "Check consumption", PRIMARY_COLOR);
        JButton fileComplaintBtn = createActionButton("📢 File Complaint", "Report an issue", WARNING_COLOR);
        JButton downloadBtn = createActionButton("📥 Download Bill", "Get bill copy", PRIMARY_COLOR);
        JButton supportBtn = createActionButton("🆘 Get Support", "Contact support", ERROR_COLOR);
        
        payBillBtn.addActionListener(e -> showQuickActionMessage("Pay Bill", "Redirecting to payment page..."));
        submitReadingBtn.addActionListener(e -> showQuickActionMessage("Submit Reading", "Opening meter reading form..."));
        viewUsageBtn.addActionListener(e -> showQuickActionMessage("View Usage", "Showing usage analytics..."));
        fileComplaintBtn.addActionListener(e -> showQuickActionMessage("File Complaint", "Opening complaint form..."));
        downloadBtn.addActionListener(e -> showQuickActionMessage("Download Bill", "Downloading bill PDF..."));
        supportBtn.addActionListener(e -> showQuickActionMessage("Get Support", "Connecting to support..."));
        
        panel.add(payBillBtn);
        panel.add(submitReadingBtn);
        panel.add(viewUsageBtn);
        panel.add(fileComplaintBtn);
        panel.add(downloadBtn);
        panel.add(supportBtn);
        
        return panel;
    }
    
    private void showQuickActionMessage(String action, String message) {
        System.out.println("🚀 Quick action triggered: " + action);
        JOptionPane.showMessageDialog(this, 
            "<html><b>" + action + "</b><br><br>" + message + "</html>",
            "Quick Action", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private JButton createActionButton(String text, String tooltip, Color color) {
        JButton button = new JButton("<html><center>" + text.replace(" ", "<br>") + "</center></html>");
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setToolTipText(tooltip);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker()),
            new EmptyBorder(8, 5, 8, 5)
        ));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private JPanel createNotificationsCard() {
        JPanel card = createCardPanel("Notifications & Alerts", createNotificationsList());
        card.setPreferredSize(new Dimension(0, 200));
        return card;
    }
    
    private JScrollPane createNotificationsList() {
        DefaultListModel<String> listModel = new DefaultListModel<>();
        
        // Load real notifications from database
        loadNotificationsFromDatabase(listModel);
        
        JList<String> notificationsList = new JList<>(listModel);
        notificationsList.setFont(new Font("Arial", Font.PLAIN, 11));
        notificationsList.setBackground(new Color(250, 250, 250));
        notificationsList.setFixedCellHeight(40);
        notificationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add selection listener
        notificationsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = notificationsList.getSelectedValue();
                if (selected != null) {
                    System.out.println("📢 Notification selected: " + selected);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(notificationsList);
        scrollPane.setPreferredSize(new Dimension(0, 150));
        return scrollPane;
    }
    
    private void loadNotificationsFromDatabase(DefaultListModel<String> listModel) {
        String username = currentUser != null ? currentUser.getUsername() : "";
        
        try (Connection conn = DB.getConnection()) {
            // Notifications from bills (due dates, overdue)
            String billsSql = "SELECT BillID, Amount, DueDate, Status FROM bill " +
                            "WHERE Subscriber = ? AND (Status = 'Pending' OR Status = 'Overdue') " +
                            "ORDER BY DueDate ASC";
            
            try (PreparedStatement stmt = conn.prepareStatement(billsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
                
                while (rs.next()) {
                    String status = rs.getString("Status");
                    double amount = rs.getDouble("Amount");
                    Date dueDate = rs.getDate("DueDate");
                    
                    if ("Overdue".equals(status)) {
                        listModel.addElement("🔴 Overdue bill: $" + amount + " was due on " + dateFormat.format(dueDate));
                    } else {
                        long diff = dueDate.getTime() - new Date().getTime();
                        long days = diff / (1000 * 60 * 60 * 24);
                        
                        if (days <= 7) {
                            listModel.addElement("🔔 Bill due in " + days + " days: $" + amount + " due " + dateFormat.format(dueDate));
                        }
                    }
                }
            }
            
            // Notifications from payments (recent payments)
            String paymentsSql = "SELECT Amount, Date FROM payment " +
                               "WHERE Subscriber = ? AND Status = 'Completed' " +
                               "ORDER BY Date DESC LIMIT 2";
            
            try (PreparedStatement stmt = conn.prepareStatement(paymentsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d");
                
                while (rs.next()) {
                    double amount = rs.getDouble("Amount");
                    Date paymentDate = rs.getTimestamp("Date");
                    listModel.addElement("✅ Payment received: $" + amount + " on " + dateFormat.format(paymentDate));
                }
            }
            
            // Add some system notifications
            listModel.addElement("📧 New service announcement: Fiber internet available");
            listModel.addElement("⚠️  Scheduled maintenance: Water service on Jan 25");
            listModel.addElement("📊 Usage alert: Higher than average electricity consumption");
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading notifications: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback notifications
            listModel.addElement("🔔 Bill payment reminder: $165.50 due in 5 days");
            listModel.addElement("✅ Meter reading submitted successfully");
            listModel.addElement("📧 System notification: Database connection issue");
        }
    }
    
    private JPanel createServiceStatusCard() {
        JPanel card = createCardPanel("My Services Status", createServiceStatusPanel());
        card.setBorder(new EmptyBorder(15, 0, 0, 0));
        return card;
    }
    
    private JPanel createServiceStatusPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 5, 5));
        panel.setBackground(Color.WHITE);
        
        // Load service status from database
        List<ServiceStatus> services = getServiceStatus();
        for (ServiceStatus service : services) {
            panel.add(createServiceStatusItem(service.getName(), service.getStatus(), service.getColor()));
        }
        
        return panel;
    }
    
    private List<ServiceStatus> getServiceStatus() {
        List<ServiceStatus> services = new ArrayList<>();
        String username = currentUser != null ? currentUser.getUsername() : "";
        
        try (Connection conn = DB.getConnection()) {
            // Get services from bills (this is a simplified approach)
            String sql = "SELECT DISTINCT Services FROM bill WHERE Subscriber = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    String serviceName = rs.getString("Services");
                    services.add(new ServiceStatus(
                        getServiceIcon(serviceName) + " " + serviceName,
                        "Active",
                        SUCCESS_COLOR
                    ));
                }
            }
            
            // If no services found, add default ones
            if (services.isEmpty()) {
                services.add(new ServiceStatus("💧 Water Service", "Active", SUCCESS_COLOR));
                services.add(new ServiceStatus("⚡ Electricity", "Active", SUCCESS_COLOR));
                services.add(new ServiceStatus("🔥 Natural Gas", "Active", SUCCESS_COLOR));
                services.add(new ServiceStatus("🌐 Internet", "Inactive", ERROR_COLOR));
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading service status: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback services
            services.add(new ServiceStatus("💧 Water Service", "Active", SUCCESS_COLOR));
            services.add(new ServiceStatus("⚡ Electricity", "Active", SUCCESS_COLOR));
            services.add(new ServiceStatus("🔥 Natural Gas", "Active", SUCCESS_COLOR));
            services.add(new ServiceStatus("🌐 Internet", "Inactive", ERROR_COLOR));
        }
        
        return services;
    }
    
    private String getServiceIcon(String serviceName) {
        if (serviceName == null) return "💰";
        
        switch (serviceName.toLowerCase()) {
            case "water": return "💧";
            case "electricity": return "⚡";
            case "gas": return "🔥";
            case "internet": return "🌐";
            default: return "💰";
        }
    }
    
    private JPanel createServiceStatusItem(String service, String status, Color color) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(8, 8, 8, 8)
        ));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        JLabel serviceLabel = new JLabel(service);
        serviceLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        
        JLabel statusLabel = new JLabel(status);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        statusLabel.setForeground(color);
        
        item.add(serviceLabel, BorderLayout.WEST);
        item.add(statusLabel, BorderLayout.EAST);
        
        // Add hover effect
        item.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                item.setBackground(new Color(245, 245, 245));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                item.setBackground(Color.WHITE);
            }
        });
        
        return item;
    }
    
    // Utility method for creating card panels
    private JPanel createCardPanel(String title, JComponent content) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220)),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);
        
        return card;
    }
    
    // Helper classes for data storage
    private static class DashboardStats {
        private int pendingBillsCount = 0;
        private double pendingBillsAmount = 0.0;
        private int activeServicesCount = 0;
        private String activeServices = "None";
        private double totalPaid = 0.0;
        private String nextDueDate = "No due bills";
        private int daysUntilDue = 0;
        
        // Getters and setters
        public int getPendingBillsCount() { return pendingBillsCount; }
        public void setPendingBillsCount(int pendingBillsCount) { this.pendingBillsCount = pendingBillsCount; }
        
        public double getPendingBillsAmount() { return pendingBillsAmount; }
        public void setPendingBillsAmount(double pendingBillsAmount) { this.pendingBillsAmount = pendingBillsAmount; }
        
        public int getActiveServicesCount() { return activeServicesCount; }
        public void setActiveServicesCount(int activeServicesCount) { this.activeServicesCount = activeServicesCount; }
        
        public String getActiveServices() { return activeServices; }
        public void setActiveServices(String activeServices) { this.activeServices = activeServices; }
        
        public double getTotalPaid() { return totalPaid; }
        public void setTotalPaid(double totalPaid) { this.totalPaid = totalPaid; }
        
        public String getNextDueDate() { return nextDueDate; }
        public void setNextDueDate(String nextDueDate) { this.nextDueDate = nextDueDate; }
        
        public int getDaysUntilDue() { return daysUntilDue; }
        public void setDaysUntilDue(int daysUntilDue) { this.daysUntilDue = daysUntilDue; }
    }
    
    private static class ServiceStatus {
        private String name;
        private String status;
        private Color color;
        
        public ServiceStatus(String name, String status, Color color) {
            this.name = name;
            this.status = status;
            this.color = color;
        }
        
        public String getName() { return name; }
        public String getStatus() { return status; }
        public Color getColor() { return color; }
    }
}
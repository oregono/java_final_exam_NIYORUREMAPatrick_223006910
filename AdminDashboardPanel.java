package com.panels;

import com.models.User;
import com.dao.StatsDAO;
import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class AdminDashboardPanel extends JPanel {
    private User currentUser;
    private StatsDAO statsDAO;
    private Timer refreshTimer;
    
    // Professional Color Scheme matching utilities theme
    private final Color PRIMARY_BLUE = new Color(0, 102, 204);     // Your header blue
    private final Color ELECTRIC_BLUE = new Color(30, 144, 255);   // Electricity
    private final Color WATER_BLUE = new Color(65, 105, 225);      // Water
    private final Color GAS_ORANGE = new Color(255, 140, 0);       // Gas
    private final Color INTERNET_PURPLE = new Color(147, 112, 219); // Internet
    private final Color SUCCESS_GREEN = new Color(34, 139, 34);    // Success
    private final Color WARNING_ORANGE = new Color(255, 165, 0);   // Warning
    private final Color DANGER_RED = new Color(220, 53, 69);       // Danger/Attention
    private final Color MAINTENANCE_GRAY = new Color(128, 128, 128); // Maintenance
    
    private final Color BACKGROUND = Color.WHITE;
    private final Color CARD_WHITE = Color.WHITE;
    private final Color TEXT_DARK = new Color(51, 51, 51);
    private final Color TEXT_MEDIUM = new Color(102, 102, 102);
    private final Color BORDER_LIGHT = new Color(221, 221, 221);

    public AdminDashboardPanel(User user) {
        this.currentUser = user;
        this.statsDAO = new StatsDAO();
        initializePanel();
        startAutoRefresh();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(BACKGROUND);
        
        // Main content with padding
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add components
        mainPanel.add(createWelcomeSection(), BorderLayout.NORTH);
        mainPanel.add(createDashboardContent(), BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createWelcomeSection() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        welcomePanel.setBackground(BACKGROUND);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel welcomeLabel = new JLabel("Utilities Platform - Admin Dashboard");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setForeground(PRIMARY_BLUE);
        
        JLabel dateLabel = new JLabel(new java.util.Date().toString());
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        dateLabel.setForeground(TEXT_MEDIUM);
        
        welcomePanel.add(welcomeLabel, BorderLayout.WEST);
        welcomePanel.add(dateLabel, BorderLayout.EAST);
        
        return welcomePanel;
    }
    
    private JPanel createDashboardContent() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(BACKGROUND);
        
        contentPanel.add(createMetricsGrid(), BorderLayout.NORTH);
        contentPanel.add(createInfoPanel(), BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private JPanel createMetricsGrid() {
        JPanel metricsPanel = new JPanel(new GridLayout(2, 3, 15, 15));
        metricsPanel.setBackground(BACKGROUND);
        metricsPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        Map<String, Object> stats = statsDAO.getDashboardStats();
        
        try {
            // FIXED: Safe value extraction with null checking
            int totalSubscribers = getSafeInteger(stats, "totalSubscribers");
            double monthlyRevenue = getSafeDouble(stats, "monthlyRevenue");
            int pendingBills = getSafeInteger(stats, "pendingBills");
            int openComplaints = getSafeInteger(stats, "openComplaints");
            double paymentSuccessRate = getSafeDouble(stats, "paymentSuccessRate");
            int newSubscribersThisMonth = getSafeInteger(stats, "newSubscribersThisMonth");
            double subscriberGrowth = getSafeDouble(stats, "subscriberGrowth");
            double revenueGrowth = getSafeDouble(stats, "revenueGrowth");
            
            metricsPanel.add(createMetricCard(
                "👥 Total Subscribers", 
                formatNumber(totalSubscribers),
                "Registered Users",
                PRIMARY_BLUE,
                getSubscriberTrend(subscriberGrowth)
            ));
            
            metricsPanel.add(createMetricCard(
                "💰 Monthly Revenue", 
                "$" + formatCurrency(monthlyRevenue),
                "From Completed Payments",
                SUCCESS_GREEN,
                getRevenueTrend(revenueGrowth)
            ));
            
            metricsPanel.add(createMetricCard(
                "🧾 Pending Bills", 
                formatNumber(pendingBills),
                "Status: Pending",
                WARNING_ORANGE,
                getBillStatus(pendingBills)
            ));
            
            metricsPanel.add(createMetricCard(
                "📢 Active Complaints", 
                formatNumber(openComplaints),
                "Open & In Progress",
                DANGER_RED,
                getComplaintStatus(openComplaints)
            ));
            
            metricsPanel.add(createMetricCard(
                "💳 Payment Success", 
                String.format("%.0f%%", paymentSuccessRate),
                "Completed Payments",
                SUCCESS_GREEN,
                getPaymentPerformance(paymentSuccessRate)
            ));
            
            metricsPanel.add(createMetricCard(
                "📈 New Subscribers", 
                formatNumber(newSubscribersThisMonth),
                "This Month",
                ELECTRIC_BLUE,
                getMonthlyGrowth(newSubscribersThisMonth)
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error creating metrics grid: " + e.getMessage());
            // Create fallback cards with default values
            metricsPanel.add(createMetricCard(
                "👥 Total Subscribers", 
                "0",
                "Registered Users",
                PRIMARY_BLUE,
                "Loading..."
            ));
            
            metricsPanel.add(createMetricCard(
                "💰 Monthly Revenue", 
                "$0",
                "From Completed Payments",
                SUCCESS_GREEN,
                "Loading..."
            ));
            
            metricsPanel.add(createMetricCard(
                "🧾 Pending Bills", 
                "0",
                "Status: Pending",
                WARNING_ORANGE,
                "Loading..."
            ));
            
            metricsPanel.add(createMetricCard(
                "📢 Active Complaints", 
                "0",
                "Open & In Progress",
                DANGER_RED,
                "Loading..."
            ));
            
            metricsPanel.add(createMetricCard(
                "💳 Payment Success", 
                "0%",
                "Completed Payments",
                SUCCESS_GREEN,
                "Loading..."
            ));
            
            metricsPanel.add(createMetricCard(
                "📈 New Subscribers", 
                "0",
                "This Month",
                ELECTRIC_BLUE,
                "Loading..."
            ));
        }
        
        return metricsPanel;
    }
    
    // FIXED: Helper methods for safe value extraction
    private int getSafeInteger(Map<String, Object> stats, String key) {
        try {
            Object value = stats.get(key);
            if (value == null) {
                return 0;
            } else if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof String) {
                try {
                    return Integer.parseInt((String) value);
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            return 0;
        } catch (Exception e) {
            System.err.println("❌ Error getting integer for key '" + key + "': " + e.getMessage());
            return 0;
        }
    }
    
    private double getSafeDouble(Map<String, Object> stats, String key) {
        try {
            Object value = stats.get(key);
            if (value == null) {
                return 0.0;
            } else if (value instanceof Double) {
                return (Double) value;
            } else if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof String) {
                try {
                    return Double.parseDouble((String) value);
                } catch (NumberFormatException e) {
                    return 0.0;
                }
            }
            return 0.0;
        } catch (Exception e) {
            System.err.println("❌ Error getting double for key '" + key + "': " + e.getMessage());
            return 0.0;
        }
    }
    
    private String getSafeString(Map<String, Object> stats, String key) {
        try {
            Object value = stats.get(key);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    private JPanel createMetricCard(String title, String value, String subtitle, Color color, String trend) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, color),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        // Title with icon
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        titleLabel.setForeground(TEXT_DARK);
        
        // Value
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(color);
        
        // Subtitle and trend
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_WHITE);
        
        JLabel subtitleLabel = new JLabel(subtitle);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitleLabel.setForeground(TEXT_MEDIUM);
        
        JLabel trendLabel = new JLabel(trend);
        trendLabel.setFont(new Font("Arial", Font.BOLD, 11));
        trendLabel.setForeground(TEXT_MEDIUM);
        trendLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        
        bottomPanel.add(subtitleLabel, BorderLayout.WEST);
        bottomPanel.add(trendLabel, BorderLayout.EAST);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);
        
        return card;
    }
    
    private JPanel createInfoPanel() {
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        infoPanel.setBackground(BACKGROUND);
        
        infoPanel.add(createDatabaseOverviewPanel());
        infoPanel.add(createQuickStatsPanel());
        
        return infoPanel;
    }
    
    private JPanel createDatabaseOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("🗃️ Database Overview");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel dbPanel = new JPanel(new GridLayout(6, 1, 8, 8));
        dbPanel.setBackground(CARD_WHITE);
        
        Map<String, Object> stats = statsDAO.getDashboardStats();
        
        try {
            // FIXED: Safe value extraction
            int pendingBills = getSafeInteger(stats, "pendingBills");
            int openComplaints = getSafeInteger(stats, "openComplaints");
            double monthlyRevenue = getSafeDouble(stats, "monthlyRevenue");
            int totalSubscribers = getSafeInteger(stats, "totalSubscribers");
            
            dbPanel.add(createDBItem("📋 Bill Table", formatNumber(pendingBills) + " pending", PRIMARY_BLUE));
            dbPanel.add(createDBItem("📢 Complaint Table", formatNumber(openComplaints) + " active", DANGER_RED));
            dbPanel.add(createDBItem("📊 Meter Table", "Readings tracked", ELECTRIC_BLUE));
            dbPanel.add(createDBItem("💳 Payment Table", "$" + formatCurrency(monthlyRevenue) + " revenue", SUCCESS_GREEN));
            dbPanel.add(createDBItem("🔧 Service Table", "5 services", WARNING_ORANGE));
            dbPanel.add(createDBItem("👥 Subscriber Table", formatNumber(totalSubscribers) + " users", PRIMARY_BLUE));
            
        } catch (Exception e) {
            System.err.println("❌ Error creating database overview: " + e.getMessage());
            dbPanel.add(createDBItem("📋 Bill Table", "0 pending", PRIMARY_BLUE));
            dbPanel.add(createDBItem("📢 Complaint Table", "0 active", DANGER_RED));
            dbPanel.add(createDBItem("📊 Meter Table", "Readings tracked", ELECTRIC_BLUE));
            dbPanel.add(createDBItem("💳 Payment Table", "$0 revenue", SUCCESS_GREEN));
            dbPanel.add(createDBItem("🔧 Service Table", "5 services", WARNING_ORANGE));
            dbPanel.add(createDBItem("👥 Subscriber Table", "0 users", PRIMARY_BLUE));
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(dbPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createDBItem(String table, String info, Color color) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(CARD_WHITE);
        
        JLabel tableLabel = new JLabel(table);
        tableLabel.setFont(new Font("Arial", Font.BOLD, 13));
        tableLabel.setForeground(TEXT_DARK);
        
        JLabel infoLabel = new JLabel(info);
        infoLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        infoLabel.setForeground(color);
        
        item.add(tableLabel, BorderLayout.WEST);
        item.add(infoLabel, BorderLayout.EAST);
        
        return item;
    }
    
    private JPanel createQuickStatsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_LIGHT, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel("📈 Performance Metrics");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        JPanel statsPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        statsPanel.setBackground(CARD_WHITE);
        
        Map<String, Object> stats = statsDAO.getDashboardStats();
        
        try {
            // FIXED: Safe value extraction
            double subscriberGrowth = getSafeDouble(stats, "subscriberGrowth");
            double revenueGrowth = getSafeDouble(stats, "revenueGrowth");
            int newSubscribersThisMonth = getSafeInteger(stats, "newSubscribersThisMonth");
            double paymentSuccessRate = getSafeDouble(stats, "paymentSuccessRate");
            
            statsPanel.add(createStatItem("Subscriber Growth", 
                String.format("%.1f%%", subscriberGrowth), 
                subscriberGrowth >= 0 ? SUCCESS_GREEN : DANGER_RED));
            statsPanel.add(createStatItem("Revenue Growth", 
                String.format("%.1f%%", revenueGrowth), 
                revenueGrowth >= 0 ? SUCCESS_GREEN : DANGER_RED));
            statsPanel.add(createStatItem("New This Month", 
                formatNumber(newSubscribersThisMonth) + " users", 
                PRIMARY_BLUE));
            statsPanel.add(createStatItem("Payment Success", 
                String.format("%.0f%% rate", paymentSuccessRate), 
                SUCCESS_GREEN));
                
        } catch (Exception e) {
            System.err.println("❌ Error creating quick stats: " + e.getMessage());
            statsPanel.add(createStatItem("Subscriber Growth", "0.0%", SUCCESS_GREEN));
            statsPanel.add(createStatItem("Revenue Growth", "0.0%", SUCCESS_GREEN));
            statsPanel.add(createStatItem("New This Month", "0 users", PRIMARY_BLUE));
            statsPanel.add(createStatItem("Payment Success", "100% rate", SUCCESS_GREEN));
        }
        
        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createStatItem(String label, String value, Color color) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(CARD_WHITE);
        
        JLabel labelLabel = new JLabel(label);
        labelLabel.setFont(new Font("Arial", Font.PLAIN, 13));
        labelLabel.setForeground(TEXT_DARK);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 13));
        valueLabel.setForeground(color);
        
        item.add(labelLabel, BorderLayout.WEST);
        item.add(valueLabel, BorderLayout.EAST);
        
        return item;
    }
    
    // Trend and status methods
    private String getSubscriberTrend(double growth) {
        if (Double.isNaN(growth)) return "No Data";
        if (growth > 10) return "Rapid Growth ↗";
        if (growth > 0) return "Growing ↗";
        if (growth < 0) return "Declining ↘";
        return "Stable →";
    }
    
    private String getRevenueTrend(double growth) {
        if (Double.isNaN(growth)) return "No Data";
        if (growth > 15) return "Excellent ↗";
        if (growth > 5) return "Good ↗";
        if (growth > 0) return "Growing ↗";
        return "Needs Attention";
    }
    
    private String getBillStatus(int pendingBills) {
        if (pendingBills == 0) return "All Clear ✓";
        if (pendingBills < 10) return "Manageable";
        if (pendingBills < 25) return "Attention Needed";
        return "High Priority!";
    }
    
    private String getComplaintStatus(int openComplaints) {
        if (openComplaints == 0) return "No Issues ✓";
        if (openComplaints < 5) return "Manageable";
        if (openComplaints < 15) return "Attention Needed";
        return "Critical!";
    }
    
    private String getPaymentPerformance(double rate) {
        if (Double.isNaN(rate)) return "No Data";
        if (rate > 95) return "Excellent ⭐";
        if (rate > 90) return "Good ✓";
        if (rate > 85) return "Adequate →";
        return "Needs Review";
    }
    
    private String getMonthlyGrowth(int newSubs) {
        if (newSubs > 20) return "Excellent 📈";
        if (newSubs > 10) return "Good 📈";
        if (newSubs > 5) return "Steady →";
        return "Slow ↘";
    }
    
    // Utility methods
    private String formatNumber(int number) {
        if (number >= 1000000) return String.format("%.1fM", number / 1000000.0);
        if (number >= 1000) return String.format("%.1fK", number / 1000.0);
        return String.valueOf(number);
    }
    
    private String formatCurrency(double amount) {
        if (Double.isNaN(amount)) return "$0";
        if (amount >= 1000000) return String.format("$%.1fM", amount / 1000000.0);
        if (amount >= 1000) return String.format("$%.1fK", amount / 1000.0);
        return String.format("$%.0f", amount);
    }
    
    private void startAutoRefresh() {
        refreshTimer = new Timer(30000, e -> refreshDashboard());
        refreshTimer.start();
    }
    
    private void refreshDashboard() {
        SwingUtilities.invokeLater(() -> {
            removeAll();
            initializePanel();
            revalidate();
            repaint();
        });
    }
}
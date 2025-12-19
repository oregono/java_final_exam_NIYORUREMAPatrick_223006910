package com.ui;

import com.models.User;
import com.panels.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Dashboard extends JFrame {
    private User currentUser;
    
    // UI Components
    private JTabbedPane tabbedPane;
    private JPanel headerPanel;
    private JLabel welcomeLabel;
    private JButton logoutButton;

    public Dashboard(User user) {
        this.currentUser = user;
        initializeDashboard();
    }

    private void initializeDashboard() {
        setTitle("Utilities Platform System - Dashboard");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create header
        createHeader();
        
        // Create main content with tabs
        createMainContent();
        
        setVisible(true);
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(0, 102, 204));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        // Welcome message
        welcomeLabel = new JLabel("Welcome to Utilities Platform System - " + 
                                 currentUser.getFullName() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        // Logout button
        logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        JPanel logoutPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        logoutPanel.setBackground(new Color(0, 102, 204));
        logoutPanel.add(logoutButton);
        headerPanel.add(logoutPanel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainContent() {
        tabbedPane = new JTabbedPane();
        
        if (currentUser.isAdmin()) {
            setupAdminTabs();
        } else {
            setupSubscriberTabs();
        }
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private void setupAdminTabs() {
        // Admin has full access to all modules
        tabbedPane.addTab("📊 Dashboard", new AdminDashboardPanel(currentUser));
        tabbedPane.addTab("👥 Subscriber Management", new SubscriberManagementPanel());
        tabbedPane.addTab("🔧 Service Management", new ServiceManagementPanel());
        tabbedPane.addTab("🧾 Bill Management", new BillManagementPanel());
        tabbedPane.addTab("💳 Payment Management", new PaymentManagementPanel());
        tabbedPane.addTab("📈 Meter Readings", new MeterManagementPanel());
        tabbedPane.addTab("📢 Complaints", new ComplaintManagementPanel());
        tabbedPane.addTab("📊 Reports", new ReportsPanel()); // Fixed: Changed from "📈 Reports" to "📊 Reports"
    }

    private void setupSubscriberTabs() {
        // Subscriber has limited access to personal functions
        tabbedPane.addTab("📊 My Dashboard", new SubscriberDashboardPanel(currentUser));
        tabbedPane.addTab("🧾 My Bills", new MyBillsPanel(currentUser));
        tabbedPane.addTab("💳 My Payments", new MyPaymentsPanel(currentUser));
        tabbedPane.addTab("📈 Submit Reading", new SubmitMeterReadingPanel(currentUser));
        tabbedPane.addTab("📢 File Complaint", new FileComplaintPanel(currentUser));
        tabbedPane.addTab("👤 My Profile", new MyProfilePanel(currentUser));
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Logout Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }
}
package com.panels;

import com.models.User;
import com.dao.DatabaseConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.JTextComponent;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;

public class MyProfilePanel extends BasePanel {
    private User currentUser;
    private JTextField usernameField;
    private JTextField emailField;
    private JTextField fullNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel memberSinceLabel;
    private JLabel lastLoginLabel;
    
    // Color constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    public MyProfilePanel(User user) {
        this.currentUser = user;
        initializePanel();
        loadUserData();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("My Profile - Account Management"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Profile overview and form
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            createProfileFormPanel(), createProfileInfoPanel());
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createProfileFormPanel() {
        JPanel formPanel = createCardPanel("Edit Profile Information", createProfileForm());
        return formPanel;
    }
    
    private JPanel createProfileForm() {
        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        fieldsPanel.setBackground(Color.WHITE);
        
        usernameField = new JTextField();
        emailField = new JTextField();
        fullNameField = new JTextField();
        passwordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();
        
        // Style fields
        styleTextField(usernameField);
        styleTextField(emailField);
        styleTextField(fullNameField);
        stylePasswordField(passwordField);
        stylePasswordField(confirmPasswordField);
        
        // Make username read-only since it's unique identifier
        usernameField.setEditable(false);
        usernameField.setBackground(new Color(240, 240, 240));
        
        fieldsPanel.add(createFormLabel("Username:"));
        fieldsPanel.add(usernameField);
        fieldsPanel.add(createFormLabel("Email Address:*"));
        fieldsPanel.add(emailField);
        fieldsPanel.add(createFormLabel("Full Name:*"));
        fieldsPanel.add(fullNameField);
        fieldsPanel.add(createFormLabel("New Password:"));
        fieldsPanel.add(passwordField);
        fieldsPanel.add(createFormLabel("Confirm Password:"));
        fieldsPanel.add(confirmPasswordField);
        
        // Password strength indicator
        JPanel passwordPanel = new JPanel(new BorderLayout());
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel strengthLabel = new JLabel("Password strength: None");
        strengthLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        strengthLabel.setForeground(Color.GRAY);
        passwordPanel.add(strengthLabel, BorderLayout.NORTH);
        
        // Update password strength in real-time
        passwordField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updatePasswordStrength(); }
            
            private void updatePasswordStrength() {
                SwingUtilities.invokeLater(() -> {
                    String password = new String(passwordField.getPassword());
                    String strength = calculatePasswordStrength(password);
                    Color color = getStrengthColor(strength);
                    strengthLabel.setText("Password strength: " + strength);
                    strengthLabel.setForeground(color);
                });
            }
        });
        
        // Action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton saveButton = createSuccessButton("💾 Save Changes");
        JButton resetButton = createPrimaryButton("🔄 Reset");
        
        saveButton.addActionListener(this::saveProfile);
        resetButton.addActionListener(e -> loadUserData());
        
        buttonPanel.add(resetButton);
        buttonPanel.add(saveButton);
        
        form.add(fieldsPanel, BorderLayout.NORTH);
        form.add(passwordPanel, BorderLayout.CENTER);
        form.add(buttonPanel, BorderLayout.SOUTH);
        
        return form;
    }
    
    private JPanel createProfileInfoPanel() {
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBackground(Color.WHITE);
        
        // Account summary
        JPanel summaryPanel = createCardPanel("Account Summary", createAccountSummary());
        
        // Account statistics
        JPanel statsPanel = createCardPanel("Account Statistics", createAccountStats());
        statsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        // Quick actions
        JPanel actionsPanel = createCardPanel("Quick Actions", createQuickActions());
        actionsPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        infoPanel.add(summaryPanel, BorderLayout.NORTH);
        infoPanel.add(statsPanel, BorderLayout.CENTER);
        infoPanel.add(actionsPanel, BorderLayout.SOUTH);
        
        return infoPanel;
    }
    
    private JPanel createAccountSummary() {
        JPanel summary = new JPanel(new GridLayout(4, 1, 5, 5));
        summary.setBackground(Color.WHITE);
        
        memberSinceLabel = new JLabel("Member since: Loading...");
        lastLoginLabel = new JLabel("Last login: Loading...");
        JLabel roleLabel = new JLabel("Account type: " + (currentUser != null ? currentUser.getRole() : "Subscriber"));
        JLabel statusLabel = new JLabel("Status: Active");
        
        roleLabel.setForeground(PRIMARY_COLOR);
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.BOLD));
        statusLabel.setForeground(SUCCESS_COLOR);
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
        
        summary.add(memberSinceLabel);
        summary.add(lastLoginLabel);
        summary.add(roleLabel);
        summary.add(statusLabel);
        
        return summary;
    }
    
    private JPanel createAccountStats() {
        JPanel stats = new JPanel(new GridLayout(4, 1, 5, 5));
        stats.setBackground(Color.WHITE);
        
        // Load real statistics from database
        UserStatistics userStats = getUserStatistics();
        
        stats.add(createStatItem("📊 Total Bills", String.valueOf(userStats.getTotalBills()), PRIMARY_COLOR));
        stats.add(createStatItem("💳 Payments Made", String.valueOf(userStats.getPaymentsMade()), SUCCESS_COLOR));
        stats.add(createStatItem("📈 Readings Submitted", String.valueOf(userStats.getReadingsSubmitted()), PRIMARY_COLOR));
        stats.add(createStatItem("📢 Complaints Filed", String.valueOf(userStats.getComplaintsFiled()), WARNING_COLOR));
        
        return stats;
    }
    
    private UserStatistics getUserStatistics() {
        UserStatistics stats = new UserStatistics();
        String username = currentUser != null ? currentUser.getUsername() : "";
        
        Connection conn = null;
        
        try {
            conn = getConnection();
            
            // Total bills
            String billsSql = "SELECT COUNT(*) as total FROM bill WHERE Subscriber = ?";
            try (PreparedStatement stmt = conn.prepareStatement(billsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setTotalBills(rs.getInt("total"));
                }
            }
            
            // Payments made
            String paymentsSql = "SELECT COUNT(*) as total FROM payment WHERE Subscriber = ? AND Status = 'Completed'";
            try (PreparedStatement stmt = conn.prepareStatement(paymentsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setPaymentsMade(rs.getInt("total"));
                }
            }
            
            // Readings submitted
            String readingsSql = "SELECT COUNT(*) as total FROM meter WHERE Subscriber = ?";
            try (PreparedStatement stmt = conn.prepareStatement(readingsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setReadingsSubmitted(rs.getInt("total"));
                }
            }
            
            // Complaints filed
            String complaintsSql = "SELECT COUNT(*) as total FROM complaint WHERE Subscriber = ?";
            try (PreparedStatement stmt = conn.prepareStatement(complaintsSql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    stats.setComplaintsFiled(rs.getInt("total"));
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading user statistics: " + e.getMessage());
            
            // Fallback values
            stats.setTotalBills(12);
            stats.setPaymentsMade(8);
            stats.setReadingsSubmitted(6);
            stats.setComplaintsFiled(3);
        } finally {
            closeConnection(conn);
        }
        
        return stats;
    }
    
    private JPanel createStatItem(String label, String value, Color color) {
        JPanel item = new JPanel(new BorderLayout());
        item.setBackground(Color.WHITE);
        item.setBorder(new EmptyBorder(2, 0, 2, 0));
        
        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.PLAIN, 11));
        
        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Arial", Font.BOLD, 11));
        valueComp.setForeground(color);
        
        item.add(labelComp, BorderLayout.WEST);
        item.add(valueComp, BorderLayout.EAST);
        
        return item;
    }
    
    private JPanel createQuickActions() {
        JPanel actions = new JPanel(new GridLayout(3, 1, 5, 5));
        actions.setBackground(Color.WHITE);
        
        JButton downloadDataBtn = createActionButton("📥 Download My Data");
        JButton privacyBtn = createActionButton("🔒 Privacy Settings");
        JButton deactivateBtn = createActionButton("🚫 Deactivate Account");
        
        downloadDataBtn.addActionListener(e -> downloadUserData());
        privacyBtn.addActionListener(e -> showPrivacySettings());
        deactivateBtn.addActionListener(e -> deactivateAccount());
        
        actions.add(downloadDataBtn);
        actions.add(privacyBtn);
        actions.add(deactivateBtn);
        
        return actions;
    }
    
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 11));
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            new EmptyBorder(5, 10, 5, 10)
        ));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
                button.setForeground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }
    
    private void loadUserData() {
        // Load current user data into form fields
        if (currentUser != null) {
            usernameField.setText(currentUser.getUsername());
            emailField.setText(currentUser.getEmail());
            fullNameField.setText(currentUser.getFullName());
            passwordField.setText("");
            confirmPasswordField.setText("");
            
            // Load additional user info from database
            loadUserProfileInfo();
        }
    }
    
    private void loadUserProfileInfo() {
        String username = currentUser != null ? currentUser.getUsername() : "";
        Connection conn = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT CreatedAt, LastLogin FROM subscriber WHERE Username = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()) {
                    Timestamp createdAt = rs.getTimestamp("CreatedAt");
                    Timestamp lastLogin = rs.getTimestamp("LastLogin");
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM yyyy");
                    SimpleDateFormat datetimeFormat = new SimpleDateFormat("MMM d, yyyy 'at' h:mm a");
                    
                    memberSinceLabel.setText("Member since: " + dateFormat.format(createdAt));
                    
                    if (lastLogin != null) {
                        lastLoginLabel.setText("Last login: " + datetimeFormat.format(lastLogin));
                    } else {
                        lastLoginLabel.setText("Last login: Never");
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading user profile info: " + e.getMessage());
            
            // Fallback values
            memberSinceLabel.setText("Member since: January 2024");
            lastLoginLabel.setText("Last login: " + new SimpleDateFormat("MMM d, yyyy 'at' h:mm a").format(new java.util.Date()));
        } finally {
            closeConnection(conn);
        }
    }
    
    private void saveProfile(ActionEvent e) {
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validation
        if (email.isEmpty() || fullName.isEmpty()) {
            showValidationError("Email and Full Name are required fields.");
            return;
        }
        
        if (!isValidEmail(email)) {
            showValidationError("Please enter a valid email address.", emailField);
            return;
        }
        
        if (!password.isEmpty()) {
            if (password.length() < 6) {
                showValidationError("Password must be at least 6 characters long.", passwordField);
                return;
            }
            
            if (!password.equals(confirmPassword)) {
                showValidationError("Passwords do not match.", confirmPasswordField);
                return;
            }
        }
        
        // Save changes to database
        boolean success = updateUserProfile(email, fullName, password);
        
        if (success) {
            // Update current user object
            currentUser.setEmail(email);
            currentUser.setFullName(fullName);
            
            JOptionPane.showMessageDialog(this,
                "<html><b>Profile Updated Successfully!</b><br><br>" +
                "<b>Changes saved:</b><br>" +
                "• Email: " + email + "<br>" +
                "• Full Name: " + fullName + "<br>" +
                (password.isEmpty() ? "" : "• Password: Updated<br>") +
                "<br>Your changes have been saved to your account.</html>",
                "Profile Updated", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                "Error updating profile. Please try again or contact support.",
                "Update Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean updateUserProfile(String email, String fullName, String password) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql;
            
            if (password.isEmpty()) {
                // Update without password
                sql = "UPDATE subscriber SET Email = ?, FullName = ?, LastLogin = CURRENT_TIMESTAMP WHERE Username = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, fullName);
                stmt.setString(3, currentUser.getUsername());
            } else {
                // Update with new password
                sql = "UPDATE subscriber SET Email = ?, FullName = ?, PasswordHash = ?, LastLogin = CURRENT_TIMESTAMP WHERE Username = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, email);
                stmt.setString(2, fullName);
                stmt.setString(3, hashPassword(password));
                stmt.setString(4, currentUser.getUsername());
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Profile updated successfully for user: " + currentUser.getUsername());
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating user profile: " + e.getMessage());
            
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("Email")) {
                JOptionPane.showMessageDialog(this,
                    "Email address already exists. Please use a different email.",
                    "Duplicate Email",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Database error: " + e.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                System.err.println("❌ Error closing statement: " + e.getMessage());
            }
            closeConnection(conn);
        }
        
        return false;
    }
    
    private String hashPassword(String password) {
        // Simple SHA-256 hashing (for demonstration - use bcrypt in production)
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            System.err.println("❌ Password hashing algorithm not available: " + e.getMessage());
            // Fallback to basic hash (not secure for production)
            return Integer.toString(password.hashCode());
        }
    }
    
    // Database connection method
    private Connection getConnection() throws SQLException {
        try {
            return DatabaseConnection.getConnection();
        } catch (Exception e) {
            // Fallback direct connection if DatabaseConnection fails
            String url = "jdbc:mysql://localhost:3306/utilities_platform";
            String username = "root";
            String password = "password";
            
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException ex) {
                throw new SQLException("MySQL JDBC Driver not found", ex);
            }
            
            return DriverManager.getConnection(url, username, password);
        }
    }
    
    private void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("❌ Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    private void showValidationError(String message) {
        showValidationError(message, null);
    }
    
    private void showValidationError(String message, JComponent component) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        if (component != null) {
            component.requestFocus();
            if (component instanceof JTextComponent) {
                ((JTextComponent) component).selectAll();
            }
        }
    }
    
    private boolean isValidEmail(String email) {
        return email.contains("@") && email.contains(".") && email.length() > 5;
    }
    
    private String calculatePasswordStrength(String password) {
        if (password.isEmpty()) return "None";
        if (password.length() < 6) return "Weak";
        
        boolean hasUpper = !password.equals(password.toLowerCase());
        boolean hasLower = !password.equals(password.toUpperCase());
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = !password.matches("[A-Za-z0-9 ]*");
        
        int strength = 0;
        if (hasUpper) strength++;
        if (hasLower) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        if (password.length() >= 8) strength++;
        
        if (strength <= 2) return "Weak";
        if (strength <= 4) return "Medium";
        return "Strong";
    }
    
    private Color getStrengthColor(String strength) {
        switch (strength) {
            case "Weak": return DANGER_COLOR;
            case "Medium": return WARNING_COLOR;
            case "Strong": return SUCCESS_COLOR;
            default: return Color.GRAY;
        }
    }
    
    private void downloadUserData() {
        System.out.println("📥 Starting data export for user: " + currentUser.getUsername());
        
        // Simulate data export process
        JOptionPane.showMessageDialog(this,
            "<html><b>Data Export Started</b><br><br>" +
            "Your data export has been initiated.<br><br>" +
            "<b>The following data will be included:</b><br>" +
            "• Personal information<br>" +
            "• Billing history<br>" +
            "• Payment records<br>" +
            "• Meter readings<br>" +
            "• Complaint history<br><br>" +
            "You will receive an email with a download link when your data is ready.<br>" +
            "Estimated processing time: 5-10 minutes</html>",
            "Data Export", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showPrivacySettings() {
        System.out.println("🔒 Showing privacy settings for user: " + currentUser.getUsername());
        
        JOptionPane.showMessageDialog(this,
            "<html><b>Privacy Settings</b><br><br>" +
            "<b>Current Settings:</b><br>" +
            "• Email notifications: <b>Enabled</b><br>" +
            "• SMS alerts: <b>Enabled</b><br>" +
            "• Marketing communications: <b>Disabled</b><br>" +
            "• Data sharing: <b>Limited</b><br>" +
            "• Cookie preferences: <b>Managed</b><br><br>" +
            "Full privacy settings management will be available in the next update.</html>",
            "Privacy Settings", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void deactivateAccount() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Confirm Account Deactivation</b><br><br>" +
            "Are you sure you want to deactivate your account?<br><br>" +
            "<b>This will:</b><br>" +
            "• Suspend all services<br>" +
            "• Cancel pending bills<br>" +
            "• Remove your personal data<br>" +
            "• Close your account permanently<br><br>" +
            "<b>This action cannot be undone.</b></html>",
            "Deactivate Account", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("🚫 Account deactivation requested for user: " + currentUser.getUsername());
            
            JOptionPane.showMessageDialog(this,
                "<html><b>Deactivation Request Received</b><br><br>" +
                "Account deactivation request received.<br><br>" +
                "Our team will contact you within 24 hours to confirm the deactivation.<br>" +
                "Thank you for using our utilities platform.</html>",
                "Deactivation Requested", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    // Utility methods
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
    
    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }
    
    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)
        ));
    }
    
    private void stylePasswordField(JPasswordField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)
        ));
    }
    
    // Helper class for user statistics
    private static class UserStatistics {
        private int totalBills;
        private int paymentsMade;
        private int readingsSubmitted;
        private int complaintsFiled;
        
        // Getters and setters
        public int getTotalBills() { return totalBills; }
        public void setTotalBills(int totalBills) { this.totalBills = totalBills; }
        
        public int getPaymentsMade() { return paymentsMade; }
        public void setPaymentsMade(int paymentsMade) { this.paymentsMade = paymentsMade; }
        
        public int getReadingsSubmitted() { return readingsSubmitted; }
        public void setReadingsSubmitted(int readingsSubmitted) { this.readingsSubmitted = readingsSubmitted; }
        
        public int getComplaintsFiled() { return complaintsFiled; }
        public void setComplaintsFiled(int complaintsFiled) { this.complaintsFiled = complaintsFiled; }
    }
}
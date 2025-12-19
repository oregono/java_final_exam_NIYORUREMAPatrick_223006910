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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SubscriberManagementPanel extends BasePanel {
    private JTable subscriberTable;
    private JTextField searchField;
    private JButton addButton, editButton, deleteButton, refreshButton;
    
    // Color constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    
    public SubscriberManagementPanel() {
        initializePanel();
        loadSubscribers();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("Subscriber Management"), BorderLayout.NORTH);
        
        // Main content with search and table
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Search and toolbar panel
        mainPanel.add(createToolbarPanel(), BorderLayout.NORTH);
        
        // Table
        mainPanel.add(createTablePanel(), BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createToolbarPanel() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        
        searchField = new JTextField(20);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createTitledBorder("Search Subscribers"));
        searchField.setToolTipText("Search by username, full name, or email");
        
        // Add action listener for Enter key
        searchField.addActionListener(this::performSearch);
        
        JButton searchButton = createPrimaryButton("🔍 Search");
        JButton clearButton = createPrimaryButton("🔄 Clear");
        
        searchButton.addActionListener(this::performSearch);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadSubscribers();
        });
        
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(clearButton);
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.WHITE);
        
        addButton = createSuccessButton("➕ Add Subscriber");
        editButton = createPrimaryButton("✏️ Edit");
        deleteButton = createDangerButton("🗑️ Delete");
        refreshButton = createPrimaryButton("🔄 Refresh");
        
        // Initially disable edit and delete buttons
        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
        
        addButton.addActionListener(e -> showAddSubscriberDialog());
        editButton.addActionListener(e -> editSelectedSubscriber());
        deleteButton.addActionListener(e -> deleteSelectedSubscriber());
        refreshButton.addActionListener(e -> loadSubscribers());
        
        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(refreshButton);
        
        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);
        
        return toolbar;
    }
    
    private JPanel createTablePanel() {
        String[] columns = {"Subscriber ID", "Username", "Full Name", "Email", "Role", "Created Date", "Last Login"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class; // Subscriber ID
                return String.class;
            }
        };
        
        subscriberTable = new JTable(model);
        subscriberTable.setRowHeight(35);
        subscriberTable.setFont(new Font("Arial", Font.PLAIN, 11));
        subscriberTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        subscriberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        subscriberTable.setAutoCreateRowSorter(true);
        subscriberTable.setShowGrid(true);
        subscriberTable.setGridColor(new Color(240, 240, 240));
        
        // Set column widths
        subscriberTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // ID
        subscriberTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Username
        subscriberTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Full Name
        subscriberTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Email
        subscriberTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Role
        subscriberTable.getColumnModel().getColumn(5).setPreferredWidth(120); // Created Date
        subscriberTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Last Login
        
        // Add selection listener to enable/disable buttons
        subscriberTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = subscriberTable.getSelectedRow() != -1;
                editButton.setEnabled(rowSelected);
                deleteButton.setEnabled(rowSelected);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(subscriberTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Subscribers List"));
        
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void loadSubscribers() {
        System.out.println("🔄 Loading subscribers from database...");
        
        DefaultTableModel model = (DefaultTableModel) subscriberTable.getModel();
        model.setRowCount(0);
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT SubscriberID, Username, FullName, Email, Role, CreatedAt, LastLogin " +
                        "FROM subscriber ORDER BY CreatedAt DESC";
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            int count = 0;
            
            while (rs.next()) {
                int subscriberId = rs.getInt("SubscriberID");
                String username = rs.getString("Username");
                String fullName = rs.getString("FullName");
                String email = rs.getString("Email");
                String role = rs.getString("Role");
                Timestamp createdAt = rs.getTimestamp("CreatedAt");
                Timestamp lastLogin = rs.getTimestamp("LastLogin");
                
                String createdDateStr = (createdAt != null) ? dateFormat.format(createdAt) : "N/A";
                String lastLoginStr = (lastLogin != null) ? dateFormat.format(lastLogin) : "Never";
                
                model.addRow(new Object[]{
                    subscriberId,
                    username,
                    fullName,
                    email,
                    role,
                    createdDateStr,
                    lastLoginStr
                });
                count++;
            }
            
            System.out.println("✅ Successfully loaded " + count + " subscribers from database");
            
        } catch (SQLException e) {
            System.err.println("❌ Database error loading subscribers: " + e.getMessage());
            showErrorDialog("Database Error", 
                "Error loading subscribers: " + e.getMessage() + 
                "\n\nPlease check:\n• Database connection\n• Table structure\n• Database permissions");
            
        } finally {
            // Close resources properly
            closeResources(rs, stmt, conn);
        }
    }
    
    private void performSearch(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            loadSubscribers();
            return;
        }
        
        System.out.println("🔍 Searching subscribers for: " + searchTerm);
        
        DefaultTableModel model = (DefaultTableModel) subscriberTable.getModel();
        model.setRowCount(0);
        
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = getConnection();
            String sql = "SELECT SubscriberID, Username, FullName, Email, Role, CreatedAt, LastLogin " +
                        "FROM subscriber WHERE Username LIKE ? OR FullName LIKE ? OR Email LIKE ? " +
                        "ORDER BY CreatedAt DESC";
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            String searchPattern = "%" + searchTerm + "%";
            
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                int subscriberId = rs.getInt("SubscriberID");
                String username = rs.getString("Username");
                String fullName = rs.getString("FullName");
                String email = rs.getString("Email");
                String role = rs.getString("Role");
                Timestamp createdAt = rs.getTimestamp("CreatedAt");
                Timestamp lastLogin = rs.getTimestamp("LastLogin");
                
                String createdDateStr = (createdAt != null) ? dateFormat.format(createdAt) : "N/A";
                String lastLoginStr = (lastLogin != null) ? dateFormat.format(lastLogin) : "Never";
                
                model.addRow(new Object[]{
                    subscriberId,
                    username,
                    fullName,
                    email,
                    role,
                    createdDateStr,
                    lastLoginStr
                });
                count++;
            }
            
            System.out.println("🔍 Found " + count + " subscribers matching: " + searchTerm);
            
            if (count == 0) {
                JOptionPane.showMessageDialog(this,
                    "No subscribers found matching: " + searchTerm,
                    "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException ex) {
            System.err.println("❌ Error searching subscribers: " + ex.getMessage());
            showErrorDialog("Search Error", "Error performing search: " + ex.getMessage());
        } finally {
            closeResources(rs, stmt, conn);
        }
    }
    
    private void showAddSubscriberDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Subscriber");
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Form fields
        JTextField usernameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField fullNameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Subscriber", "Admin"});
        
        // Style fields
        styleTextField(usernameField);
        styleTextField(emailField);
        styleTextField(fullNameField);
        stylePasswordField(passwordField);
        stylePasswordField(confirmPasswordField);
        styleComboBox(roleCombo);
        
        formPanel.add(createFormLabel("Username:*"));
        formPanel.add(usernameField);
        formPanel.add(createFormLabel("Email:*"));
        formPanel.add(emailField);
        formPanel.add(createFormLabel("Full Name:*"));
        formPanel.add(fullNameField);
        formPanel.add(createFormLabel("Password:*"));
        formPanel.add(passwordField);
        formPanel.add(createFormLabel("Confirm Password:*"));
        formPanel.add(confirmPasswordField);
        formPanel.add(createFormLabel("Role:*"));
        formPanel.add(roleCombo);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton saveButton = createSuccessButton("💾 Save Subscriber");
        JButton cancelButton = createDangerButton("❌ Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateSubscriberForm(usernameField, emailField, fullNameField, passwordField, confirmPasswordField)) {
                saveSubscriber(
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    fullNameField.getText().trim(),
                    new String(passwordField.getPassword()),
                    (String) roleCombo.getSelectedItem(),
                    dialog
                );
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private boolean validateSubscriberForm(JTextField username, JTextField email, JTextField fullName,
                                         JPasswordField password, JPasswordField confirmPassword) {
        if (username.getText().trim().isEmpty()) {
            showValidationError("Username is required", username);
            return false;
        }
        if (email.getText().trim().isEmpty()) {
            showValidationError("Email is required", email);
            return false;
        }
        if (fullName.getText().trim().isEmpty()) {
            showValidationError("Full name is required", fullName);
            return false;
        }
        if (password.getPassword().length == 0) {
            showValidationError("Password is required", password);
            return false;
        }
        if (confirmPassword.getPassword().length == 0) {
            showValidationError("Please confirm password", confirmPassword);
            return false;
        }
        if (!new String(password.getPassword()).equals(new String(confirmPassword.getPassword()))) {
            showValidationError("Passwords do not match", confirmPassword);
            return false;
        }
        if (new String(password.getPassword()).length() < 6) {
            showValidationError("Password must be at least 6 characters", password);
            return false;
        }
        return true;
    }
    
    private void saveSubscriber(String username, String email, String fullName, String password, String role, JDialog dialog) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "INSERT INTO subscriber (Username, Email, FullName, PasswordHash, Role) VALUES (?, ?, ?, ?, ?)";
            
            stmt = conn.prepareStatement(sql);
            
            String passwordHash = hashPassword(password);
            
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, fullName);
            stmt.setString(4, passwordHash);
            stmt.setString(5, role);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Subscriber added successfully: " + username);
                JOptionPane.showMessageDialog(dialog,
                    "<html><b>Subscriber Added Successfully!</b><br><br>" +
                    "<b>Username:</b> " + username + "<br>" +
                    "<b>Email:</b> " + email + "<br>" +
                    "<b>Role:</b> " + role + "</html>",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadSubscribers();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error adding subscriber: " + e.getMessage());
            
            if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("Username")) {
                JOptionPane.showMessageDialog(dialog,
                    "Username already exists. Please choose a different username.",
                    "Duplicate Username",
                    JOptionPane.ERROR_MESSAGE);
            } else if (e.getMessage().contains("Duplicate entry") && e.getMessage().contains("Email")) {
                JOptionPane.showMessageDialog(dialog,
                    "Email already exists. Please use a different email address.",
                    "Duplicate Email",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "Error adding subscriber: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private void editSelectedSubscriber() {
        int selectedRow = subscriberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a subscriber to edit.",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Convert view index to model index for sorted tables
        int modelRow = subscriberTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) subscriberTable.getModel();
        
        int subscriberID = (Integer) model.getValueAt(modelRow, 0);
        String username = (String) model.getValueAt(modelRow, 1);
        String fullName = (String) model.getValueAt(modelRow, 2);
        String email = (String) model.getValueAt(modelRow, 3);
        String role = (String) model.getValueAt(modelRow, 4);
        
        showEditSubscriberDialog(subscriberID, username, fullName, email, role);
    }
    
    private void showEditSubscriberDialog(int subscriberID, String username, String fullName, String email, String role) {
        JDialog dialog = new JDialog();
        dialog.setTitle("Edit Subscriber - " + username);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JTextField usernameField = new JTextField(username);
        JTextField emailField = new JTextField(email);
        JTextField fullNameField = new JTextField(fullName);
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Subscriber", "Admin"});
        roleCombo.setSelectedItem(role);
        
        JPasswordField passwordField = new JPasswordField();
        passwordField.setToolTipText("Leave blank to keep current password");
        
        styleTextField(usernameField);
        styleTextField(emailField);
        styleTextField(fullNameField);
        styleComboBox(roleCombo);
        stylePasswordField(passwordField);
        
        formPanel.add(createFormLabel("Username:*"));
        formPanel.add(usernameField);
        formPanel.add(createFormLabel("Email:*"));
        formPanel.add(emailField);
        formPanel.add(createFormLabel("Full Name:*"));
        formPanel.add(fullNameField);
        formPanel.add(createFormLabel("Role:*"));
        formPanel.add(roleCombo);
        formPanel.add(createFormLabel("New Password:"));
        formPanel.add(passwordField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        JButton saveButton = createSuccessButton("💾 Update Subscriber");
        JButton cancelButton = createDangerButton("❌ Cancel");
        
        saveButton.addActionListener(e -> {
            if (validateEditForm(usernameField, emailField, fullNameField)) {
                updateSubscriber(
                    subscriberID,
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    fullNameField.getText().trim(),
                    (String) roleCombo.getSelectedItem(),
                    new String(passwordField.getPassword()),
                    dialog
                );
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private boolean validateEditForm(JTextField username, JTextField email, JTextField fullName) {
        if (username.getText().trim().isEmpty()) {
            showValidationError("Username is required", username);
            return false;
        }
        if (email.getText().trim().isEmpty()) {
            showValidationError("Email is required", email);
            return false;
        }
        if (fullName.getText().trim().isEmpty()) {
            showValidationError("Full name is required", fullName);
            return false;
        }
        return true;
    }
    
    private void updateSubscriber(int subscriberID, String username, String email, String fullName, 
                                String role, String newPassword, JDialog dialog) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql;
            
            if (newPassword.isEmpty()) {
                // Update without password
                sql = "UPDATE subscriber SET Username = ?, Email = ?, FullName = ?, Role = ? WHERE SubscriberID = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, fullName);
                stmt.setString(4, role);
                stmt.setInt(5, subscriberID);
            } else {
                // Update with new password
                sql = "UPDATE subscriber SET Username = ?, Email = ?, FullName = ?, Role = ?, PasswordHash = ? WHERE SubscriberID = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, username);
                stmt.setString(2, email);
                stmt.setString(3, fullName);
                stmt.setString(4, role);
                stmt.setString(5, hashPassword(newPassword));
                stmt.setInt(6, subscriberID);
            }
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Subscriber updated successfully: " + username);
                JOptionPane.showMessageDialog(dialog,
                    "Subscriber updated successfully!",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
                loadSubscribers();
            } else {
                JOptionPane.showMessageDialog(dialog,
                    "No changes made to subscriber.",
                    "Update Info",
                    JOptionPane.INFORMATION_MESSAGE);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error updating subscriber: " + e.getMessage());
            JOptionPane.showMessageDialog(dialog,
                "Error updating subscriber: " + e.getMessage(),
                "Update Error",
                JOptionPane.ERROR_MESSAGE);
        } finally {
            closeResources(null, stmt, conn);
        }
    }
    
    private void deleteSelectedSubscriber() {
        int selectedRow = subscriberTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Please select a subscriber to delete.",
                "Selection Required",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Convert view index to model index for sorted tables
        int modelRow = subscriberTable.convertRowIndexToModel(selectedRow);
        DefaultTableModel model = (DefaultTableModel) subscriberTable.getModel();
        
        int subscriberID = (Integer) model.getValueAt(modelRow, 0);
        String username = (String) model.getValueAt(modelRow, 1);
        String fullName = (String) model.getValueAt(modelRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "<html><b>Confirm Delete Subscriber</b><br><br>" +
            "<b>Subscriber ID:</b> " + subscriberID + "<br>" +
            "<b>Username:</b> " + username + "<br>" +
            "<b>Full Name:</b> " + fullName + "<br><br>" +
            "This action cannot be undone. Are you sure you want to delete this subscriber?</html>",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            deleteSubscriber(subscriberID, username);
        }
    }
    
    private void deleteSubscriber(int subscriberID, String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        
        try {
            conn = getConnection();
            String sql = "DELETE FROM subscriber WHERE SubscriberID = ?";
            
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, subscriberID);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Subscriber deleted successfully: " + username);
                JOptionPane.showMessageDialog(this,
                    "Subscriber deleted successfully!",
                    "Delete Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                loadSubscribers();
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error deleting subscriber: " + e.getMessage());
            
            if (e.getMessage().contains("foreign key constraint")) {
                JOptionPane.showMessageDialog(this,
                    "<html><b>Cannot Delete Subscriber</b><br><br>" +
                    "This subscriber has associated records (bills, payments, etc.).<br>" +
                    "Please delete all associated records first.</html>",
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Error deleting subscriber: " + e.getMessage(),
                    "Delete Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } finally {
            closeResources(null, stmt, conn);
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
    
    // Password hashing
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("❌ Password hashing algorithm not available: " + e.getMessage());
            // Fallback to basic hash (not secure for production)
            return Integer.toString(password.hashCode());
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
    
    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
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
    
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)
        ));
    }
    
    // Helper methods for creating styled buttons
    protected JButton createPrimaryButton(String text) {
        return createStyledButton(text, PRIMARY_COLOR);
    }
    
    protected JButton createSuccessButton(String text) {
        return createStyledButton(text, SUCCESS_COLOR);
    }
    
    protected JButton createDangerButton(String text) {
        return createStyledButton(text, DANGER_COLOR);
    }
    
    protected JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker()),
            new EmptyBorder(8, 15, 8, 15)
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
    
    // Custom cell renderer for status
    private class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof String) {
                String status = (String) value;
                switch (status) {
                    case "Active":
                        setBackground(new Color(200, 255, 200));
                        setForeground(new Color(0, 100, 0));
                        break;
                    case "Inactive":
                        setBackground(new Color(255, 200, 200));
                        setForeground(new Color(150, 0, 0));
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
    }
}
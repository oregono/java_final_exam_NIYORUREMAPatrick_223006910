package com.ui;

import com.dao.UserDAO;
import com.models.User;
import com.utils.DB;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginForm extends JFrame implements ActionListener {
    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton cancelButton;
    private JLabel titleLabel;
    private JLabel usernameLabel;
    private JLabel passwordLabel;
    private JLabel versionLabel;
    
    // DAO for database operations
    private UserDAO userDAO;

    public LoginForm() {
        userDAO = new UserDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Utilities Platform System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        
        // Set layout
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        // Title Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(0, 102, 204)); // Professional blue
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        titleLabel = new JLabel("UTILITIES PLATFORM SYSTEM", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
      
        
        add(titlePanel, BorderLayout.NORTH);

        // Main Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 20, 40));

        // Username
        usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 40, 100, 25);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 40, 250, 35);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        formPanel.add(usernameField);

        // Password
        passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 100, 100, 25);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        formPanel.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 100, 250, 35);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        formPanel.add(passwordField);

        
        JLabel adminLabel = new JLabel("Admin: username='admin', password='admin123'");
        adminLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        adminLabel.setForeground(Color.DARK_GRAY);
        
        JLabel userLabel = new JLabel("Subscriber: username='john_doe', password='user123'");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        userLabel.setForeground(Color.DARK_GRAY);
        


        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        loginButton = createStyledButton("Login", new Color(0, 102, 204));
        cancelButton = createStyledButton("Cancel", new Color(204, 0, 0));

        buttonPanel.add(loginButton);
        buttonPanel.add(cancelButton);

        // Add panels to frame
        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add Enter key listener for login
        getRootPane().setDefaultButton(loginButton);
        
        // Focus on username field when form opens
        SwingUtilities.invokeLater(() -> usernameField.requestFocusInWindow());
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
        
        button.addActionListener(this);
        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            performLogin();
        } else if (e.getSource() == cancelButton) {
            performCancel();
        }
    }

    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showErrorMessage("Please enter both username and password.", "Validation Error");
            return;
        }

        // Show loading indicator
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        loginButton.setEnabled(false);

        try {
            // Authenticate user
            User user = userDAO.authenticate(username, password);
            
            if (user != null) {
                // Login successful
                showSuccessMessage("Login Successful!\nWelcome, " + user.getFullName() + " (" + user.getRole() + ")");
                
                // Close login form and open dashboard
                dispose();
                openDashboard(user);
                
            } else {
                // Login failed
                showErrorMessage("Invalid username or password. Please try again.", "Login Failed");
                
                // Clear password field and refocus
                passwordField.setText("");
                usernameField.requestFocus();
            }
            
        } catch (Exception ex) {
            showErrorMessage("Database error: " + ex.getMessage(), "System Error");
            ex.printStackTrace();
        } finally {
            // Restore cursor and button state
            setCursor(Cursor.getDefaultCursor());
            loginButton.setEnabled(true);
        }
    }

    private void performCancel() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit the application?",
            "Exit Confirmation",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void openDashboard(User user) {
        // Open the main dashboard
        SwingUtilities.invokeLater(() -> {
            new Dashboard(user).setVisible(true);
        });
        
        System.out.println("🎯 Dashboard opened for user: " + user.getUsername());
        System.out.println("🎯 User role: " + user.getRole());
        System.out.println("🎯 User ID: " + user.getSubscriberID());
    }

    private void showErrorMessage(String message, String title) {
        JOptionPane.showMessageDialog(this, 
            message, 
            title, 
            JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, 
            message, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }

    // Main method for testing
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getLookAndFeel());
            
            // Set some UI improvements
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("Panel.background", Color.WHITE);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}
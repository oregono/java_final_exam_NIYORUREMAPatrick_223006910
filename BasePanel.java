package com.panels;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BasePanel extends JPanel {
    // Color scheme for consistent styling
    protected Color primaryColor = new Color(41, 128, 185);    // Blue
    protected Color secondaryColor = new Color(240, 240, 240); // Light Gray
    protected Color successColor = new Color(39, 174, 96);     // Green
    protected Color warningColor = new Color(243, 156, 18);    // Orange
    protected Color dangerColor = new Color(231, 76, 60);      // Red
    protected Color infoColor = new Color(52, 152, 219);       // Light Blue
    protected Color darkColor = new Color(51, 51, 51);         // Dark Gray
    
    // Font constants
    protected Font headerFont = new Font("Arial", Font.BOLD, 18);
    protected Font titleFont = new Font("Arial", Font.BOLD, 14);
    protected Font normalFont = new Font("Arial", Font.PLAIN, 12);
    protected Font smallFont = new Font("Arial", Font.PLAIN, 11);
    
    public BasePanel() {
        setBackground(Color.WHITE);
        setLayout(new BorderLayout());
    }
    
    protected JPanel createHeaderPanel(String title) {
        return createHeaderPanel(title, null);
    }
    
    protected JPanel createHeaderPanel(String title, String subtitle) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        
        // Title section
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(headerFont);
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);
        
        // Add subtitle if provided
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(smallFont);
            subtitleLabel.setForeground(new Color(200, 220, 255));
            subtitleLabel.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
            titlePanel.add(subtitleLabel, BorderLayout.SOUTH);
        }
        
        headerPanel.add(titlePanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    protected JPanel createHeaderPanelWithActions(String title, JComponent... actions) {
        JPanel headerPanel = createHeaderPanel(title);
        
        if (actions.length > 0) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
            actionPanel.setOpaque(false);
            
            for (JComponent action : actions) {
                actionPanel.add(action);
            }
            
            headerPanel.add(actionPanel, BorderLayout.EAST);
        }
        
        return headerPanel;
    }
    
    protected JPanel createCardPanel(String title, Component content) {
        return createCardPanel(title, content, null);
    }
    
    protected JPanel createCardPanel(String title, Component content, Color borderColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        
        Border border = BorderFactory.createLineBorder(
            borderColor != null ? borderColor : new Color(220, 220, 220), 
            1
        );
        
        card.setBorder(BorderFactory.createCompoundBorder(
            border,
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        if (title != null) {
            JLabel cardTitle = new JLabel(title);
            cardTitle.setFont(titleFont);
            cardTitle.setForeground(darkColor);
            cardTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
            card.add(cardTitle, BorderLayout.NORTH);
        }
        
        card.add(content, BorderLayout.CENTER);
        return card;
    }
    
    protected JPanel createStatsCard(String title, String value, Color color, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, color),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        
        JLabel titleLabel = new JLabel(title, JLabel.CENTER);
        titleLabel.setFont(smallFont);
        titleLabel.setForeground(Color.DARK_GRAY);
        
        JLabel valueLabel = new JLabel(value, JLabel.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(color);
        
        if (icon != null) {
            JLabel iconLabel = new JLabel(icon, JLabel.CENTER);
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
            card.add(iconLabel, BorderLayout.NORTH);
        }
        
        card.add(titleLabel, BorderLayout.CENTER);
        card.add(valueLabel, BorderLayout.SOUTH);
        
        return card;
    }
    
    // Button creation methods
    protected JButton createPrimaryButton(String text) {
        return createStyledButton(text, primaryColor);
    }
    
    protected JButton createSuccessButton(String text) {
        return createStyledButton(text, successColor);
    }
    
    protected JButton createWarningButton(String text) {
        return createStyledButton(text, warningColor);
    }
    
    protected JButton createDangerButton(String text) {
        return createStyledButton(text, dangerColor);
    }
    
    protected JButton createInfoButton(String text) {
        return createStyledButton(text, infoColor);
    }
    
    protected JButton createSecondaryButton(String text) {
        return createStyledButton(text, new Color(108, 117, 125)); // Bootstrap secondary
    }
    
    protected JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(normalFont);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(color.darker()),
            BorderFactory.createEmptyBorder(8, 15, 8, 15)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effects
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker().darker()),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
                button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(color.darker()),
                    BorderFactory.createEmptyBorder(8, 15, 8, 15)
                ));
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                button.setBackground(color.darker().darker());
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                button.setBackground(color.darker());
            }
        });
        
        return button;
    }
    
    // Form component styling methods
    protected JTextField createStyledTextField(int columns) {
        JTextField field = new JTextField(columns);
        styleFormComponent(field);
        return field;
    }
    
    protected JPasswordField createStyledPasswordField(int columns) {
        JPasswordField field = new JPasswordField(columns);
        styleFormComponent(field);
        return field;
    }
    
    protected JTextArea createStyledTextArea(int rows, int columns) {
        JTextArea area = new JTextArea(rows, columns);
        styleFormComponent(area);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }
    
    protected JComboBox<String> createStyledComboBox(String[] items) {
        JComboBox<String> combo = new JComboBox<>(items);
        styleFormComponent(combo);
        return combo;
    }
    
    private void styleFormComponent(JComponent component) {
        component.setFont(normalFont);
        component.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(206, 212, 218)),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        component.setBackground(Color.WHITE);
    }
    
    // Loading and status methods
    protected JDialog createLoadingDialog(String message) {
        JDialog loadingDialog = new JDialog();
        loadingDialog.setUndecorated(true);
        loadingDialog.setModal(true);
        loadingDialog.setSize(200, 120);
        loadingDialog.setLocationRelativeTo(this);
        
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(BorderFactory.createLineBorder(primaryColor, 2));
        contentPanel.setBackground(Color.WHITE);
        
        JLabel messageLabel = new JLabel(message, JLabel.CENTER);
        messageLabel.setFont(normalFont);
        
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        contentPanel.add(progressBar, BorderLayout.SOUTH);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        loadingDialog.add(contentPanel);
        return loadingDialog;
    }
    
    protected void showSuccessMessage(String message) {
        showMessage(message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    protected void showErrorMessage(String message) {
        showMessage(message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    protected void showWarningMessage(String message) {
        showMessage(message, "Warning", JOptionPane.WARNING_MESSAGE);
    }
    
    protected void showInfoMessage(String message) {
        showMessage(message, "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }
    
    protected int showConfirmDialog(String message, String title) {
        return JOptionPane.showConfirmDialog(this, message, title, 
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }
    
    // Layout utility methods
    protected JPanel createFormPanel(int rows, int cols, int hgap, int vgap) {
        JPanel formPanel = new JPanel(new GridLayout(rows, cols, hgap, vgap));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return formPanel;
    }
    
    protected JPanel createBorderedPanel(Component component, String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            title
        ));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }
    
    protected JSeparator createSeparator() {
        JSeparator separator = new JSeparator();
        separator.setBackground(new Color(200, 200, 200));
        return separator;
    }
    
    // Color utility methods
    protected Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "success":
            case "completed":
            case "paid":
            case "resolved":
            case "verified":
                return successColor;
            case "warning":
            case "pending":
            case "in progress":
                return warningColor;
            case "error":
            case "failed":
            case "overdue":
            case "open":
                return dangerColor;
            case "info":
            case "active":
            default:
                return infoColor;
        }
    }
    
    protected Color getPriorityColor(String priority) {
        switch (priority.toLowerCase()) {
            case "urgent":
            case "high":
                return dangerColor;
            case "medium":
                return warningColor;
            case "low":
                return successColor;
            default:
                return infoColor;
        }
    }
}
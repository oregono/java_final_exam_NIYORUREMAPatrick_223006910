package com.panels;

import com.models.User;
import com.utils.DB;
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
 * SubmitMeterReadingPanel - Professional meter reading submission interface
 * Provides users with comprehensive meter reading submission and history tracking
 */
public class SubmitMeterReadingPanel extends BasePanel {
    
    private User currentUser;
    private JComboBox<String> serviceCombo;
    private JTextField readingField;
    private JTextArea notesArea;
    private JLabel lastReadingLabel;
    private JLabel consumptionLabel;
    private JLabel unitLabel;
    private JTable historyTable;
    
    // Color constants for consistent styling
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    
    public SubmitMeterReadingPanel(User user) {
        this.currentUser = user;
        initializePanel();
        loadReadingHistory();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("Submit Meter Reading"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Reading form and history
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            createReadingFormPanel(), createReadingHistoryPanel());
        splitPane.setResizeWeight(0.6);
        splitPane.setDividerLocation(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createReadingFormPanel() {
        JPanel formPanel = createCardPanel("Submit New Reading", createReadingForm());
        return formPanel;
    }
    
    private JPanel createReadingForm() {
        JPanel form = new JPanel(new BorderLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        fieldsPanel.setBackground(Color.WHITE);
        
        serviceCombo = new JComboBox<>(new String[]{"Water", "Electricity", "Gas"});
        readingField = new JTextField();
        unitLabel = new JLabel("kWh");
        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        
        lastReadingLabel = new JLabel("Loading...");
        consumptionLabel = new JLabel("Loading...");
        
        // Style components
        styleComboBox(serviceCombo);
        readingField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)
        ));
        
        // Style labels
        lastReadingLabel.setForeground(PRIMARY_COLOR);
        lastReadingLabel.setFont(lastReadingLabel.getFont().deriveFont(Font.BOLD));
        consumptionLabel.setForeground(SUCCESS_COLOR);
        consumptionLabel.setFont(consumptionLabel.getFont().deriveFont(Font.BOLD));
        unitLabel.setForeground(Color.DARK_GRAY);
        
        fieldsPanel.add(createFormLabel("Service:*"));
        fieldsPanel.add(serviceCombo);
        fieldsPanel.add(createFormLabel("Current Reading:*"));
        fieldsPanel.add(readingField);
        fieldsPanel.add(createFormLabel("Unit:"));
        fieldsPanel.add(unitLabel);
        fieldsPanel.add(createFormLabel("Last Reading:"));
        fieldsPanel.add(lastReadingLabel);
        fieldsPanel.add(createFormLabel("Last Consumption:"));
        fieldsPanel.add(consumptionLabel);
        
        // Update unit based on service selection
        serviceCombo.addActionListener(e -> updateServiceInfo());
        
        // Notes section
        JPanel notesPanel = new JPanel(new BorderLayout());
        notesPanel.setBackground(Color.WHITE);
        notesPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JLabel notesTitle = createFormLabel("Notes (Optional):");
        notesPanel.add(notesTitle, BorderLayout.NORTH);
        notesPanel.add(notesScroll, BorderLayout.CENTER);
        
        // Character counter for notes
        JLabel charCountLabel = new JLabel("0/500 characters");
        charCountLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        charCountLabel.setForeground(Color.GRAY);
        notesPanel.add(charCountLabel, BorderLayout.SOUTH);
        
        // Update character count
        notesArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCharCount(); }
            
            private void updateCharCount() {
                SwingUtilities.invokeLater(() -> {
                    int length = notesArea.getText().length();
                    charCountLabel.setText(length + "/500 characters");
                    if (length > 500) {
                        charCountLabel.setForeground(ERROR_COLOR);
                        charCountLabel.setFont(charCountLabel.getFont().deriveFont(Font.BOLD));
                    } else if (length > 400) {
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
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton submitButton = createSuccessButton("📤 Submit Reading");
        JButton clearButton = createPrimaryButton("🔄 Clear Form");
        
        submitButton.addActionListener(this::submitReading);
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);
        
        form.add(fieldsPanel, BorderLayout.NORTH);
        form.add(notesPanel, BorderLayout.CENTER);
        form.add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize with default service
        updateServiceInfo();
        
        return form;
    }
    
    private JPanel createReadingHistoryPanel() {
        JPanel historyPanel = createCardPanel("Recent Reading History", createHistoryTable());
        return historyPanel;
    }
    
    private JScrollPane createHistoryTable() {
        String[] columns = {"Date", "Service", "Reading", "Unit", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(model);
        historyTable.setRowHeight(30);
        historyTable.setFont(new Font("Arial", Font.PLAIN, 11));
        historyTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        historyTable.setEnabled(false);
        historyTable.setShowGrid(true);
        historyTable.setGridColor(new Color(240, 240, 240));
        
        // Set column widths
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Date
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(80);  // Service
        historyTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Reading
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // Unit
        historyTable.getColumnModel().getColumn(4).setPreferredWidth(80);  // Status
        
        // Custom status renderer
        historyTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (value instanceof String) {
                    String status = (String) value;
                    if ("Verified".equals(status)) {
                        setBackground(new Color(200, 255, 200));
                        setForeground(new Color(0, 100, 0));
                        setFont(getFont().deriveFont(Font.BOLD));
                    } else if ("Pending".equals(status)) {
                        setBackground(new Color(255, 255, 200));
                        setForeground(new Color(150, 150, 0));
                    } else {
                        setBackground(new Color(255, 200, 200));
                        setForeground(new Color(150, 0, 0));
                    }
                }
                
                super.setHorizontalAlignment(SwingConstants.CENTER);
                setBorder(new EmptyBorder(0, 5, 0, 5));
                return this;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        return scrollPane;
    }
    
    private void loadReadingHistory() {
        DefaultTableModel model = (DefaultTableModel) historyTable.getModel();
        model.setRowCount(0);
        
        String username = currentUser != null ? currentUser.getUsername() : "";
        String sql = "SELECT Date, Service, Reading, Unit, Status " +
                    "FROM meter WHERE Subscriber = ? " +
                    "ORDER BY Date DESC LIMIT 10";
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            int count = 0;
            while (rs.next()) {
                model.addRow(new Object[]{
                    dateFormat.format(rs.getTimestamp("Date")),
                    rs.getString("Service"),
                    String.format("%.2f", rs.getDouble("Reading")),
                    rs.getString("Unit"),
                    rs.getString("Status")
                });
                count++;
            }
            
            System.out.println("✅ Loaded " + count + " meter reading records from database");
            
            // If no readings found, add a message
            if (count == 0) {
                model.addRow(new Object[]{"No readings", "found", "0.00", "N/A", "N/A"});
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading reading history: " + e.getMessage());
            e.printStackTrace();
            model.addRow(new Object[]{"Error", "loading data", "0.00", "N/A", "Error"});
        }
    }
    
    private void updateServiceInfo() {
        String service = (String) serviceCombo.getSelectedItem();
        System.out.println("🔄 Updating service info for: " + service);
        
        // Get last reading from database
        LastReadingInfo lastReading = getLastReading(service);
        
        unitLabel.setText(lastReading.getUnit());
        lastReadingLabel.setText(String.format("%.2f", lastReading.getReading()));
        consumptionLabel.setText(String.format("%.1f %s", lastReading.getConsumption(), lastReading.getUnit()));
        
        // Update tooltips
        readingField.setToolTipText("Enter current " + service + " meter reading in " + lastReading.getUnit());
    }
    
    private LastReadingInfo getLastReading(String service) {
        String username = currentUser != null ? currentUser.getUsername() : "";
        String sql = "SELECT Reading, Unit FROM meter " +
                    "WHERE Subscriber = ? AND Service = ? " +
                    "ORDER BY Date DESC LIMIT 1";
        
        // Default values
        double reading = 0.0;
        String unit = getDefaultUnit(service);
        double consumption = 0.0;
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, service);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                reading = rs.getDouble("Reading");
                unit = rs.getString("Unit");
                
                // Calculate consumption from previous reading
                consumption = calculateConsumption(service, reading);
            } else {
                // No previous reading found, use default values
                reading = getDefaultReading(service);
                consumption = getDefaultConsumption(service);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading last reading: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to default values
            reading = getDefaultReading(service);
            consumption = getDefaultConsumption(service);
        }
        
        return new LastReadingInfo(reading, unit, consumption);
    }
    
    private double calculateConsumption(String service, double currentReading) {
        String username = currentUser != null ? currentUser.getUsername() : "";
        String sql = "SELECT Reading FROM meter " +
                    "WHERE Subscriber = ? AND Service = ? " +
                    "ORDER BY Date DESC LIMIT 1, 1"; // Get the second most recent reading
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, service);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double previousReading = rs.getDouble("Reading");
                return currentReading - previousReading;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error calculating consumption: " + e.getMessage());
        }
        
        // Return default consumption if no previous reading found
        return getDefaultConsumption(service);
    }
    
    private String getDefaultUnit(String service) {
        switch (service) {
            case "Electricity": return "kWh";
            case "Water": return "m³";
            case "Gas": return "m³";
            default: return "";
        }
    }
    
    private double getDefaultReading(String service) {
        switch (service) {
            case "Electricity": return 1250.50;
            case "Water": return 456.75;
            case "Gas": return 850.25;
            default: return 0.0;
        }
    }
    
    private double getDefaultConsumption(String service) {
        switch (service) {
            case "Electricity": return 125.5;
            case "Water": return 45.7;
            case "Gas": return 85.0;
            default: return 0.0;
        }
    }
    
    private void submitReading(ActionEvent e) {
        String service = (String) serviceCombo.getSelectedItem();
        String reading = readingField.getText().trim();
        String notes = notesArea.getText().trim();
        
        System.out.println("📤 Submitting meter reading for " + service + ": " + reading);
        
        // Validation
        if (reading.isEmpty()) {
            showValidationError("Please enter the meter reading.", readingField);
            return;
        }
        
        if (notes.length() > 500) {
            showValidationError("Notes must be 500 characters or less.", notesArea);
            return;
        }
        
        try {
            double readingValue = Double.parseDouble(reading);
            if (readingValue <= 0) {
                showValidationError("Reading must be a positive number.", readingField);
                return;
            }
            
            // Check if reading is reasonable (not less than previous reading)
            LastReadingInfo lastReading = getLastReading(service);
            if (readingValue < lastReading.getReading()) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "<html><b>Potential Reading Error</b><br><br>" +
                    "The reading you entered (" + readingValue + ") is less than the last reading (" + 
                    String.format("%.2f", lastReading.getReading()) + ").<br>" +
                    "This might indicate an error or meter replacement.<br><br>" +
                    "Are you sure you want to submit this reading?</html>",
                    "Confirm Reading", 
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Check for unusually high consumption
            double consumption = readingValue - lastReading.getReading();
            if (consumption > getReasonableConsumptionLimit(service)) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "<html><b>High Consumption Alert</b><br><br>" +
                    "The calculated consumption (" + String.format("%.1f", consumption) + " " + 
                    lastReading.getUnit() + ") seems unusually high.<br>" +
                    "This might indicate a leak or meter issue.<br><br>" +
                    "Do you want to proceed with submission?</html>",
                    "High Consumption Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (confirm != JOptionPane.YES_OPTION) {
                    return;
                }
            }
            
            // Submit the reading to database
            boolean success = saveReadingToDatabase(service, readingValue, notes, lastReading.getUnit(), consumption);
            
            if (success) {
                showSuccessMessage(service, readingValue, lastReading.getUnit());
                clearForm();
                loadReadingHistory(); // Refresh history table
                updateServiceInfo(); // Update last reading info
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to submit reading. Please try again or contact support.",
                    "Submission Error",
                    JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            showValidationError("Please enter a valid number for the reading.", readingField);
        }
    }
    
    private void showValidationError(String message, JComponent component) {
        JOptionPane.showMessageDialog(this, message, "Validation Error", JOptionPane.ERROR_MESSAGE);
        component.requestFocus();
        if (component instanceof JTextComponent) {
            ((JTextComponent) component).selectAll();
        }
    }
    
    private boolean saveReadingToDatabase(String service, double reading, String notes, String unit, double consumption) {
        String sql = "INSERT INTO meter (Subscriber, Service, Unit, Reading, Consumption, Type, Status) " +
                    "VALUES (?, ?, ?, ?, ?, 'Current', 'Pending')";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, currentUser != null ? currentUser.getUsername() : "Unknown");
            stmt.setString(2, service);
            stmt.setString(3, unit);
            stmt.setDouble(4, reading);
            stmt.setDouble(5, consumption);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Meter reading saved to database:");
                System.out.println("   Service: " + service);
                System.out.println("   Reading: " + reading);
                System.out.println("   Unit: " + unit);
                System.out.println("   Consumption: " + consumption);
                System.out.println("   User: " + (currentUser != null ? currentUser.getUsername() : "Unknown"));
                return true;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error saving meter reading: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private void showSuccessMessage(String service, double readingValue, String unit) {
        String reference = "MTR-" + System.currentTimeMillis();
        
        JOptionPane.showMessageDialog(this,
            "<html><div style='width: 350px;'>" +
            "<h3>Meter Reading Submitted Successfully!</h3>" +
            "<p><b>Service:</b> " + service + "</p>" +
            "<p><b>Reading:</b> " + String.format("%.2f", readingValue) + " " + unit + "</p>" +
            "<p><b>Date:</b> " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()) + "</p>" +
            "<p><b>Reference:</b> " + reference + "</p>" +
            "<p>Your reading has been recorded and will be verified by our team.</p>" +
            "<p>You will receive a confirmation email shortly.</p>" +
            "</div></html>",
            "Submission Successful", 
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private double getReasonableConsumptionLimit(String service) {
        switch (service) {
            case "Electricity": return 500.0; // kWh
            case "Water": return 100.0; // m³
            case "Gas": return 200.0; // m³
            default: return 1000.0;
        }
    }
    
    private void clearForm() {
        readingField.setText("");
        notesArea.setText("");
        readingField.requestFocus();
        System.out.println("🔄 Meter reading form cleared");
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
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            new EmptyBorder(5, 5, 5, 5)
        ));
    }
    
    // Helper class for last reading information
    private static class LastReadingInfo {
        private double reading;
        private String unit;
        private double consumption;
        
        public LastReadingInfo(double reading, String unit, double consumption) {
            this.reading = reading;
            this.unit = unit;
            this.consumption = consumption;
        }
        
        public double getReading() { return reading; }
        public String getUnit() { return unit; }
        public double getConsumption() { return consumption; }
    }
}
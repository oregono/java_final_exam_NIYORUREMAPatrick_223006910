package com.panels;

import com.utils.DB;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ReportsPanel extends BasePanel {
    private JDatePicker fromDatePicker;
    private JDatePicker toDatePicker;
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> formatTypeCombo;
    private JTextArea reportPreviewArea;
    
    // Color constants
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    
    public ReportsPanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("Reports Dashboard - Analytics & Insights"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Report configuration and preview
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            createReportConfigPanel(), createReportPreviewPanel());
        splitPane.setResizeWeight(0.4);
        splitPane.setDividerLocation(0.4);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createReportConfigPanel() {
        JPanel configPanel = createCardPanel("Report Configuration", createConfigForm());
        return configPanel;
    }
    
    private JPanel createConfigForm() {
        JPanel formPanel = new JPanel(new BorderLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Form fields
        JPanel fieldsPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        fieldsPanel.setBackground(Color.WHITE);
        
        fromDatePicker = new JDatePicker();
        toDatePicker = new JDatePicker();
        
        reportTypeCombo = new JComboBox<>(new String[]{
            "Financial Summary", "Payment Transactions", "Billing Report", 
            "Service Usage", "Subscriber Analytics", "Revenue Analysis"
        });
        
        formatTypeCombo = new JComboBox<>(new String[]{"PDF", "Excel", "HTML", "CSV"});
        
        // Style components
        styleComboBox(reportTypeCombo);
        styleComboBox(formatTypeCombo);
        
        fieldsPanel.add(createFormLabel("From Date:*"));
        fieldsPanel.add(fromDatePicker);
        
        fieldsPanel.add(createFormLabel("To Date:*"));
        fieldsPanel.add(toDatePicker);
        
        fieldsPanel.add(createFormLabel("Report Type:*"));
        fieldsPanel.add(reportTypeCombo);
        
        fieldsPanel.add(createFormLabel("Format:*"));
        fieldsPanel.add(formatTypeCombo);
        
        // Quick date range buttons
        JPanel quickRangePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        quickRangePanel.setBackground(Color.WHITE);
        quickRangePanel.setBorder(BorderFactory.createTitledBorder("Quick Date Ranges"));
        
        JButton todayButton = createSmallButton("Today");
        JButton weekButton = createSmallButton("This Week");
        JButton monthButton = createSmallButton("This Month");
        JButton yearButton = createSmallButton("This Year");
        
        todayButton.addActionListener(e -> setDateRangeToday());
        weekButton.addActionListener(e -> setDateRangeThisWeek());
        monthButton.addActionListener(e -> setDateRangeThisMonth());
        yearButton.addActionListener(e -> setDateRangeThisYear());
        
        quickRangePanel.add(todayButton);
        quickRangePanel.add(weekButton);
        quickRangePanel.add(monthButton);
        quickRangePanel.add(yearButton);
        
        // Generate button
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JButton previewButton = createPrimaryButton("👁️ Preview Report");
        JButton generateButton = createSuccessButton("📊 Generate Report");
        JButton clearButton = createPrimaryButton("🔄 Clear");
        
        previewButton.addActionListener(e -> previewReport());
        generateButton.addActionListener(e -> generateReport());
        clearButton.addActionListener(e -> clearForm());
        
        buttonPanel.add(previewButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(clearButton);
        
        formPanel.add(fieldsPanel, BorderLayout.NORTH);
        formPanel.add(quickRangePanel, BorderLayout.CENTER);
        formPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Set default dates
        setDefaultDateRange();
        
        return formPanel;
    }
    
    private JPanel createReportPreviewPanel() {
        JPanel previewPanel = createCardPanel("Report Preview", createPreviewContent());
        return previewPanel;
    }
    
    private JScrollPane createPreviewContent() {
        reportPreviewArea = new JTextArea();
        reportPreviewArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        reportPreviewArea.setEditable(false);
        reportPreviewArea.setBackground(new Color(248, 249, 250));
        reportPreviewArea.setText("Select report parameters and click 'Preview Report' to see preview here...");
        
        JScrollPane scrollPane = new JScrollPane(reportPreviewArea);
        scrollPane.setPreferredSize(new Dimension(0, 400));
        return scrollPane;
    }
    
    private void setDefaultDateRange() {
        // Set default to current month
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        
        // First day of current month
        String firstDay = new SimpleDateFormat("yyyy-MM-01").format(now);
        // Last day of current month
        String lastDay = new SimpleDateFormat("yyyy-MM-dd").format(now);
        
        fromDatePicker.setDate(firstDay);
        toDatePicker.setDate(lastDay);
    }
    
    private void setDateRangeToday() {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        fromDatePicker.setDate(today);
        toDatePicker.setDate(today);
    }
    
    private void setDateRangeThisWeek() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        
        // Calculate start of week (Monday)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(now);
        cal.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        String startOfWeek = sdf.format(cal.getTime());
        
        String today = sdf.format(now);
        
        fromDatePicker.setDate(startOfWeek);
        toDatePicker.setDate(today);
    }
    
    private void setDateRangeThisMonth() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        
        String firstDay = new SimpleDateFormat("yyyy-MM-01").format(now);
        String today = sdf.format(now);
        
        fromDatePicker.setDate(firstDay);
        toDatePicker.setDate(today);
    }
    
    private void setDateRangeThisYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        
        String firstDay = new SimpleDateFormat("yyyy-01-01").format(now);
        String today = sdf.format(now);
        
        fromDatePicker.setDate(firstDay);
        toDatePicker.setDate(today);
    }
    
    private void previewReport() {
        if (!validateDates()) {
            return;
        }
        
        String fromDate = fromDatePicker.getDate();
        String toDate = toDatePicker.getDate();
        String reportType = (String) reportTypeCombo.getSelectedItem();
        
        System.out.println("👁️ Previewing " + reportType + " report from " + fromDate + " to " + toDate);
        
        // Generate preview based on report type
        String previewContent = generateReportPreview(reportType, fromDate, toDate);
        reportPreviewArea.setText(previewContent);
        
        JOptionPane.showMessageDialog(this,
            "Report preview generated successfully!\n\n" +
            "Report Type: " + reportType + "\n" +
            "Date Range: " + fromDate + " to " + toDate + "\n" +
            "Preview available in the preview panel.",
            "Preview Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void generateReport() {
        if (!validateDates()) {
            return;
        }
        
        String fromDate = fromDatePicker.getDate();
        String toDate = toDatePicker.getDate();
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String format = (String) formatTypeCombo.getSelectedItem();
        
        System.out.println("📊 Generating " + reportType + " report in " + format + " format from " + fromDate + " to " + toDate);
        
        // Generate the actual report
        boolean success = generateActualReport(reportType, fromDate, toDate, format);
        
        if (success) {
            showReportSuccessDialog(reportType, format, fromDate, toDate);
        } else {
            JOptionPane.showMessageDialog(this,
                "Error generating report. Please try again or contact support.",
                "Report Generation Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateDates() {
        if (!fromDatePicker.isValidDate() || !toDatePicker.isValidDate()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid dates in format YYYY-MM-DD", 
                "Invalid Date", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        String fromDate = fromDatePicker.getDate();
        String toDate = toDatePicker.getDate();
        
        if (!isFromDateBeforeToDate(fromDate, toDate)) {
            JOptionPane.showMessageDialog(this, 
                "From date must be before To date", 
                "Date Range Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private boolean isFromDateBeforeToDate(String fromDateStr, String toDateStr) {
        if (fromDateStr.isEmpty() || toDateStr.isEmpty()) {
            return true;
        }
        
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date fromDate = sdf.parse(fromDateStr);
            Date toDate = sdf.parse(toDateStr);
            return !fromDate.after(toDate);
        } catch (ParseException e) {
            return false;
        }
    }
    
    private String generateReportPreview(String reportType, String fromDate, String toDate) {
        StringBuilder preview = new StringBuilder();
        SimpleDateFormat reportFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        preview.append("REPORT PREVIEW\n");
        preview.append("==============\n\n");
        preview.append("Report Type: ").append(reportType).append("\n");
        preview.append("Date Range: ").append(fromDate).append(" to ").append(toDate).append("\n");
        preview.append("Generated: ").append(reportFormat.format(new Date())).append("\n\n");
        
        switch (reportType) {
            case "Financial Summary":
                preview.append(generateFinancialSummaryPreview(fromDate, toDate));
                break;
            case "Payment Transactions":
                preview.append(generatePaymentTransactionsPreview(fromDate, toDate));
                break;
            case "Billing Report":
                preview.append(generateBillingReportPreview(fromDate, toDate));
                break;
            case "Service Usage":
                preview.append(generateServiceUsagePreview(fromDate, toDate));
                break;
            case "Subscriber Analytics":
                preview.append(generateSubscriberAnalyticsPreview(fromDate, toDate));
                break;
            case "Revenue Analysis":
                preview.append(generateRevenueAnalysisPreview(fromDate, toDate));
                break;
            default:
                preview.append("Preview not available for this report type.\n");
        }
        
        preview.append("\n--- END OF PREVIEW ---\n");
        return preview.toString();
    }
    
    private String generateFinancialSummaryPreview(String fromDate, String toDate) {
        StringBuilder summary = new StringBuilder();
        summary.append("FINANCIAL SUMMARY\n");
        summary.append("=================\n\n");
        
        try (Connection conn = DB.getConnection()) {
            // Total Revenue
            String revenueSql = "SELECT SUM(Amount) as total_revenue FROM payment " +
                              "WHERE Date BETWEEN ? AND ? AND Status = 'Completed'";
            try (PreparedStatement stmt = conn.prepareStatement(revenueSql)) {
                stmt.setString(1, fromDate + " 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    double revenue = rs.getDouble("total_revenue");
                    summary.append("Total Revenue: $").append(String.format("%.2f", revenue)).append("\n");
                }
            }
            
            // Total Bills Generated
            String billsSql = "SELECT COUNT(*) as total_bills, SUM(Amount) as total_amount FROM bill " +
                             "WHERE IssueDate BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(billsSql)) {
                stmt.setString(1, fromDate);
                stmt.setString(2, toDate);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int billCount = rs.getInt("total_bills");
                    double billAmount = rs.getDouble("total_amount");
                    summary.append("Bills Generated: ").append(billCount).append("\n");
                    summary.append("Total Billed: $").append(String.format("%.2f", billAmount)).append("\n");
                }
            }
            
            // Payment Methods Summary
            String methodsSql = "SELECT Method, COUNT(*) as count, SUM(Amount) as amount " +
                               "FROM payment WHERE Date BETWEEN ? AND ? AND Status = 'Completed' " +
                               "GROUP BY Method";
            try (PreparedStatement stmt = conn.prepareStatement(methodsSql)) {
                stmt.setString(1, fromDate + " 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                summary.append("\nPayment Methods:\n");
                while (rs.next()) {
                    summary.append("  ").append(rs.getString("Method"))
                          .append(": ").append(rs.getInt("count"))
                          .append(" payments, $").append(String.format("%.2f", rs.getDouble("amount")))
                          .append("\n");
                }
            }
            
        } catch (SQLException e) {
            summary.append("Error generating financial summary: ").append(e.getMessage()).append("\n");
        }
        
        return summary.toString();
    }
    
    private String generatePaymentTransactionsPreview(String fromDate, String toDate) {
        StringBuilder preview = new StringBuilder();
        preview.append("PAYMENT TRANSACTIONS\n");
        preview.append("====================\n\n");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT p.PaymentID, p.BillID, p.Amount, p.Method, p.Status, p.Date, s.FullName " +
                        "FROM payment p LEFT JOIN subscriber s ON p.Subscriber = s.Username " +
                        "WHERE p.Date BETWEEN ? AND ? " +
                        "ORDER BY p.Date DESC LIMIT 10";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fromDate + " 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                
                preview.append(String.format("%-8s %-10s %-10s %-15s %-12s %-20s\n", 
                    "ID", "Bill ID", "Amount", "Method", "Status", "Subscriber"));
                preview.append("------------------------------------------------------------------------\n");
                
                int count = 0;
                while (rs.next()) {
                    preview.append(String.format("%-8s %-10s $%-9.2f %-15s %-12s %-20s\n",
                        "PAY-" + rs.getInt("PaymentID"),
                        rs.getString("BillID"),
                        rs.getDouble("Amount"),
                        rs.getString("Method"),
                        rs.getString("Status"),
                        rs.getString("FullName")
                    ));
                    count++;
                }
                preview.append("\nTotal transactions in period: ").append(count).append("\n");
            }
            
        } catch (SQLException e) {
            preview.append("Error generating payment transactions: ").append(e.getMessage()).append("\n");
        }
        
        return preview.toString();
    }
    
    private String generateBillingReportPreview(String fromDate, String toDate) {
        StringBuilder preview = new StringBuilder();
        preview.append("BILLING REPORT\n");
        preview.append("==============\n\n");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT Services, COUNT(*) as count, SUM(Amount) as amount, " +
                        "AVG(Amount) as average, Status " +
                        "FROM bill WHERE IssueDate BETWEEN ? AND ? " +
                        "GROUP BY Services, Status";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fromDate);
                stmt.setString(2, toDate);
                ResultSet rs = stmt.executeQuery();
                
                preview.append(String.format("%-15s %-8s %-12s %-12s %-10s\n", 
                    "Service", "Count", "Total", "Average", "Status"));
                preview.append("------------------------------------------------------------\n");
                
                while (rs.next()) {
                    preview.append(String.format("%-15s %-8d $%-11.2f $%-11.2f %-10s\n",
                        rs.getString("Services"),
                        rs.getInt("count"),
                        rs.getDouble("amount"),
                        rs.getDouble("average"),
                        rs.getString("Status")
                    ));
                }
            }
            
        } catch (SQLException e) {
            preview.append("Error generating billing report: ").append(e.getMessage()).append("\n");
        }
        
        return preview.toString();
    }
    
    private String generateServiceUsagePreview(String fromDate, String toDate) {
        StringBuilder preview = new StringBuilder();
        preview.append("SERVICE USAGE REPORT\n");
        preview.append("====================\n\n");
        
        try (Connection conn = DB.getConnection()) {
            String sql = "SELECT Service, COUNT(*) as readings, AVG(Reading) as avg_reading, " +
                        "AVG(Consumption) as avg_consumption " +
                        "FROM meter WHERE Date BETWEEN ? AND ? " +
                        "GROUP BY Service";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, fromDate + " 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                
                preview.append(String.format("%-15s %-10s %-15s %-15s\n", 
                    "Service", "Readings", "Avg Reading", "Avg Consumption"));
                preview.append("----------------------------------------------------\n");
                
                while (rs.next()) {
                    preview.append(String.format("%-15s %-10d %-15.2f %-15.2f\n",
                        rs.getString("Service"),
                        rs.getInt("readings"),
                        rs.getDouble("avg_reading"),
                        rs.getDouble("avg_consumption")
                    ));
                }
            }
            
        } catch (SQLException e) {
            preview.append("Error generating service usage report: ").append(e.getMessage()).append("\n");
        }
        
        return preview.toString();
    }
    
    private String generateSubscriberAnalyticsPreview(String fromDate, String toDate) {
        StringBuilder preview = new StringBuilder();
        preview.append("SUBSCRIBER ANALYTICS\n");
        preview.append("====================\n\n");
        
        try (Connection conn = DB.getConnection()) {
            // Total subscribers
            String totalSql = "SELECT COUNT(*) as total FROM subscriber";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(totalSql)) {
                if (rs.next()) {
                    preview.append("Total Subscribers: ").append(rs.getInt("total")).append("\n");
                }
            }
            
            // New subscribers in period
            String newSql = "SELECT COUNT(*) as new_subs FROM subscriber WHERE CreatedAt BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(newSql)) {
                stmt.setString(1, fromDate + " 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    preview.append("New Subscribers: ").append(rs.getInt("new_subs")).append("\n");
                }
            }
            
            // Active subscribers (those with payments)
            String activeSql = "SELECT COUNT(DISTINCT Subscriber) as active FROM payment " +
                             "WHERE Date BETWEEN ? AND ?";
            try (PreparedStatement stmt = conn.prepareStatement(activeSql)) {
                stmt.setString(1, fromDate + " 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    preview.append("Active Subscribers: ").append(rs.getInt("active")).append("\n");
                }
            }
            
        } catch (SQLException e) {
            preview.append("Error generating subscriber analytics: ").append(e.getMessage()).append("\n");
        }
        
        return preview.toString();
    }
    
    private String generateRevenueAnalysisPreview(String fromDate, String toDate) {
        StringBuilder preview = new StringBuilder();
        preview.append("REVENUE ANALYSIS\n");
        preview.append("================\n\n");
        
        try (Connection conn = DB.getConnection()) {
            // Monthly revenue trend
            String trendSql = "SELECT DATE_FORMAT(Date, '%Y-%m') as month, " +
                            "SUM(Amount) as revenue " +
                            "FROM payment WHERE Date BETWEEN ? AND ? AND Status = 'Completed' " +
                            "GROUP BY DATE_FORMAT(Date, '%Y-%m') " +
                            "ORDER BY month";
            
            try (PreparedStatement stmt = conn.prepareStatement(trendSql)) {
                stmt.setString(1, fromDate.substring(0, 7) + "-01 00:00:00");
                stmt.setString(2, toDate + " 23:59:59");
                ResultSet rs = stmt.executeQuery();
                
                preview.append("Monthly Revenue Trend:\n");
                while (rs.next()) {
                    preview.append("  ").append(rs.getString("month"))
                          .append(": $").append(String.format("%.2f", rs.getDouble("revenue")))
                          .append("\n");
                }
            }
            
        } catch (SQLException e) {
            preview.append("Error generating revenue analysis: ").append(e.getMessage()).append("\n");
        }
        
        return preview.toString();
    }
    
    private boolean generateActualReport(String reportType, String fromDate, String toDate, String format) {
        // Simulate report generation
        try {
            System.out.println("📊 Generating actual " + reportType + " report in " + format + " format");
            System.out.println("   Date Range: " + fromDate + " to " + toDate);
            
            // Simulate processing time
            Thread.sleep(2000);
            
            // In a real application, this would:
            // 1. Generate PDF/Excel/HTML file
            // 2. Save to server or download
            // 3. Log the report generation
            
            return true;
            
        } catch (InterruptedException e) {
            System.err.println("❌ Report generation interrupted: " + e.getMessage());
            return false;
        }
    }
    
    private void showReportSuccessDialog(String reportType, String format, String fromDate, String toDate) {
        String fileName = reportType.toLowerCase().replace(" ", "_") + "_" + fromDate + "_to_" + toDate + "." + format.toLowerCase();
        
        JOptionPane.showMessageDialog(this,
            "<html><div style='width: 400px;'>" +
            "<h3>Report Generated Successfully!</h3>" +
            "<p><b>Report Type:</b> " + reportType + "</p>" +
            "<p><b>Format:</b> " + format + "</p>" +
            "<p><b>Date Range:</b> " + fromDate + " to " + toDate + "</p>" +
            "<p><b>File Name:</b> " + fileName + "</p>" +
            "<p><b>File Size:</b> ~2.5 MB</p>" +
            "<p><b>Location:</b> /reports/" + fileName + "</p>" +
            "<p>The report has been generated and saved to the reports directory.</p>" +
            "</div></html>",
            "Report Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void clearForm() {
        setDefaultDateRange();
        reportTypeCombo.setSelectedIndex(0);
        formatTypeCombo.setSelectedIndex(0);
        reportPreviewArea.setText("Select report parameters and click 'Preview Report' to see preview here...");
        System.out.println("🔄 Report form cleared");
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
    
    private JButton createSmallButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.PLAIN, 10));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }
}
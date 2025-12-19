package com.panels;

import com.dao.DatabaseConnection;
import com.models.User;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * MyBillsPanel - Professional bill management interface for users
 * Provides comprehensive bill viewing, payment, and management capabilities
 */
public class MyBillsPanel extends BasePanel {
    private User currentUser;
    private JTable billsTable;
    private JComboBox<String> statusFilter;
    
    // Color constants for consistent styling
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(39, 174, 96);
    private static final Color WARNING_COLOR = new Color(243, 156, 18);
    private static final Color ERROR_COLOR = new Color(231, 76, 60);
    
    public MyBillsPanel(User user) {
        this.currentUser = user;
        initializePanel();
        loadMyBills();
        updateSummaryCards();
    }
    
    private void initializePanel() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Header
        add(createHeaderPanel("My Bills - Billing History"), BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Summary panel
        mainPanel.add(createBillsSummaryPanel(), BorderLayout.NORTH);
        
        // Bills table
        mainPanel.add(createBillsTablePanel(), BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createBillsSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Initialize with loading state
        summaryPanel.add(createSummaryCard("Total Bills", "0", PRIMARY_COLOR));
        summaryPanel.add(createSummaryCard("Pending", "0", WARNING_COLOR));
        summaryPanel.add(createSummaryCard("Paid", "0", SUCCESS_COLOR));
        summaryPanel.add(createSummaryCard("Overdue", "0", ERROR_COLOR));
        
        return summaryPanel;
    }
    
    private void updateSummaryCards() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT " +
                        "COUNT(*) as total, " +
                        "SUM(CASE WHEN Status = 'Pending' THEN 1 ELSE 0 END) as pending, " +
                        "SUM(CASE WHEN Status = 'Paid' THEN 1 ELSE 0 END) as paid, " +
                        "SUM(CASE WHEN Status = 'Overdue' THEN 1 ELSE 0 END) as overdue " +
                        "FROM bill WHERE Subscriber = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser.getUsername());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                updateSummaryCard(0, "Total Bills", String.valueOf(rs.getInt("total")), PRIMARY_COLOR);
                updateSummaryCard(1, "Pending", String.valueOf(rs.getInt("pending")), WARNING_COLOR);
                updateSummaryCard(2, "Paid", String.valueOf(rs.getInt("paid")), SUCCESS_COLOR);
                updateSummaryCard(3, "Overdue", String.valueOf(rs.getInt("overdue")), ERROR_COLOR);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading bill summary: " + e.getMessage());
            showError("Unable to load bill summary");
        }
    }
    
    private void updateSummaryCard(int index, String title, String value, Color color) {
        JPanel summaryPanel = (JPanel) ((JPanel) getComponent(1)).getComponent(0);
        JPanel card = (JPanel) summaryPanel.getComponent(index);
        
        JLabel titleLabel = (JLabel) card.getComponent(0);
        JLabel valueLabel = (JLabel) card.getComponent(1);
        
        titleLabel.setText(title);
        valueLabel.setText(value);
        valueLabel.setForeground(color);
        
        // Update the border color
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, color),
            new EmptyBorder(10, 10, 10, 10)
        ));
    }
    
    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(2, 0, 0, 0, color),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.DARK_GRAY);
        
        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valueLabel.setForeground(color);
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }
    
    private JPanel createBillsTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(Color.WHITE);
        
        // Filter panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        statusFilter = new JComboBox<>(new String[]{"All Bills", "Pending", "Paid", "Overdue"});
        statusFilter.setBorder(BorderFactory.createTitledBorder("Filter by Status"));
        
        JButton filterButton = createPrimaryButton("🔍 Apply Filter");
        JButton clearButton = createPrimaryButton("🔄 Show All");
        
        filterButton.addActionListener(this::applyFilter);
        clearButton.addActionListener(e -> loadMyBills());
        
        filterPanel.add(statusFilter);
        filterPanel.add(filterButton);
        filterPanel.add(clearButton);
        
        // Bills table
        String[] columns = {"Bill ID", "Service", "Amount", "Issue Date", "Due Date", "Status", "Actions"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only actions column is editable
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 2: return Double.class; // Amount
                    case 3: case 4: return Date.class; // Dates
                    default: return String.class;
                }
            }
        };
        
        billsTable = new JTable(model);
        billsTable.setRowHeight(40);
        billsTable.setFont(new Font("Arial", Font.PLAIN, 11));
        billsTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        billsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        billsTable.setShowGrid(true);
        billsTable.setGridColor(new Color(240, 240, 240));
        
        // Set column widths
        billsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Bill ID
        billsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Service
        billsTable.getColumnModel().getColumn(2).setPreferredWidth(80);  // Amount
        billsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Issue Date
        billsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Due Date
        billsTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Status
        billsTable.getColumnModel().getColumn(6).setPreferredWidth(120); // Actions
        
        // Custom renderers and editors
        billsTable.getColumnModel().getColumn(2).setCellRenderer(new AmountRenderer());
        billsTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        billsTable.getColumnModel().getColumn(6).setCellRenderer(new ActionRenderer());
        billsTable.getColumnModel().getColumn(6).setCellEditor(new ActionEditor());
        
        JScrollPane scrollPane = new JScrollPane(billsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("My Bills History"));
        
        tablePanel.add(filterPanel, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private void loadMyBills() {
        DefaultTableModel model = (DefaultTableModel) billsTable.getModel();
        model.setRowCount(0);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT BillID, Services, Amount, IssueDate, DueDate, Status, Reference " +
                        "FROM bill WHERE Subscriber = ? ORDER BY IssueDate DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser.getUsername());
            ResultSet rs = stmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            int billCount = 0;
            
            while (rs.next()) {
                String billId = rs.getString("BillID");
                String service = rs.getString("Services");
                double amount = rs.getDouble("Amount");
                Date issueDate = rs.getDate("IssueDate");
                Date dueDate = rs.getDate("DueDate");
                String status = rs.getString("Status");
                String reference = rs.getString("Reference");
                
                model.addRow(new Object[]{billId, service, amount, issueDate, dueDate, status, "VIEW"});
                billCount++;
            }
            
            System.out.println("✅ Loaded " + billCount + " bills for user: " + currentUser.getUsername());
            updateSummaryCards();
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading bills from database: " + e.getMessage());
            JOptionPane.showMessageDialog(this, 
                "Error loading bills: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void applyFilter(ActionEvent e) {
        String filter = (String) statusFilter.getSelectedItem();
        System.out.println("🔍 Applying filter: " + filter);
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT BillID, Services, Amount, IssueDate, DueDate, Status, Reference " +
                        "FROM bill WHERE Subscriber = ?";
            
            if (!"All Bills".equals(filter)) {
                sql += " AND Status = ?";
            }
            sql += " ORDER BY IssueDate DESC";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, currentUser.getUsername());
            
            if (!"All Bills".equals(filter)) {
                stmt.setString(2, filter);
            }
            
            ResultSet rs = stmt.executeQuery();
            DefaultTableModel model = (DefaultTableModel) billsTable.getModel();
            model.setRowCount(0);
            
            int filteredRows = 0;
            while (rs.next()) {
                String billId = rs.getString("BillID");
                String service = rs.getString("Services");
                double amount = rs.getDouble("Amount");
                Date issueDate = rs.getDate("IssueDate");
                Date dueDate = rs.getDate("DueDate");
                String status = rs.getString("Status");
                String reference = rs.getString("Reference");
                
                model.addRow(new Object[]{billId, service, amount, issueDate, dueDate, status, "VIEW"});
                filteredRows++;
            }
            
            JOptionPane.showMessageDialog(this, 
                "<html><b>Filter Applied:</b> " + filter + "<br>" +
                "<b>Showing:</b> " + filteredRows + " bills</html>",
                "Filter Applied", 
                JOptionPane.INFORMATION_MESSAGE);
                
        } catch (SQLException ex) {
            System.err.println("❌ Error applying filter: " + ex.getMessage());
            showError("Error applying filter: " + ex.getMessage());
        }
    }
    
    private void viewBillDetails(String billId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT b.BillID, b.Services, b.Amount, b.IssueDate, b.DueDate, b.Status, b.Reference, " +
                        "s.FullName, s.Email " +
                        "FROM bill b " +
                        "JOIN subscriber s ON b.Subscriber = s.Username " +
                        "WHERE b.BillID = ? AND b.Subscriber = ?";
            
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, billId);
            stmt.setString(2, currentUser.getUsername());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                showBillDetailsDialog(rs);
            } else {
                showError("Bill not found: " + billId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading bill details: " + e.getMessage());
            showError("Error loading bill details: " + e.getMessage());
        }
    }
    
    private void showBillDetailsDialog(ResultSet rs) throws SQLException {
        String billId = rs.getString("BillID");
        
        JDialog dialog = new JDialog();
        dialog.setTitle("Bill Details - " + billId);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel detailsPanel = new JPanel(new GridLayout(9, 2, 10, 10));
        detailsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Bill details from database
        detailsPanel.add(createDetailLabel("Bill ID:"));
        detailsPanel.add(createDetailValue(billId));
        detailsPanel.add(createDetailLabel("Subscriber:"));
        detailsPanel.add(createDetailValue(rs.getString("FullName")));
        detailsPanel.add(createDetailLabel("Email:"));
        detailsPanel.add(createDetailValue(rs.getString("Email")));
        detailsPanel.add(createDetailLabel("Service:"));
        detailsPanel.add(createDetailValue(rs.getString("Services")));
        detailsPanel.add(createDetailLabel("Amount:"));
        detailsPanel.add(createDetailValue(String.format("$%.2f", rs.getDouble("Amount")), SUCCESS_COLOR, Font.BOLD));
        detailsPanel.add(createDetailLabel("Issue Date:"));
        detailsPanel.add(createDetailValue(rs.getDate("IssueDate").toString()));
        detailsPanel.add(createDetailLabel("Due Date:"));
        detailsPanel.add(createDetailValue(rs.getDate("DueDate").toString()));
        detailsPanel.add(createDetailLabel("Status:"));
        detailsPanel.add(createDetailValue(rs.getString("Status"), getStatusColor(rs.getString("Status")), Font.BOLD));
        detailsPanel.add(createDetailLabel("Reference:"));
        detailsPanel.add(createDetailValue(rs.getString("Reference")));
        
        JButton downloadButton = createPrimaryButton("📥 Download PDF");
        JButton printButton = createPrimaryButton("🖨️ Print");
        JButton closeButton = createPrimaryButton("Close");
        
        downloadButton.addActionListener(evt -> {
            System.out.println("📥 Downloading PDF for bill: " + billId);
            JOptionPane.showMessageDialog(dialog, 
                "Bill PDF download started for " + billId + "\n" +
                "Your download will begin shortly...",
                "Download Started",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        printButton.addActionListener(evt -> {
            System.out.println("🖨️ Printing bill: " + billId);
            JOptionPane.showMessageDialog(dialog, 
                "Opening print dialog for " + billId + "\n" +
                "Please select your printer and preferences.",
                "Print Dialog",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        closeButton.addActionListener(evt -> dialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        buttonPanel.add(downloadButton);
        buttonPanel.add(printButton);
        buttonPanel.add(closeButton);
        
        dialog.add(detailsPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void payBill(String billId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT Amount, Services, DueDate, Status FROM bill WHERE BillID = ? AND Subscriber = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, billId);
            stmt.setString(2, currentUser.getUsername());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                double amount = rs.getDouble("Amount");
                String service = rs.getString("Services");
                Date dueDate = rs.getDate("DueDate");
                String status = rs.getString("Status");
                
                showPaymentDialog(billId, amount, service, dueDate, status);
            } else {
                showError("Bill not found: " + billId);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error loading bill for payment: " + e.getMessage());
            showError("Error loading bill: " + e.getMessage());
        }
    }
    
    private void showPaymentDialog(String billId, double amount, String service, Date dueDate, String status) {
        JDialog paymentDialog = new JDialog();
        paymentDialog.setTitle("Pay Bill - " + billId);
        paymentDialog.setModal(true);
        paymentDialog.setLayout(new BorderLayout());
        paymentDialog.setSize(400, 400);
        paymentDialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(7, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Payment form with real data
        formPanel.add(createDetailLabel("Bill Amount:"));
        formPanel.add(createDetailValue(String.format("$%.2f", amount), SUCCESS_COLOR, Font.BOLD));
        formPanel.add(createDetailLabel("Service:"));
        formPanel.add(createDetailValue(service));
        formPanel.add(createDetailLabel("Due Date:"));
        formPanel.add(createDetailValue(dueDate.toString()));
        formPanel.add(createDetailLabel("Payment Method:"));
        JComboBox<String> methodCombo = new JComboBox<>(new String[]{"Credit Card", "Bank Transfer", "Cash", "Mobile Money"});
        formPanel.add(methodCombo);
        formPanel.add(createDetailLabel("Card Number:"));
        JTextField cardField = new JTextField();
        cardField.setText("**** **** **** 1234");
        formPanel.add(cardField);
        formPanel.add(createDetailLabel("Expiry Date:"));
        JTextField expiryField = new JTextField();
        expiryField.setText("12/2025");
        formPanel.add(expiryField);
        formPanel.add(createDetailLabel("CVV:"));
        JTextField cvvField = new JTextField();
        cvvField.setText("***");
        formPanel.add(cvvField);
        
        JButton payButton = createSuccessButton("💳 Pay Now");
        JButton cancelButton = createDangerButton("Cancel");
        
        payButton.addActionListener(evt -> processPayment(billId, amount, (String) methodCombo.getSelectedItem(), paymentDialog));
        cancelButton.addActionListener(evt -> paymentDialog.dispose());
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        buttonPanel.add(payButton);
        buttonPanel.add(cancelButton);
        
        paymentDialog.add(formPanel, BorderLayout.CENTER);
        paymentDialog.add(buttonPanel, BorderLayout.SOUTH);
        paymentDialog.setVisible(true);
    }
    
    private void processPayment(String billId, double amount, String method, JDialog dialog) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Insert payment record
                String paymentSql = "INSERT INTO payment (BillID, Amount, Method, Reference, Status, Subscriber) " +
                                  "VALUES (?, ?, ?, ?, 'Completed', ?)";
                PreparedStatement paymentStmt = conn.prepareStatement(paymentSql);
                paymentStmt.setString(1, billId);
                paymentStmt.setDouble(2, amount);
                paymentStmt.setString(3, method);
                paymentStmt.setString(4, "PAY-" + System.currentTimeMillis());
                paymentStmt.setString(5, currentUser.getUsername());
                paymentStmt.executeUpdate();
                
                // Update bill status
                String billSql = "UPDATE bill SET Status = 'Paid' WHERE BillID = ? AND Subscriber = ?";
                PreparedStatement billStmt = conn.prepareStatement(billSql);
                billStmt.setString(1, billId);
                billStmt.setString(2, currentUser.getUsername());
                billStmt.executeUpdate();
                
                conn.commit();
                
                String transactionId = "TXN-" + System.currentTimeMillis();
                JOptionPane.showMessageDialog(dialog, 
                    "<html><h3>Payment Successful!</h3>" +
                    "<p><b>Bill:</b> " + billId + "</p>" +
                    "<p><b>Amount:</b> $" + String.format("%.2f", amount) + "</p>" +
                    "<p><b>Transaction ID:</b> " + transactionId + "</p>" +
                    "<p><b>Method:</b> " + method + "</p>" +
                    "<p>Thank you for your payment!</p></html>",
                    "Payment Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
                dialog.dispose();
                loadMyBills(); // Refresh the table
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error processing payment: " + e.getMessage());
            showError("Payment failed: " + e.getMessage());
        }
    }
    
    protected Color getStatusColor(String status) {
        switch (status) {
            case "Paid": return SUCCESS_COLOR;
            case "Pending": return WARNING_COLOR;
            case "Overdue": return ERROR_COLOR;
            default: return Color.BLACK;
        }
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    // Utility methods for creating styled components
    private JLabel createDetailLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setForeground(Color.DARK_GRAY);
        return label;
    }
    
    private JLabel createDetailValue(String text) {
        return createDetailValue(text, Color.BLACK, Font.PLAIN);
    }
    
    private JLabel createDetailValue(String text, Color color, int style) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", style, 12));
        label.setForeground(color);
        return label;
    }
    
    // Custom cell renderers
    private class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                setText(String.format("$%.2f", (Double) value));
                setForeground(new Color(0, 100, 0));
                setFont(getFont().deriveFont(Font.BOLD));
            }
            
            super.setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(new EmptyBorder(0, 5, 0, 5));
            return this;
        }
    }
    
    private class StatusRenderer extends DefaultTableCellRenderer {
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
                        break;
                    case "Pending":
                        setBackground(new Color(255, 255, 200));
                        setForeground(new Color(150, 150, 0));
                        break;
                    case "Overdue":
                        setBackground(new Color(255, 200, 200));
                        setForeground(new Color(150, 0, 0));
                        break;
                    default:
                        setBackground(table.getBackground());
                        setForeground(table.getForeground());
                }
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            super.setHorizontalAlignment(SwingConstants.CENTER);
            setBorder(new EmptyBorder(0, 5, 0, 5));
            return this;
        }
    }
    
    // Action renderer and editor
    private class ActionRenderer extends JPanel implements TableCellRenderer {
        private JButton viewButton;
        private JButton payButton;
        
        public ActionRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
            setBackground(Color.WHITE);
            
            viewButton = createActionButton("View", PRIMARY_COLOR);
            payButton = createActionButton("Pay", SUCCESS_COLOR);
            
            add(viewButton);
            add(payButton);
        }
        
        private JButton createActionButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 10));
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                new EmptyBorder(5, 10, 5, 10)
            ));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            String status = (String) table.getValueAt(row, 5);
            viewButton.setVisible(true);
            payButton.setVisible("Pending".equals(status) || "Overdue".equals(status));
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(Color.WHITE);
            }
            
            return this;
        }
    }
    
    private class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private JPanel panel;
        private JButton viewButton;
        private JButton payButton;
        private String currentBillId;
        
        public ActionEditor() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
            panel.setBackground(Color.WHITE);
            
            viewButton = createActionButton("View", PRIMARY_COLOR);
            payButton = createActionButton("Pay", SUCCESS_COLOR);
            
            viewButton.addActionListener(e -> {
                fireEditingStopped();
                viewBillDetails(currentBillId);
            });
            
            payButton.addActionListener(e -> {
                fireEditingStopped();
                payBill(currentBillId);
            });
            
            panel.add(viewButton);
            panel.add(payButton);
        }
        
        private JButton createActionButton(String text, Color color) {
            JButton button = new JButton(text);
            button.setFont(new Font("Arial", Font.PLAIN, 10));
            button.setBackground(color);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color.darker()),
                new EmptyBorder(5, 10, 5, 10)
            ));
            button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            return button;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            currentBillId = (String) table.getValueAt(row, 0);
            String status = (String) table.getValueAt(row, 5);
            
            viewButton.setVisible(true);
            payButton.setVisible("Pending".equals(status) || "Overdue".equals(status));
            
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(Color.WHITE);
            }
            
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "VIEW";
        }
    }
}
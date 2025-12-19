package com.panels;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class JDatePicker extends JPanel {
    private JTextField dateField;
    private SimpleDateFormat dateFormat;
    private Color borderColor = Color.GRAY;
    private Color errorBorderColor = Color.RED;
    private boolean required = false;

    public JDatePicker() {
        this("yyyy-MM-dd");
    }

    public JDatePicker(String format) {
        dateFormat = new SimpleDateFormat(format);
        initializeComponent();
    }

    public JDatePicker(String format, boolean required) {
        this(format);
        this.required = required;
    }

    private void initializeComponent() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        updateBorder();

        dateField = new JTextField(10);
        dateField.setText(dateFormat.format(new Date()));
        dateField.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 2));
        
        // Add focus listener for validation feedback
        dateField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                validateDateField();
            }
        });

        JButton calendarButton = createCalendarButton();
        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
    }

    private JButton createCalendarButton() {
        JButton calendarButton = new JButton("📅");
        calendarButton.setPreferredSize(new Dimension(30, 25));
        calendarButton.setFocusPainted(false);
        calendarButton.setBackground(new Color(240, 240, 240));
        calendarButton.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        calendarButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        calendarButton.addActionListener(e -> showCalendarDialog());
        return calendarButton;
    }

    private void showCalendarDialog() {
        // Create a simple calendar dialog
        JDialog calendarDialog = new JDialog((Frame) null, "Select Date", true);
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.setSize(300, 300);
        calendarDialog.setLocationRelativeTo(this);

        // Calendar panel
        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 2, 2));
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Get current date
        Calendar calendar = Calendar.getInstance();
        try {
            Date currentDate = dateFormat.parse(dateField.getText());
            calendar.setTime(currentDate);
        } catch (ParseException e) {
            calendar.setTime(new Date());
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Month and year selector
        JPanel headerPanel = new JPanel(new BorderLayout());
        JButton prevMonth = new JButton("◀");
        JButton nextMonth = new JButton("▶");
        JLabel monthLabel = new JLabel(getMonthYearString(month, year), JLabel.CENTER);
        
        prevMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, -1);
            updateCalendar(calendarPanel, calendar, monthLabel);
        });
        
        nextMonth.addActionListener(e -> {
            calendar.add(Calendar.MONTH, 1);
            updateCalendar(calendarPanel, calendar, monthLabel);
        });

        headerPanel.add(prevMonth, BorderLayout.WEST);
        headerPanel.add(monthLabel, BorderLayout.CENTER);
        headerPanel.add(nextMonth, BorderLayout.EAST);

        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : days) {
            JLabel dayLabel = new JLabel(dayName, JLabel.CENTER);
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
            calendarPanel.add(dayLabel);
        }

        updateCalendar(calendarPanel, calendar, monthLabel);

        // Today button
        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(e -> {
            dateField.setText(dateFormat.format(new Date()));
            calendarDialog.dispose();
        });

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(todayButton);

        calendarDialog.add(headerPanel, BorderLayout.NORTH);
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.add(buttonPanel, BorderLayout.SOUTH);
        calendarDialog.setVisible(true);
    }

    private void updateCalendar(JPanel calendarPanel, Calendar calendar, JLabel monthLabel) {
        calendarPanel.removeAll();
        
        // Day headers
        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : days) {
            JLabel dayLabel = new JLabel(dayName, JLabel.CENTER);
            dayLabel.setFont(dayLabel.getFont().deriveFont(Font.BOLD));
            calendarPanel.add(dayLabel);
        }

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        
        monthLabel.setText(getMonthYearString(month, year));

        // Fill in the days
        Calendar tempCal = (Calendar) calendar.clone();
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        
        int firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Empty cells before first day
        for (int i = 1; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }

        // Day buttons
        for (int day = 1; day <= daysInMonth; day++) {
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setFocusPainted(false);
            final int selectedDay = day;
            
            dayButton.addActionListener(e -> {
                calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                dateField.setText(dateFormat.format(calendar.getTime()));
                ((Window) getTopLevelAncestor()).dispose();
            });
            
            calendarPanel.add(dayButton);
        }

        calendarPanel.revalidate();
        calendarPanel.repaint();
    }

    private String getMonthYearString(int month, int year) {
        String[] months = {"January", "February", "March", "April", "May", "June", 
                          "July", "August", "September", "October", "November", "December"};
        return months[month] + " " + year;
    }

    // VALIDATION METHODS
    public boolean isValidDate() {
        String dateText = dateField.getText().trim();
        
        if (required && dateText.isEmpty()) {
            setErrorState(true);
            return false;
        }
        
        if (dateText.isEmpty()) {
            setErrorState(false);
            return true; // Empty is valid for optional fields
        }
        
        try {
            dateFormat.setLenient(false); // Strict parsing
            Date date = dateFormat.parse(dateText);
            
            // Additional validation: check if date is not in the future (for meter readings)
            if (date.after(new Date())) {
                setErrorState(true);
                return false;
            }
            
            setErrorState(false);
            return true;
        } catch (ParseException e) {
            setErrorState(true);
            return false;
        }
    }

    private void validateDateField() {
        isValidDate(); // This will update the border color
    }

    private void setErrorState(boolean hasError) {
        borderColor = hasError ? errorBorderColor : Color.GRAY;
        updateBorder();
        
        if (hasError) {
            dateField.setToolTipText("Please enter a valid date in format: " + dateFormat.toPattern());
        } else {
            dateField.setToolTipText(null);
        }
    }

    private void updateBorder() {
        setBorder(BorderFactory.createLineBorder(borderColor));
    }

    // GETTER AND SETTER METHODS
    public String getDate() {
        return dateField.getText().trim();
    }

    public Date getDateAsDate() {
        try {
            return dateFormat.parse(dateField.getText().trim());
        } catch (ParseException e) {
            return null;
        }
    }

    public void setDate(String date) {
        dateField.setText(date);
        validateDateField();
    }

    public void setDate(Date date) {
        if (date != null) {
            dateField.setText(dateFormat.format(date));
        } else {
            dateField.setText("");
        }
        validateDateField();
    }

    public void clear() {
        dateField.setText("");
        setErrorState(false);
    }

    public void setRequired(boolean required) {
        this.required = required;
        validateDateField();
    }

    public boolean isRequired() {
        return required;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        dateField.setEnabled(enabled);
        Component[] components = getComponents();
        for (Component comp : components) {
            if (comp instanceof JButton) {
                comp.setEnabled(enabled);
            }
        }
    }

    // Utility method to get date in SQL format
    public String getDateForSQL() {
        if (isValidDate()) {
            return getDate();
        }
        return null;
    }

    // Static utility methods
    public static boolean isValidDateString(String dateString, String format) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            sdf.setLenient(false);
            sdf.parse(dateString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static String formatDateForDisplay(Date date) {
        return new SimpleDateFormat("MMM dd, yyyy").format(date);
    }
}
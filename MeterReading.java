package com.dao;

import java.sql.Timestamp;

public class MeterReading {
    private int id;
    private String subscriber;
    private String service;
    private double reading;
    private String unit;
    private Timestamp date;
    private String type;
    private String status;
    private int consumption;
    
    // Default constructor
    public MeterReading() {}
    
    // Full constructor
    public MeterReading(int id, String subscriber, String service, double reading, 
                       String unit, Timestamp date, String type, String status, int consumption) {
        this.id = id;
        this.subscriber = subscriber;
        this.service = service;
        this.reading = reading;
        this.unit = unit;
        this.date = date;
        this.type = type;
        this.status = status;
        this.consumption = consumption;
    }
    
    // Constructor without ID (for new readings)
    public MeterReading(String subscriber, String service, double reading, 
                       String unit, Timestamp date, String type, String status, int consumption) {
        this(0, subscriber, service, reading, unit, date, type, status, consumption);
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getSubscriber() { return subscriber; }
    public void setSubscriber(String subscriber) { this.subscriber = subscriber; }
    
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    
    public double getReading() { return reading; }
    public void setReading(double reading) { this.reading = reading; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public int getConsumption() { return consumption; }
    public void setConsumption(int consumption) { this.consumption = consumption; }
    
    // Utility methods
    public boolean isVerified() {
        return "Verified".equalsIgnoreCase(status);
    }
    
    public boolean isPending() {
        return "Pending".equalsIgnoreCase(status);
    }
    
    public boolean isOverdue() {
        return "Overdue".equalsIgnoreCase(status);
    }
    
    // Validation methods
    public boolean isValid() {
        return subscriber != null && !subscriber.trim().isEmpty() &&
               service != null && !service.trim().isEmpty() &&
               reading >= 0 &&
               date != null;
    }
    
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (subscriber == null || subscriber.trim().isEmpty()) {
            errors.append("Subscriber is required. ");
        }
        
        if (service == null || service.trim().isEmpty()) {
            errors.append("Service is required. ");
        }
        
        if (reading < 0) {
            errors.append("Reading must be non-negative. ");
        }
        
        if (date == null) {
            errors.append("Date is required. ");
        }
        
        return errors.toString().trim();
    }
    
    @Override
    public String toString() {
        return String.format(
            "MeterReading[id=%d, subscriber='%s', service='%s', reading=%.2f, unit='%s', " +
            "date=%s, type='%s', status='%s', consumption=%d]",
            id, subscriber, service, reading, unit, date, type, status, consumption
        );
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        MeterReading that = (MeterReading) o;
        
        if (id != that.id) return false;
        if (Double.compare(that.reading, reading) != 0) return false;
        if (consumption != that.consumption) return false;
        if (!subscriber.equals(that.subscriber)) return false;
        if (!service.equals(that.service)) return false;
        if (unit != null ? !unit.equals(that.unit) : that.unit != null) return false;
        if (!date.equals(that.date)) return false;
        if (!type.equals(that.type)) return false;
        return status.equals(that.status);
    }
    
    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + subscriber.hashCode();
        result = 31 * result + service.hashCode();
        temp = Double.doubleToLongBits(reading);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        result = 31 * result + date.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + status.hashCode();
        result = 31 * result + consumption;
        return result;
    }
    
    // Builder pattern for fluent creation
    public static class Builder {
        private int id;
        private String subscriber;
        private String service;
        private double reading;
        private String unit;
        private Timestamp date;
        private String type = "Current";
        private String status = "Pending";
        private int consumption;
        
        public Builder subscriber(String subscriber) {
            this.subscriber = subscriber;
            return this;
        }
        
        public Builder service(String service) {
            this.service = service;
            return this;
        }
        
        public Builder reading(double reading) {
            this.reading = reading;
            return this;
        }
        
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public Builder date(Timestamp date) {
            this.date = date;
            return this;
        }
        
        public Builder type(String type) {
            this.type = type;
            return this;
        }
        
        public Builder status(String status) {
            this.status = status;
            return this;
        }
        
        public Builder consumption(int consumption) {
            this.consumption = consumption;
            return this;
        }
        
        public Builder id(int id) {
            this.id = id;
            return this;
        }
        
        public MeterReading build() {
            return new MeterReading(id, subscriber, service, reading, unit, date, type, status, consumption);
        }
    }
    
    // Static factory methods
    public static Builder builder() {
        return new Builder();
    }
    
    public static MeterReading createCurrentReading(String subscriber, String service, double reading, String unit) {
        return new Builder()
            .subscriber(subscriber)
            .service(service)
            .reading(reading)
            .unit(unit)
            .date(new Timestamp(System.currentTimeMillis()))
            .type("Current")
            .status("Pending")
            .build();
    }
    
    public static MeterReading createPreviousReading(String subscriber, String service, double reading, String unit, Timestamp date) {
        return new Builder()
            .subscriber(subscriber)
            .service(service)
            .reading(reading)
            .unit(unit)
            .date(date)
            .type("Previous")
            .status("Verified")
            .build();
    }
}
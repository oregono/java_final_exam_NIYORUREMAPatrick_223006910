package com.dao;

import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private String billId;
    private double amount;
    private Timestamp date;
    private String method;
    private String reference;
    private String status;
    private String subscriber;

    // Private constructor for builder
    private Payment(Builder builder) {
        this.paymentId = builder.paymentId;
        this.billId = builder.billId;
        this.amount = builder.amount;
        this.date = builder.date;
        this.method = builder.method;
        this.reference = builder.reference;
        this.status = builder.status;
        this.subscriber = builder.subscriber;
    }

    // Getters
    public int getPaymentId() { return paymentId; }
    public String getBillId() { return billId; }
    public double getAmount() { return amount; }
    public Timestamp getDate() { return date; }
    public String getMethod() { return method; }
    public String getReference() { return reference; }
    public String getStatus() { return status; }
    public String getSubscriber() { return subscriber; }

    // Builder class
    public static class Builder {
        private int paymentId;
        private String billId;
        private double amount;
        private Timestamp date;
        private String method;
        private String reference;
        private String status;
        private String subscriber;

        public Builder paymentId(int paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Builder billId(String billId) {
            this.billId = billId;
            return this;
        }

        public Builder amount(double amount) {
            this.amount = amount;
            return this;
        }

        public Builder date(Timestamp date) {
            this.date = date;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder subscriber(String subscriber) {
            this.subscriber = subscriber;
            return this;
        }

        public Payment build() {
            return new Payment(this);
        }
    }

    // Static builder method
    public static Builder builder() {
        return new Builder();
    }

    // Validation method
    public boolean isValid() {
        return billId != null && !billId.trim().isEmpty() &&
               amount > 0 &&
               method != null && !method.trim().isEmpty() &&
               reference != null && !reference.trim().isEmpty() &&
               status != null && !status.trim().isEmpty() &&
               subscriber != null && !subscriber.trim().isEmpty();
    }

    // Validation errors method
    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (billId == null || billId.trim().isEmpty()) {
            errors.append("Bill ID is required. ");
        }
        if (amount <= 0) {
            errors.append("Amount must be greater than 0. ");
        }
        if (method == null || method.trim().isEmpty()) {
            errors.append("Payment method is required. ");
        }
        if (reference == null || reference.trim().isEmpty()) {
            errors.append("Reference is required. ");
        }
        if (status == null || status.trim().isEmpty()) {
            errors.append("Status is required. ");
        }
        if (subscriber == null || subscriber.trim().isEmpty()) {
            errors.append("Subscriber is required. ");
        }
        
        return errors.toString().trim();
    }

    @Override
    public String toString() {
        return String.format(
            "Payment{paymentId=%d, billId='%s', amount=%.2f, date=%s, method='%s', reference='%s', status='%s', subscriber='%s'}",
            paymentId, billId, amount, date, method, reference, status, subscriber
        );
    }
}
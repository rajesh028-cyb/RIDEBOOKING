package com.ridebooking.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Payment model representing fare and payment transactions in the Ride Booking System.
 */
public class Payment {
    private int paymentId;
    private int tripId;
    private int riderId;
    private int driverId;
    private double amount;
    private LocalDateTime paymentDate;
    private String paymentMethod; // Cash, UPI, Wallet
    private String paymentStatus; // Paid, Failed, Pending
    private double driverEarnings;
    private double platformEarnings;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Standard Payment Methods
    public static final String METHOD_CASH = "Cash";
    public static final String METHOD_UPI = "UPI";
    public static final String METHOD_WALLET = "Wallet";

    // Standard Payment Statuses
    public static final String STATUS_PAID = "Paid";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_PENDING = "Pending";

    /**
     * Full constructor for Payment with explicit earnings and date.
     */
    public Payment(int paymentId, int tripId, int riderId, int driverId, double amount,
                   LocalDateTime paymentDate, String paymentMethod, String paymentStatus,
                   double driverEarnings, double platformEarnings) {
        this.paymentId = paymentId;
        this.tripId = tripId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.amount = amount;
        this.paymentDate = (paymentDate != null) ? paymentDate : LocalDateTime.now();
        this.paymentMethod = normalizeMethod(paymentMethod);
        this.paymentStatus = normalizeStatus(paymentStatus);
        this.driverEarnings = driverEarnings;
        this.platformEarnings = platformEarnings;
    }

    /**
     * Standard constructor that auto-calculates driver and platform earnings and sets timestamp to now.
     */
    public Payment(int paymentId, int tripId, int riderId, int driverId, double amount,
                   String paymentMethod, String paymentStatus) {
        this.paymentId = paymentId;
        this.tripId = tripId;
        this.riderId = riderId;
        this.driverId = driverId;
        this.amount = amount;
        this.paymentDate = LocalDateTime.now();
        this.paymentMethod = normalizeMethod(paymentMethod);
        this.paymentStatus = normalizeStatus(paymentStatus);
        recalculateEarnings();
    }

    /**
     * Backward-compatible constructor for existing service integration.
     */
    public Payment(int paymentId, int tripId, double amount, String paymentStatus) {
        this(paymentId, tripId, 0, 0, amount, METHOD_CASH, paymentStatus);
    }

    /**
     * Recalculates revenue distribution based on standard 80% driver / 20% platform share.
     */
    public void recalculateEarnings() {
        if (STATUS_PAID.equalsIgnoreCase(this.paymentStatus) || STATUS_PENDING.equalsIgnoreCase(this.paymentStatus)) {
            this.driverEarnings = Math.round(this.amount * 0.80 * 100.0) / 100.0;
            this.platformEarnings = Math.round(this.amount * 0.20 * 100.0) / 100.0;
        } else {
            // Failed payment generates no earnings
            this.driverEarnings = 0.0;
            this.platformEarnings = 0.0;
        }
    }

    private static String normalizeMethod(String method) {
        if (method == null) return METHOD_CASH;
        String m = method.trim();
        if (m.equalsIgnoreCase("cash")) return METHOD_CASH;
        if (m.equalsIgnoreCase("upi")) return METHOD_UPI;
        if (m.equalsIgnoreCase("wallet")) return METHOD_WALLET;
        return m;
    }

    private static String normalizeStatus(String status) {
        if (status == null) return STATUS_PENDING;
        String s = status.trim();
        if (s.equalsIgnoreCase("paid")) return STATUS_PAID;
        if (s.equalsIgnoreCase("failed")) return STATUS_FAILED;
        if (s.equalsIgnoreCase("pending")) return STATUS_PENDING;
        return s;
    }

    // Getters and Setters
    public int getPaymentId() {
        return paymentId;
    }

    public int getId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getRiderId() {
        return riderId;
    }

    public void setRiderId(int riderId) {
        this.riderId = riderId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
        recalculateEarnings();
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getFormattedPaymentDate() {
        return (paymentDate != null) ? paymentDate.format(FORMATTER) : "N/A";
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = normalizeMethod(paymentMethod);
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = normalizeStatus(paymentStatus);
        recalculateEarnings();
    }

    public double getDriverEarnings() {
        return driverEarnings;
    }

    public void setDriverEarnings(double driverEarnings) {
        this.driverEarnings = driverEarnings;
    }

    public double getPlatformEarnings() {
        return platformEarnings;
    }

    public void setPlatformEarnings(double platformEarnings) {
        this.platformEarnings = platformEarnings;
    }

    public boolean isPaid() {
        return STATUS_PAID.equalsIgnoreCase(this.paymentStatus);
    }

    public boolean isFailed() {
        return STATUS_FAILED.equalsIgnoreCase(this.paymentStatus);
    }

    public boolean isPending() {
        return STATUS_PENDING.equalsIgnoreCase(this.paymentStatus);
    }

    @Override
    public String toString() {
        return String.format(
            "Payment [ID: %d | Trip ID: %d | Rider ID: %d | Driver ID: %d | Amount: ₹%.2f | Date: %s | Method: %s | Status: %s | Driver Earnings: ₹%.2f | Platform Earnings: ₹%.2f]",
            paymentId, tripId, riderId, driverId, amount, getFormattedPaymentDate(), paymentMethod, paymentStatus, driverEarnings, platformEarnings
        );
    }
}

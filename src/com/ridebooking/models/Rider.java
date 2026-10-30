package com.ridebooking.models;

/**
 * Represents a rider in the ride booking system.
 */
public class Rider {
    private int riderId;
    private String name;
    private String phoneNumber;
    private String email;
    private double averageRating;
    private boolean failedPayment;
    private boolean flaggedForAdminReview;

    public Rider(int riderId, String name, String phoneNumber, String email) {
        this.riderId = riderId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.averageRating = 0.0;
        this.failedPayment = false;
        this.flaggedForAdminReview = false;
    }

    public Rider(int riderId, String name, String phoneNumber, String email,
                 double averageRating, boolean failedPayment, boolean flaggedForAdminReview) {
        this.riderId = riderId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.averageRating = averageRating;
        this.failedPayment = failedPayment;
        this.flaggedForAdminReview = flaggedForAdminReview;
    }

    public int getRiderId() {
        return riderId;
    }

    public void setRiderId(int riderId) {
        this.riderId = riderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(double averageRating) {
        this.averageRating = averageRating;
    }

    public boolean hasFailedPayment() {
        return failedPayment;
    }

    public boolean isFailedPayment() {
        return failedPayment;
    }

    public void setFailedPayment(boolean failedPayment) {
        this.failedPayment = failedPayment;
    }

    public boolean isFlaggedForAdminReview() {
        return flaggedForAdminReview;
    }

    public void setFlaggedForAdminReview(boolean flaggedForAdminReview) {
        this.flaggedForAdminReview = flaggedForAdminReview;
    }

    @Override
    public String toString() {
        return "Rider{"
                + "riderId=" + riderId
                + ", name='" + name + '\''
                + ", phoneNumber='" + phoneNumber + '\''
                + ", email='" + email + '\''
                + ", averageRating=" + averageRating
                + ", failedPayment=" + failedPayment
                + ", flaggedForAdminReview=" + flaggedForAdminReview
                + '}';
    }
}


package com.ridebooking.models;

public class Driver {

    public enum VehicleType {
        BIKE, AUTO, MINI, SEDAN, SUV
    }

    public enum DocumentStatus {
        PENDING, APPROVED, REJECTED
    }

    public enum AvailabilityStatus {
        AVAILABLE, BUSY, OFFLINE, SUSPENDED
    }

    private int driverId;
    private String name;
    private String phoneNumber;
    private String licenceNumber;

    private String vehicleRegistrationNumber;
    private VehicleType vehicleType;
    private int seatingCapacity;

    private DocumentStatus documentStatus;
    private AvailabilityStatus availabilityStatus;

    private double averageRating;
    private int completedRides;
    private int dailyCancellationCount;

    private java.time.LocalDate cancellationDate;

    public Driver(int driverId, String name, String phoneNumber,
                  String licenceNumber, String vehicleRegistrationNumber,
                  VehicleType vehicleType, int seatingCapacity) {

        this.driverId = driverId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.licenceNumber = licenceNumber;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.vehicleType = vehicleType;
        this.seatingCapacity = seatingCapacity;

        this.documentStatus = DocumentStatus.PENDING;
        this.availabilityStatus = AvailabilityStatus.OFFLINE;

        this.averageRating = 0.0;
        this.completedRides = 0;
        this.dailyCancellationCount = 0;
        this.cancellationDate = java.time.LocalDate.now();
    }

    public int getDriverId() {
        return driverId;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getLicenceNumber() {
        return licenceNumber;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    public DocumentStatus getDocumentStatus() {
        return documentStatus;
    }

    public AvailabilityStatus getAvailabilityStatus() {
        return availabilityStatus;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public int getCompletedRides() {
        return completedRides;
    }

    public int getDailyCancellationCount() {
        resetDailyCancellationsIfNeeded();
        return dailyCancellationCount;
    }

    public void updateDetails(String name, String phoneNumber,
                              String licenceNumber,
                              String vehicleRegistrationNumber,
                              VehicleType vehicleType, int seatingCapacity) {

        this.name = name;
        this.phoneNumber = phoneNumber;
        this.licenceNumber = licenceNumber;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.vehicleType = vehicleType;
        this.seatingCapacity = seatingCapacity;
    }

    public void setDocumentStatus(DocumentStatus status) {
        this.documentStatus = status;
    }

    public void setAvailabilityStatus(AvailabilityStatus status) {
        this.availabilityStatus = status;
    }

    public void updateRating(double newRating) {
        completedRides++;
        averageRating =
            ((averageRating * (completedRides - 1)) + newRating)
            / completedRides;
    }

    public void recordCancellation() {
        resetDailyCancellationsIfNeeded();
        dailyCancellationCount++;
    }

    private void resetDailyCancellationsIfNeeded() {
        java.time.LocalDate today = java.time.LocalDate.now();

        if (!today.equals(cancellationDate)) {
            dailyCancellationCount = 0;
            cancellationDate = today;
        }
    }
}

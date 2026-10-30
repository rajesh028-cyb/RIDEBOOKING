package com.ridebooking.service;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Driver.AvailabilityStatus;
import com.ridebooking.models.Driver.DocumentStatus;
import com.ridebooking.models.Driver.VehicleType;
import com.ridebooking.repository.DriverRepository;
import java.util.List;

public class DriverService {

    private final DriverRepository repository;
    private int nextDriverId = 1;

    public DriverService(DriverRepository repository) {
        this.repository = repository;
    }

    // Register a new driver
    public Driver registerDriver(String name, String phoneNumber,
                                 String licenceNumber,
                                 String vehicleRegistrationNumber,
                                 VehicleType vehicleType,
                                 int seatingCapacity) {

        validateText(name, "Name");
        validatePhoneNumber(phoneNumber);
        validateText(licenceNumber, "Licence number");
        validateText(vehicleRegistrationNumber, "Vehicle registration number");

        if (!repository.isPhoneNumberUnique(phoneNumber)) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        if (!repository.isLicenceNumberUnique(licenceNumber)) {
            throw new IllegalArgumentException("Licence number already registered");
        }

        validateVehicle(vehicleType, seatingCapacity);

        Driver driver = new Driver(
            nextDriverId++,
            name.trim(),
            phoneNumber.trim(),
            licenceNumber.trim(),
            vehicleRegistrationNumber.trim(),
            vehicleType,
            seatingCapacity
        );

        repository.saveDriver(driver);
        return driver;
    }

    private void validateText(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
    }

    private void validatePhoneNumber(String phoneNumber) {
        validateText(phoneNumber, "Phone number");

        if (!phoneNumber.matches("\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException("Invalid phone number");
        }
    }

    private void validateVehicle(VehicleType type, int capacity) {
        if (type == null) {
            throw new IllegalArgumentException("Vehicle type is required");
        }

        int expectedCapacity;

        switch (type) {
            case BIKE:
                expectedCapacity = 1;
                break;
            case AUTO:
                expectedCapacity = 3;
                break;
            case MINI:
            case SEDAN:
                expectedCapacity = 4;
                break;
            case SUV:
                expectedCapacity = 6;
                break;
            default:
                throw new IllegalArgumentException("Invalid vehicle type");
        }

        if (capacity != expectedCapacity) {
            throw new IllegalArgumentException(
                type + " must have seating capacity " + expectedCapacity
            );
        }
    }

    // Find driver
    public Driver getDriverById(int driverId) {
        Driver driver = repository.findDriverById(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver not found");
        }

        return driver;
    }

    public Driver getDriverByPhoneNumber(String phoneNumber) {
        Driver driver = repository.findDriverByPhoneNumber(phoneNumber);

        if (driver == null) {
            throw new IllegalArgumentException("Driver not found");
        }

        return driver;
    }

    public List<Driver> getAllDrivers() {
        return repository.getAllDrivers();
    }

    public List<Driver> getAvailableDrivers() {
        return repository.findAvailableDrivers();
    }

    // Approve or reject documents
    public void updateDocumentStatus(int driverId, DocumentStatus status) {
        Driver driver = getDriverById(driverId);

        if (status == null) {
            throw new IllegalArgumentException("Document status is required");
        }

        driver.setDocumentStatus(status);

        if (status != DocumentStatus.APPROVED) {
            driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        }
    }

    // Update driver availability
    public void updateAvailability(int driverId, AvailabilityStatus status) {
        Driver driver = getDriverById(driverId);

        if (status == null) {
            throw new IllegalArgumentException("Availability status is required");
        }

        if (status == AvailabilityStatus.AVAILABLE) {
            checkCanAcceptRide(driver);
        }

        if (driver.getAvailabilityStatus() == AvailabilityStatus.SUSPENDED
                && status != AvailabilityStatus.SUSPENDED
                && driver.getAverageRating() < 3.0
                && driver.getCompletedRides() >= 20) {
            throw new IllegalArgumentException("Driver is suspended due to low rating");
        }

        driver.setAvailabilityStatus(status);
    }

    // Check whether driver can accept a ride
    public void checkCanAcceptRide(Driver driver) {
        if (driver.getDocumentStatus() != DocumentStatus.APPROVED) {
            throw new IllegalArgumentException("Driver documents are not approved");
        }

        if (driver.getAvailabilityStatus() == AvailabilityStatus.SUSPENDED) {
            throw new IllegalArgumentException("Driver is suspended");
        }

        if (driver.getDailyCancellationCount() >= 3) {
            throw new IllegalArgumentException(
                "Daily cancellation limit reached"
            );
        }

        if (driver.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new IllegalArgumentException("Driver is not available");
        }
    }

    // Called by RideService when assigning a ride
    public Driver assignDriverForRide(int driverId) {
        Driver driver = getDriverById(driverId);

        checkCanAcceptRide(driver);

        driver.setAvailabilityStatus(AvailabilityStatus.BUSY);

        return driver;
    }

    // Called by RideService when a ride is completed
    public void completeRide(int driverId) {
        Driver driver = getDriverById(driverId);

        if (driver.getAvailabilityStatus() != AvailabilityStatus.BUSY) {
            throw new IllegalArgumentException("Driver has no active ride");
        }

        driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
    }

    // Called by RideService when a driver cancels a ride
    public void recordDriverCancellation(int driverId) {
        Driver driver = getDriverById(driverId);

        if (driver.getAvailabilityStatus() != AvailabilityStatus.BUSY) {
            throw new IllegalArgumentException("Driver has no active ride");
        }

        driver.recordCancellation();

        if (driver.getDailyCancellationCount() >= 3) {
            driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        } else {
            driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        }
    }

    // Called by RatingService after a completed ride
    public void updateDriverRating(int driverId, double rating) {
        Driver driver = getDriverById(driverId);

        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        driver.updateRating(rating);

        if (driver.getCompletedRides() >= 20
                && driver.getAverageRating() < 3.0) {
            driver.setAvailabilityStatus(AvailabilityStatus.SUSPENDED);
        }
    }

    // Update driver and vehicle details
    public void updateDriverDetails(int driverId, String name,
                                    String phoneNumber,
                                    String licenceNumber,
                                    String registrationNumber,
                                    VehicleType vehicleType,
                                    int seatingCapacity) {

        Driver driver = getDriverById(driverId);

        validateText(name, "Name");
        validatePhoneNumber(phoneNumber);
        validateText(licenceNumber, "Licence number");
        validateText(registrationNumber, "Vehicle registration number");
        validateVehicle(vehicleType, seatingCapacity);

        Driver existingPhone = repository.findDriverByPhoneNumber(phoneNumber);

        if (existingPhone != null && existingPhone.getDriverId() != driverId) {
            throw new IllegalArgumentException("Phone number already registered");
        }

        for (Driver existing : repository.getAllDrivers()) {
            if (existing.getDriverId() != driverId
                    && existing.getLicenceNumber().equalsIgnoreCase(licenceNumber)) {
                throw new IllegalArgumentException("Licence number already registered");
            }
        }

        driver.updateDetails(
            name.trim(),
            phoneNumber.trim(),
            licenceNumber.trim(),
            registrationNumber.trim(),
            vehicleType,
            seatingCapacity
        );

        repository.updateDriver(driver);
    }
}

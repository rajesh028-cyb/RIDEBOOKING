package com.ridebooking.repository;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Driver.AvailabilityStatus;
import java.util.ArrayList;
import java.util.List;

public class DriverRepository {

    private final ArrayList<Driver> drivers = new ArrayList<>();

    public void saveDriver(Driver driver) {
        drivers.add(driver);
    }

    public Driver findDriverById(int driverId) {
        for (Driver driver : drivers) {
            if (driver.getDriverId() == driverId) {
                return driver;
            }
        }
        return null;
    }

    public Driver findDriverByPhoneNumber(String phoneNumber) {
        for (Driver driver : drivers) {
            if (driver.getPhoneNumber().equalsIgnoreCase(phoneNumber)) {
                return driver;
            }
        }
        return null;
    }

    public boolean isPhoneNumberUnique(String phoneNumber) {
        return findDriverByPhoneNumber(phoneNumber) == null;
    }

    public boolean isLicenceNumberUnique(String licenceNumber) {
        for (Driver driver : drivers) {
            if (driver.getLicenceNumber().equalsIgnoreCase(licenceNumber)) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<Driver> findAvailableDrivers() {
        ArrayList<Driver> availableDrivers = new ArrayList<>();

        for (Driver driver : drivers) {
            if (driver.getAvailabilityStatus() == AvailabilityStatus.AVAILABLE) {
                availableDrivers.add(driver);
            }
        }

        return availableDrivers;
    }

    public void updateDriver(Driver updatedDriver) {
        if (findDriverById(updatedDriver.getDriverId()) == null) {
            throw new IllegalArgumentException("Driver not found");
        }
        // Driver objects are stored by reference, so their updated
        // fields are already reflected in this repository.
    }

    public void updateDriverStatus(int driverId, AvailabilityStatus status) {
        Driver driver = findDriverById(driverId);

        if (driver == null) {
            throw new IllegalArgumentException("Driver not found");
        }

        driver.setAvailabilityStatus(status);
    }

    public List<Driver> getAllDrivers() {
        return new ArrayList<>(drivers);
    }
}

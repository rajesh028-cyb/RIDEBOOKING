package com.ridebooking.service;

import com.ridebooking.models.Driver;
import com.ridebooking.repository.DriverRepository;
import java.util.List;

public class DriverService {
    private DriverRepository driverRepository;
    private int nextDriverId = 1;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public Driver registerDriver(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be empty.");
        }
        validatePhone(phone);

        Driver driver = new Driver(nextDriverId++, name.trim(), phone.trim());
        driverRepository.saveDriver(driver);
        return driver;
    }

    public Driver getDriverById(int id) {
        return driverRepository.findDriverById(id);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.getAllDrivers();
    }

    public Driver findAvailableDriver() {
        return driverRepository.findAvailableDriver();
    }

    public void setDriverAvailability(int driverId, boolean availability) {
        Driver driver = driverRepository.findDriverById(driverId);
        if (driver != null) {
            driver.setAvailability(availability);
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String cleanPhone = phone.trim();
        if (cleanPhone.length() < 7 || cleanPhone.length() > 15) {
            throw new IllegalArgumentException("Phone number must have between 7 and 15 digits.");
        }
        for (int i = 0; i < cleanPhone.length(); i++) {
            char c = cleanPhone.charAt(i);
            if (!Character.isDigit(c) && c != '+' && c != '-') {
                throw new IllegalArgumentException("Phone number contains invalid characters.");
            }
        }
    }
}

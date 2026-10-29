package com.ridebooking.service;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Rider;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RiderRepository;
import com.ridebooking.repository.TripRepository;
import java.util.List;

public class RideService {
    private RiderRepository riderRepository;
    private DriverRepository driverRepository;
    private TripRepository tripRepository;

    private int nextRiderId = 1;
    private int nextDriverId = 1;
    private int nextTripId = 1;

    public RideService(RiderRepository riderRepository, DriverRepository driverRepository, TripRepository tripRepository) {
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
        this.tripRepository = tripRepository;
    }

    public RideService(RiderRepository riderRepository) {
        this(riderRepository, new DriverRepository(), new TripRepository());
    }

    public RideService() {
        this(new RiderRepository(), new DriverRepository(), new TripRepository());
    }

    // ==========================================
    // Module 1: Rider Registration & Management
    // ==========================================
    public Rider registerRider(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Rider name cannot be empty.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        if (phone.trim().length() < 7) {
            throw new IllegalArgumentException("Phone number must have at least 7 digits.");
        }

        Rider rider = new Rider(nextRiderId++, name.trim(), phone.trim());
        riderRepository.save(rider);
        return rider;
    }

    public Rider getRiderById(int id) {
        return riderRepository.findById(id);
    }

    public List<Rider> getAllRiders() {
        return riderRepository.findAll();
    }

    // ==========================================
    // Module 2: Driver Registration & Management
    // ==========================================
    public Driver registerDriver(String name, String phone) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Driver name cannot be empty.");
        }
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        if (phone.trim().length() < 7) {
            throw new IllegalArgumentException("Phone number must have at least 7 digits.");
        }

        Driver driver = new Driver(nextDriverId++, name.trim(), phone.trim());
        driverRepository.save(driver);
        return driver;
    }

    public Driver getDriverById(int id) {
        return driverRepository.findById(id);
    }

    public List<Driver> getAllDrivers() {
        return driverRepository.findAll();
    }

    // ==========================================
    // Module 3: Ride Booking
    // ==========================================
    public Trip bookRide(int riderId, double distance) {
        Rider rider = riderRepository.findById(riderId);
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found with ID: " + riderId);
        }

        if (distance <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero.");
        }

        Trip trip = new Trip(nextTripId++, rider, distance);
        tripRepository.save(trip);
        return trip;
    }

    // ==========================================
    // Module 4: Driver Matching
    // ==========================================
    public Driver assignDriver(int tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"REQUESTED".equals(trip.getStatus())) {
            throw new IllegalStateException("Driver can only be assigned to trips with REQUESTED status. Current status: " + trip.getStatus());
        }

        if (trip.getDriver() != null) {
            throw new IllegalStateException("A driver is already assigned to this trip (Driver ID: " + trip.getDriver().getId() + ").");
        }

        Driver availableDriver = driverRepository.findAvailableDriver();
        if (availableDriver == null) {
            throw new IllegalStateException("No drivers are currently available. Please try again later.");
        }

        // Assign driver and mark as unavailable
        trip.setDriver(availableDriver);
        availableDriver.setAvailable(false);
        return availableDriver;
    }

    // ==========================================
    // Module 5: Trip Lifecycle
    // ==========================================
    public void acceptRide(int tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (trip.getDriver() == null) {
            throw new IllegalStateException("Cannot accept ride: No driver has been assigned yet.");
        }

        if (!"REQUESTED".equals(trip.getStatus())) {
            throw new IllegalStateException("Ride can only be accepted when status is REQUESTED. Current status: " + trip.getStatus());
        }

        trip.setStatus("ACCEPTED");
    }

    public void startTrip(int tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"ACCEPTED".equals(trip.getStatus())) {
            throw new IllegalStateException("Trip can only be started when status is ACCEPTED. Current status: " + trip.getStatus());
        }

        trip.setStatus("IN_PROGRESS");
    }

    public void completeTrip(int tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"IN_PROGRESS".equals(trip.getStatus())) {
            throw new IllegalStateException("Trip can only be completed when status is IN_PROGRESS. Current status: " + trip.getStatus());
        }

        trip.setStatus("COMPLETED");

        // Make the driver available again
        if (trip.getDriver() != null) {
            trip.getDriver().setAvailable(true);
        }
    }

    // ==========================================
    // Module 6: Payment
    // ==========================================
    public double makePayment(int tripId) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"COMPLETED".equals(trip.getStatus())) {
            throw new IllegalStateException("Payment can only be made after trip completion. Current trip status: " + trip.getStatus());
        }

        if ("PAID".equals(trip.getPaymentStatus())) {
            throw new IllegalStateException("Payment has already been made for this trip.");
        }

        trip.setPaymentStatus("PAID");
        return trip.getFare();
    }

    // ==========================================
    // Module 7: Ratings
    // ==========================================
    public void rateTrip(int tripId, int rating) {
        Trip trip = tripRepository.findById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"COMPLETED".equals(trip.getStatus())) {
            throw new IllegalStateException("Rating can only be given for completed trips. Current trip status: " + trip.getStatus());
        }

        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        trip.setRating(rating);
    }

    // ==========================================
    // Module 8: View Details
    // ==========================================
    public Trip getTripById(int id) {
        return tripRepository.findById(id);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.findAll();
    }
}

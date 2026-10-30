package com.ridebooking.service;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Rider;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RiderRepository;
import com.ridebooking.repository.TripRepository;
import java.util.List;

public class TripService {
    private TripRepository tripRepository;
    private RiderRepository riderRepository;
    private DriverRepository driverRepository;
    private int nextTripId = 101;

    public TripService(TripRepository tripRepository,
                       RiderRepository riderRepository,
                       DriverRepository driverRepository) {
        this.tripRepository = tripRepository;
        this.riderRepository = riderRepository;
        this.driverRepository = driverRepository;
    }

    public Trip bookRide(int riderId, String pickupLocation, String dropLocation, double distance) {
        Rider rider = riderRepository.findRiderById(riderId);
        if (rider == null) {
            throw new IllegalArgumentException("Rider not found with ID: " + riderId);
        }

        if (pickupLocation == null || pickupLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Pickup location cannot be empty.");
        }

        if (dropLocation == null || dropLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Drop location cannot be empty.");
        }

        if (distance <= 0) {
            throw new IllegalArgumentException("Distance must be greater than zero.");
        }

        Trip trip = new Trip(nextTripId++, rider, pickupLocation.trim(), dropLocation.trim(), distance);
        tripRepository.saveTrip(trip);
        return trip;
    }

    public Driver assignDriver(int tripId) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"REQUESTED".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Driver can only be assigned to trips with REQUESTED status. Current status: " + trip.getTripStatus());
        }

        if (trip.getDriver() != null) {
            throw new IllegalStateException("A driver is already assigned to this trip (Driver ID: " + trip.getDriver().getDriverId() + ").");
        }

        Driver availableDriver = driverRepository.findAvailableDriver();
        if (availableDriver == null) {
            throw new IllegalStateException("No drivers are currently available. Please try again later.");
        }

        trip.setDriver(availableDriver);
        availableDriver.setAvailability(false);
        return availableDriver;
    }

    public void acceptRide(int tripId) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (trip.getDriver() == null) {
            throw new IllegalStateException("Cannot accept ride: No driver has been assigned yet.");
        }

        if (!"REQUESTED".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Ride can only be accepted when status is REQUESTED. Current status: " + trip.getTripStatus());
        }

        trip.setTripStatus("ACCEPTED");
    }

    public void startTrip(int tripId) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"ACCEPTED".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Trip can only be started when status is ACCEPTED. Current status: " + trip.getTripStatus());
        }

        trip.setTripStatus("IN_PROGRESS");
    }

    public void completeTrip(int tripId) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"IN_PROGRESS".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Trip can only be completed when status is IN_PROGRESS. Current status: " + trip.getTripStatus());
        }

        trip.setTripStatus("COMPLETED");

        if (trip.getDriver() != null) {
            trip.getDriver().setAvailability(true);
        }
    }

    public Trip getTripById(int id) {
        return tripRepository.findTripById(id);
    }

    public List<Trip> getAllTrips() {
        return tripRepository.getAllTrips();
    }
}

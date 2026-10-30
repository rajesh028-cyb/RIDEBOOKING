package com.ridebooking.service;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Rider;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RiderRepository;
import com.ridebooking.repository.TripRepository;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class RideService {
    private static final double MAX_MATCH_DISTANCE_KM = 5.0;
    private static final long ACCEPTANCE_SECONDS = 30;
    private static final long EXPIRY_SECONDS = 120;
    private static final double CANCELLATION_CHARGE = 30.0;
    private final RiderService riderService;
    private final DriverService driverService;
    private final TripRepository tripRepository;
    private final Map<Integer, List<Integer>> offers = new HashMap<>();
    private final Map<String, Instant> offerTimes = new HashMap<>();
    private final Random random = new Random();
    private int nextTripId = 101;

    public RideService(RiderService riders, DriverService drivers, TripRepository trips) {
        if (riders == null || drivers == null || trips == null) throw new IllegalArgumentException("Services and repository are required");
        riderService = riders; driverService = drivers; tripRepository = trips;
    }
    public RideService(RiderRepository riders, DriverRepository drivers, TripRepository trips) {
        this(new RiderService(riders), new DriverService(drivers), trips);
    }
    public RideService(RiderService riders, DriverService drivers) {
        this(riders, drivers, new TripRepository());
    }

    public Trip requestRide(int riderId, double pickupX, double pickupY, double dropX, double dropY, Trip.VehicleType type) {
        Rider rider = riderService.getRiderById(riderId);
        if (rider == null) throw new IllegalArgumentException("Rider not found: " + riderId);
        if (tripRepository.findActiveTripsByRider(riderId).size() > 0) throw new IllegalStateException("Rider already has an active ride");
        Trip trip = new Trip(nextTripId++, riderId, pickupX, pickupY, dropX, dropY, type, String.format("%04d", random.nextInt(10000)));
        trip.setRider(rider);
        tripRepository.saveTrip(trip);
        matchDrivers(trip);
        return trip;
    }
    public Trip createRideRequest(int riderId, double pickupX, double pickupY, double dropX, double dropY, Trip.VehicleType type) {
        return requestRide(riderId, pickupX, pickupY, dropX, dropY, type);
    }
    public Trip bookRide(int riderId, String pickup, String drop, double distance) {
        if (distance <= 0) throw new IllegalArgumentException("Distance must be greater than zero");
        return requestRide(riderId, 0, 0, distance, 0, Trip.VehicleType.ANY);
    }

    private void matchDrivers(Trip trip) {
        List<Driver> candidates = new ArrayList<>();
        for (Driver driver : driverService.getAllDrivers()) {
            if (driver.isAvailable() && isApproved(driver) && matchesVehicle(driver, trip.getRequestedVehicleType())) {
                double distance = distanceToDriver(driver, trip.getPickupX(), trip.getPickupY());
                if (distance <= MAX_MATCH_DISTANCE_KM) candidates.add(driver);
            }
        }
        candidates.sort(Comparator.comparingDouble(d -> distanceToDriver(d, trip.getPickupX(), trip.getPickupY())));
        List<Integer> offered = new ArrayList<>();
        for (int i = 0; i < Math.min(3, candidates.size()); i++) {
            int driverId = candidates.get(i).getDriverId();
            offered.add(driverId);
            offerTimes.put(offerKey(trip.getTripId(), driverId), Instant.now());
        }
        offers.put(trip.getTripId(), offered);
    }
    public void acceptRide(int tripId, int driverId) {
        Trip trip = required(tripId);
        expireIfNeeded(trip);
        if (trip.getStatusEnum() != Trip.TripStatus.REQUESTED) throw new IllegalStateException("Ride is not pending");
        List<Integer> offered = offers.get(tripId);
        if (offered == null || !offered.contains(driverId)) throw new IllegalArgumentException("Ride was not offered to this driver");
        Instant offeredAt = offerTimes.get(offerKey(tripId, driverId));
        if (offeredAt == null || Duration.between(offeredAt, Instant.now()).getSeconds() >= ACCEPTANCE_SECONDS) {
            offered.remove(Integer.valueOf(driverId));
            offerTimes.remove(offerKey(tripId, driverId));
            throw new IllegalStateException("Driver acceptance window has expired");
        }
        Driver driver = driverService.getDriverById(driverId);
        if (driver == null || !driver.isAvailable()) throw new IllegalStateException("Driver is unavailable");
        trip.setDriver(driver);
        trip.markAccepted(Instant.now());
        driver.setAvailability(false);
    }
    public void rejectRide(int tripId, int driverId) {
        required(tripId);
        List<Integer> offered = offers.get(tripId);
        if (offered != null) offered.remove(Integer.valueOf(driverId));
        offerTimes.remove(offerKey(tripId, driverId));
    }
    public void expirePendingRequests() { for (Trip trip : tripRepository.retrievePendingRideRequests()) expireIfNeeded(trip); }
    private void expireIfNeeded(Trip trip) {
        if (Duration.between(trip.getRequestTime(), Instant.now()).getSeconds() >= EXPIRY_SECONDS) {
            List<Integer> tripOffers = offers.remove(trip.getTripId());
            trip.setStatus(Trip.TripStatus.EXPIRED);
            for (Integer driverId : new ArrayList<>(tripOffers == null ? new ArrayList<>() : tripOffers))
                offerTimes.remove(offerKey(trip.getTripId(), driverId));
        }
    }
    public void markDriverArrived(int tripId) { requireStatus(tripId, Trip.TripStatus.ACCEPTED); required(tripId).setStatus(Trip.TripStatus.DRIVER_ARRIVED); }
    public void startTrip(int tripId, String otp) {
        Trip trip = required(tripId);
        requireStatus(trip, Trip.TripStatus.DRIVER_ARRIVED);
        if (!trip.getOtp().equals(otp)) throw new IllegalArgumentException("Invalid OTP");
        trip.markStarted(Instant.now());
    }
    public void startTrip(int tripId) { throw new IllegalStateException("OTP is required to start a trip"); }
    public void completeTrip(int tripId) {
        Trip trip = required(tripId); requireStatus(trip, Trip.TripStatus.IN_PROGRESS);
        trip.markCompleted(Instant.now()); if (trip.getDriver() != null) trip.getDriver().setAvailability(true);
    }
    public void cancelByRider(int tripId, String reason) {
        Trip trip = required(tripId);
        if (trip.getStatusEnum() == Trip.TripStatus.IN_PROGRESS || trip.getStatusEnum() == Trip.TripStatus.COMPLETED)
            throw new IllegalStateException("Trip cannot be cancelled now");
        double charge = trip.getAcceptanceTime() != null && Duration.between(trip.getAcceptanceTime(), Instant.now()).getSeconds() >= 120 ? CANCELLATION_CHARGE : 0;
        trip.cancel(reason, charge); freeDriver(trip);
    }
    public void cancelByDriver(int tripId, String reason) {
        Trip trip = required(tripId);
        if (trip.getStatusEnum() != Trip.TripStatus.ACCEPTED) throw new IllegalStateException("Driver can cancel only before arrival");
        trip.cancel(reason, 0); freeDriver(trip);
    }
    private void freeDriver(Trip trip) { if (trip.getDriver() != null) trip.getDriver().setAvailability(true); }
    private String offerKey(int tripId, int driverId) { return tripId + ":" + driverId; }
    private Trip required(int id) { Trip trip = tripRepository.findTripById(id); if (trip == null) throw new IllegalArgumentException("Trip not found: " + id); return trip; }
    private void requireStatus(int id, Trip.TripStatus status) { requireStatus(required(id), status); }
    private void requireStatus(Trip trip, Trip.TripStatus status) { if (trip.getStatusEnum() != status) throw new IllegalStateException("Expected " + status + " but was " + trip.getStatusEnum()); }

    public Trip getTripForPayment(int tripId) { Trip trip = required(tripId); if (trip.getStatusEnum() != Trip.TripStatus.COMPLETED) throw new IllegalStateException("Trip is not completed"); return trip; }
    public Trip getCompletedTrip(int tripId) { return getTripForPayment(tripId); }
    public List<Trip> getCompletedTrips() { List<Trip> result = new ArrayList<>(); for (Trip t : tripRepository.getAllTrips()) if (t.getStatusEnum() == Trip.TripStatus.COMPLETED) result.add(t); return result; }
    public Trip getTripById(int id) { return tripRepository.findTripById(id); }
    public List<Trip> getAllTrips() { return tripRepository.getAllTrips(); }

    private boolean isApproved(Driver d) { return booleanProperty(d, "isApproved", true); }
    private boolean matchesVehicle(Driver d, Trip.VehicleType requested) { if (requested == Trip.VehicleType.ANY) return true; String value = stringProperty(d, "getVehicleType"); return value == null || requested.name().equalsIgnoreCase(value); }
    private double distanceToDriver(Driver d, double x, double y) { Double dx = numberProperty(d, "getX"); Double dy = numberProperty(d, "getY"); return dx == null || dy == null ? 0 : Math.hypot(dx - x, dy - y); }
    private boolean booleanProperty(Object object, String method, boolean fallback) { try { return (Boolean) object.getClass().getMethod(method).invoke(object); } catch (ReflectiveOperationException e) { return fallback; } }
    private String stringProperty(Object object, String method) { try { Object value = object.getClass().getMethod(method).invoke(object); return value == null ? null : value.toString(); } catch (ReflectiveOperationException e) { return null; } }
    private Double numberProperty(Object object, String method) { try { Object value = object.getClass().getMethod(method).invoke(object); return value instanceof Number ? ((Number) value).doubleValue() : null; } catch (ReflectiveOperationException e) { return null; } }
}

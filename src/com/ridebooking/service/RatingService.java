package com.ridebooking.service;

import com.ridebooking.models.Rating;
import com.ridebooking.repository.RatingRepository;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RatingService {
    private final RatingRepository repository;
    private final Object rideService;
    private final Object driverService;
    private final Object riderService;
    private final Map<Integer, Double> averages = new HashMap<>();
    private final Map<Integer, Boolean> driverSuspensions = new HashMap<>();
    private final Map<Integer, Boolean> riderReviews = new HashMap<>();
    private int nextRatingId = 1;

    public RatingService(RatingRepository repository, Object rideService) {
        this(repository, rideService, null, null);
    }

    public RatingService(RatingRepository repository, Object rideService,
                         Object driverService, Object riderService) {
        if (repository == null) throw new IllegalArgumentException("RatingRepository is required.");
        this.repository = repository;
        this.rideService = rideService;
        this.driverService = driverService;
        this.riderService = riderService;
    }

    public Rating submitRiderRating(int tripId, int riderId, int driverId, int ratingValue) {
        return submit(tripId, riderId, driverId, ratingValue, Rating.ReviewerType.RIDER);
    }

    public Rating submitDriverRating(int tripId, int driverId, int riderId, int ratingValue) {
        return submit(tripId, driverId, riderId, ratingValue, Rating.ReviewerType.DRIVER);
    }

    public Rating rateTrip(int tripId, int reviewerId, int receiverId, int ratingValue, String reviewerType) {
        if ("RIDER".equalsIgnoreCase(reviewerType)) {
            return submit(tripId, reviewerId, receiverId, ratingValue, Rating.ReviewerType.RIDER);
        }
        if ("DRIVER".equalsIgnoreCase(reviewerType)) {
            return submit(tripId, reviewerId, receiverId, ratingValue, Rating.ReviewerType.DRIVER);
        }
        throw new IllegalArgumentException("Reviewer type must be Rider or Driver.");
    }

    private Rating submit(int tripId, int reviewerId, int receiverId, int value,
                          Rating.ReviewerType type) {
        if (rideService == null) throw new IllegalStateException("RideService is required for rating validation.");
        if (value < 1 || value > 5) throw new IllegalArgumentException("Rating must be between 1 and 5.");
        Object trip = invoke(rideService, "getRide", tripId);
        if (trip == null) trip = invoke(rideService, "findRideById", tripId);
        if (trip == null) trip = invoke(rideService, "getTripById", tripId);
        if (trip == null) trip = invoke(rideService, "findTripById", tripId);
        if (trip == null) throw new IllegalArgumentException("Trip not found: " + tripId);

        if (!isCompleted(trip)) throw new IllegalStateException("Ratings can only be submitted for completed trips.");
        if (repository.hasRatedTrip(reviewerId, tripId)) {
            throw new IllegalStateException("This person has already rated trip " + tripId + '.');
        }

        LocalDateTime completedAt = completionTime(trip);
        Duration age = Duration.between(completedAt, LocalDateTime.now());
        if (age.isNegative()) throw new IllegalStateException("Trip completion time is in the future.");
        if (age.toHours() >= 24) throw new IllegalStateException("The 24-hour rating period has expired.");

        Rating rating = new Rating(nextRatingId++, tripId, reviewerId, receiverId, value, type, LocalDateTime.now());
        repository.save(rating);
        recalculate(receiverId, type);
        return rating;
    }

    private boolean isCompleted(Object trip) {
        String status = stringValue(trip, "getTripStatus", "getStatus", "getRideStatus");
        return status != null && status.equalsIgnoreCase("COMPLETED");
    }

    private LocalDateTime completionTime(Object trip) {
        Object value = value(trip, "getCompletionTime", "getCompletedAt", "getCompletionDate", "getCompletedDate");
        if (value == null) return LocalDateTime.now();
        if (value instanceof LocalDateTime) return (LocalDateTime) value;
        if (value instanceof Date) return LocalDateTime.ofInstant(((Date) value).toInstant(), ZoneId.systemDefault());
        if (value instanceof java.sql.Timestamp) return ((java.sql.Timestamp) value).toLocalDateTime();
        if (value instanceof LocalDate) return ((LocalDate) value).atTime(LocalTime.MAX);
        return LocalDateTime.parse(value.toString());
    }

    public void recalculateAverageRating(int receiverId, String reviewerType) {
        Rating.ReviewerType type;
        try { type = Rating.ReviewerType.valueOf(reviewerType.toUpperCase()); }
        catch (Exception e) { throw new IllegalArgumentException("Reviewer type must be Rider or Driver."); }
        recalculate(receiverId, type == Rating.ReviewerType.RIDER ? Rating.ReviewerType.DRIVER : Rating.ReviewerType.RIDER);
    }

    private void recalculate(int receiverId, Rating.ReviewerType ratingReviewerType) {
        List<Rating> received = ratingReviewerType == Rating.ReviewerType.RIDER
                ? repository.findRatingsReceivedByDriver(receiverId)
                : repository.findRatingsReceivedByRider(receiverId);
        double sum = 0;
        for (Rating rating : received) sum += rating.getValue();
        double average = received.isEmpty() ? 0.0 : sum / received.size();
        averages.put(receiverId, average);
        int rideCount = completedRideCount(receiverId, ratingReviewerType);
        if (ratingReviewerType == Rating.ReviewerType.RIDER && rideCount >= 20 && average < 3.0) {
            driverSuspensions.put(receiverId, true);
            suspendDriver(receiverId);
        }
        if (ratingReviewerType == Rating.ReviewerType.DRIVER && rideCount >= 10 && average < 3.0) {
            riderReviews.put(receiverId, true);
            flagRider(receiverId);
        }
    }

    private int completedRideCount(int personId, Rating.ReviewerType type) {
        Object rides = invoke(rideService, "getAllRides", "getAllTrips", "getTrips", "getRides");
        if (!(rides instanceof Iterable)) return 0;
        int count = 0;
        for (Object ride : (Iterable<?>) rides) {
            if (isCompleted(ride) && matchesPerson(ride, personId, type)) count++;
        }
        return count;
    }

    private boolean matchesPerson(Object ride, int personId, Rating.ReviewerType type) {
        String[] getters = type == Rating.ReviewerType.RIDER
                ? new String[]{"getDriverId", "getAssignedDriverId"}
                : new String[]{"getRiderId", "getCustomerId"};
        Object id = value(ride, getters);
        return id instanceof Number && ((Number) id).intValue() == personId;
    }

    public double getAverageRating(int personId) { return averages.getOrDefault(personId, calculateAverage(personId)); }

    private double calculateAverage(int personId) {
        List<Rating> ratings = repository.findAll();
        List<Rating> received = new ArrayList<>();
        for (Rating rating : ratings) if (rating.getReceiverId() == personId) received.add(rating);
        double sum = 0;
        for (Rating rating : received) sum += rating.getValue();
        return received.isEmpty() ? 0.0 : sum / received.size();
    }

    public boolean isDriverSuspended(int driverId) { return driverSuspensions.getOrDefault(driverId, false); }
    public boolean requiresRiderReview(int riderId) { return riderReviews.getOrDefault(riderId, false); }

    private void suspendDriver(int driverId) {
        invoke(driverService, "setSuspended", driverId, true);
        invoke(driverService, "suspendDriver", driverId);
        invoke(driverService, "updateSuspensionStatus", driverId, true);
    }

    private void flagRider(int riderId) {
        invoke(riderService, "setAdminReviewRequired", riderId, true);
        invoke(riderService, "flagForAdminReview", riderId);
        invoke(riderService, "updateAdminReviewStatus", riderId, true);
    }

    private Object invoke(Object target, String... methodNames) {
        if (target == null || methodNames.length == 0) return null;
        for (String methodName : methodNames) {
            Object result = call(target, methodName);
            if (result != null) return result;
        }
        return null;
    }

    private Object invoke(Object target, String methodName, Object... arguments) { return call(target, methodName, arguments); }

    private Object call(Object target, String methodName, Object... arguments) {
        if (target == null) return null;
        for (Method method : target.getClass().getMethods()) {
            if (!method.getName().equals(methodName) || method.getParameterCount() != arguments.length) continue;
            boolean compatible = true;
            for (int i = 0; i < arguments.length; i++) {
                Class<?> t = method.getParameterTypes()[i];
                if (arguments[i] != null && !isAssignable(t, arguments[i].getClass())) { compatible = false; break; }
            }
            if (!compatible) continue;
            try { method.setAccessible(true); return method.invoke(target, arguments); }
            catch (IllegalAccessException | InvocationTargetException e) { throw new IllegalStateException("Could not call " + methodName, e); }
        }
        return null;
    }

    private boolean isAssignable(Class<?> type, Class<?> valueType) {
        if (!type.isPrimitive()) return type.isAssignableFrom(valueType);
        if (type == int.class) return valueType == Integer.class;
        if (type == boolean.class) return valueType == Boolean.class;
        if (type == long.class) return valueType == Long.class;
        return false;
    }

    private Object value(Object target, String... methodNames) {
        for (String name : methodNames) { Object result = call(target, name); if (result != null) return result; }
        return null;
    }
    private String stringValue(Object target, String... methodNames) {
        Object value = value(target, methodNames);
        return value == null ? null : value.toString();
    }
}

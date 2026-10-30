package com.ridebooking.repository;

import com.ridebooking.models.Rating;
import java.util.ArrayList;
import java.util.List;

public class RatingRepository {
    private final List<Rating> ratings = new ArrayList<>();

    public void save(Rating rating) {
        if (rating == null) throw new IllegalArgumentException("Rating is required.");
        ratings.add(rating);
    }
    public void saveRating(Rating rating) { save(rating); }

    public List<Rating> findRatingsByTrip(int tripId) {
        List<Rating> result = new ArrayList<>();
        for (Rating rating : ratings) {
            if (rating.getTripId() == tripId) result.add(rating);
        }
        return result;
    }

    public List<Rating> findRatingsByTripId(int tripId) { return findRatingsByTrip(tripId); }

    public List<Rating> findRatingsReceivedByDriver(int driverId) {
        return findReceived(driverId, Rating.ReviewerType.RIDER);
    }

    public List<Rating> findRatingsReceivedByRider(int riderId) {
        return findReceived(riderId, Rating.ReviewerType.DRIVER);
    }

    private List<Rating> findReceived(int personId, Rating.ReviewerType expectedReviewer) {
        List<Rating> result = new ArrayList<>();
        for (Rating rating : ratings) {
            if (rating.getReceiverId() == personId && rating.getReviewerType() == expectedReviewer) {
                result.add(rating);
            }
        }
        return result;
    }

    public boolean hasRatedTrip(int reviewerId, int tripId) {
        for (Rating rating : ratings) {
            if (rating.getReviewerId() == reviewerId && rating.getTripId() == tripId) return true;
        }
        return false;
    }

    public List<Rating> findAll() { return new ArrayList<>(ratings); }
    public List<Rating> getAllRatings() { return findAll(); }
}

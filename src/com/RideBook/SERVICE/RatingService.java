package com.ridebooking.service;

import com.ridebooking.models.Rating;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.RatingRepository;
import com.ridebooking.repository.TripRepository;
import java.util.List;

public class RatingService {
    private RatingRepository ratingRepository;
    private TripRepository tripRepository;
    private int nextRatingId = 1;

    public RatingService(RatingRepository ratingRepository, TripRepository tripRepository) {
        this.ratingRepository = ratingRepository;
        this.tripRepository = tripRepository;
    }

    public Rating rateTrip(int tripId, int score) {
        Trip trip = tripRepository.findTripById(tripId);
        if (trip == null) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        if (!"COMPLETED".equals(trip.getTripStatus())) {
            throw new IllegalStateException("Rating can only be given after trip completion. Current trip status: " + trip.getTripStatus());
        }

        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        Rating existingRating = ratingRepository.findRatingByTripId(tripId);
        if (existingRating != null) {
            throw new IllegalStateException("Trip has already been rated.");
        }

        Rating rating = new Rating(nextRatingId++, tripId, score);
        ratingRepository.saveRating(rating);
        return rating;
    }

    public Rating getRatingByTripId(int tripId) {
        return ratingRepository.findRatingByTripId(tripId);
    }

    public List<Rating> getAllRatings() {
        return ratingRepository.getAllRatings();
    }
}
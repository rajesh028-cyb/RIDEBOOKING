package com.ridebooking.repository;

import com.ridebooking.models.Rating;
import java.util.ArrayList;
import java.util.List;

public class RatingRepository {
    private List<Rating> ratings = new ArrayList<>();

    public void saveRating(Rating rating) {
        ratings.add(rating);
    }

    public void save(Rating rating) {
        saveRating(rating);
    }

    public Rating findRatingByTripId(int tripId) {
        for (Rating rating : ratings) {
            if (rating.getTripId() == tripId) {
                return rating;
            }
        }
        return null;
    }

    public Rating findRatingById(int ratingId) {
        for (Rating rating : ratings) {
            if (rating.getRatingId() == ratingId) {
                return rating;
            }
        }
        return null;
    }

    public List<Rating> getAllRatings() {
        return ratings;
    }

    public List<Rating> findAll() {
        return getAllRatings();
    }
}

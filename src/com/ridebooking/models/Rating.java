package com.ridebooking.models;

public class Rating {
    private int ratingId;
    private int tripId;
    private int score; // 1 to 5

    public Rating(int ratingId, int tripId, int score) {
        this.ratingId = ratingId;
        this.tripId = tripId;
        this.score = score;
    }

    public int getRatingId() {
        return ratingId;
    }

    public int getId() {
        return ratingId;
    }

    public int getTripId() {
        return tripId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }
}

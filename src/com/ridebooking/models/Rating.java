package com.ridebooking.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class Rating {
    public enum ReviewerType { RIDER, DRIVER }

    private final int ratingId;
    private final int tripId;
    private final int reviewerId;
    private final int receiverId;
    private final int value;
    private final ReviewerType reviewerType;
    private final LocalDateTime reviewDate;

    public Rating(int ratingId, int tripId, int reviewerId, int receiverId,
                  int value, ReviewerType reviewerType, LocalDateTime reviewDate) {
        if (value < 1 || value > 5) {
            throw new IllegalArgumentException("Rating value must be between 1 and 5.");
        }
        this.ratingId = ratingId;
        this.tripId = tripId;
        this.reviewerId = reviewerId;
        this.receiverId = receiverId;
        this.value = value;
        this.reviewerType = Objects.requireNonNull(reviewerType, "reviewerType is required");
        this.reviewDate = Objects.requireNonNullElse(reviewDate, LocalDateTime.now());
    }

    public Rating(int ratingId, int tripId, int reviewerId, int receiverId,
                  int value, String reviewerType, LocalDateTime reviewDate) {
        this(ratingId, tripId, reviewerId, receiverId, value,
                ReviewerType.valueOf(reviewerType.toUpperCase()), reviewDate);
    }

    public int getRatingId() { return ratingId; }
    public int getTripId() { return tripId; }
    public int getReviewerId() { return reviewerId; }
    public int getReceiverId() { return receiverId; }
    public int getValue() { return value; }
    public int getRating() { return value; }
    public int getScore() { return value; }
    public ReviewerType getReviewerType() { return reviewerType; }
    public String getReviewerTypeName() { return reviewerType.name(); }
    public LocalDateTime getReviewDate() { return reviewDate; }
    public String getDate() { return reviewDate.toString(); }

    @Override
    public String toString() {
        return "Rating{ratingId=" + ratingId + ", tripId=" + tripId
                + ", reviewerId=" + reviewerId + ", receiverId=" + receiverId
                + ", value=" + value + ", reviewerType=" + reviewerType
                + ", reviewDate=" + reviewDate + '}';
    }
}

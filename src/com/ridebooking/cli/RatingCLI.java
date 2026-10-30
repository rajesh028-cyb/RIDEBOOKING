package com.ridebooking.cli;

import com.ridebooking.models.Rating;
import com.ridebooking.repository.RatingRepository;
import com.ridebooking.service.RatingService;
import java.util.List;
import java.util.Scanner;

public class RatingCLI {
    private final RatingService ratingService;
    private final RatingRepository repository;
    private final Scanner scanner;

    public RatingCLI(RatingService ratingService, RatingRepository repository) {
        if (ratingService == null || repository == null) {
            throw new IllegalArgumentException("Rating service and repository are required.");
        }
        this.ratingService = ratingService;
        this.repository = repository;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice;
        do {
            System.out.println("\n========== RATING MANAGEMENT ==========");
            System.out.println("1. Submit rider rating");
            System.out.println("2. Submit driver rating");
            System.out.println("3. View received ratings");
            System.out.println("4. Display average rating");
            System.out.println("5. Display review/suspension status");
            System.out.println("0. Back");
            choice = readInt("Enter choice: ");
            try {
                switch (choice) {
                    case 1: submitRiderRating(); break;
                    case 2: submitDriverRating(); break;
                    case 3: viewReceivedRatings(); break;
                    case 4: displayAverageRating(); break;
                    case 5: displayReviewStatus(); break;
                    case 0: break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (choice != 0);
    }

    public void submitRiderRating() {
        int tripId = readInt("Trip ID: ");
        int riderId = readInt("Rider ID: ");
        int driverId = readInt("Driver ID: ");
        int value = readInt("Rating (1-5): ");
        Rating rating = ratingService.submitRiderRating(tripId, riderId, driverId, value);
        System.out.println("Rating submitted: " + rating);
    }

    public void submitDriverRating() {
        int tripId = readInt("Trip ID: ");
        int driverId = readInt("Driver ID: ");
        int riderId = readInt("Rider ID: ");
        int value = readInt("Rating (1-5): ");
        Rating rating = ratingService.submitDriverRating(tripId, driverId, riderId, value);
        System.out.println("Rating submitted: " + rating);
    }

    public void viewReceivedRatings() {
        int personId = readInt("Person ID: ");
        System.out.print("Type (Rider/Driver): ");
        String type = scanner.nextLine().trim();
        List<Rating> ratings;
        if (type.equalsIgnoreCase("driver")) ratings = repository.findRatingsReceivedByDriver(personId);
        else if (type.equalsIgnoreCase("rider")) ratings = repository.findRatingsReceivedByRider(personId);
        else throw new IllegalArgumentException("Type must be Rider or Driver.");
        if (ratings.isEmpty()) System.out.println("No ratings found.");
        else for (Rating rating : ratings) System.out.println(rating);
    }

    public void displayAverageRating() {
        int personId = readInt("Person ID: ");
        System.out.printf("Average rating: %.2f%n", ratingService.getAverageRating(personId));
    }

    public void displayReviewStatus() {
        int personId = readInt("Person ID: ");
        System.out.print("Type (Rider/Driver): ");
        String type = scanner.nextLine().trim();
        if (type.equalsIgnoreCase("driver")) {
            System.out.println("Suspension status: " + (ratingService.isDriverSuspended(personId) ? "SUSPENDED" : "ACTIVE"));
        } else if (type.equalsIgnoreCase("rider")) {
            System.out.println("Admin review: " + (ratingService.requiresRiderReview(personId) ? "REQUIRED" : "NOT REQUIRED"));
        } else {
            throw new IllegalArgumentException("Type must be Rider or Driver.");
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try { return Integer.parseInt(line); }
            catch (NumberFormatException e) { System.out.println("Please enter a whole number."); }
        }
    }
}

package com.ridebooking.cli;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Payment;
import com.ridebooking.models.Rating;
import com.ridebooking.models.Rider;
import com.ridebooking.models.Trip;
import com.ridebooking.service.DriverService;
import com.ridebooking.service.PaymentService;
import com.ridebooking.service.RatingService;
import com.ridebooking.service.RiderService;
import com.ridebooking.service.TripService;
import java.util.List;
import java.util.Scanner;

public class RideMenu {
    private final RiderService riderService;
    private final DriverService driverService;
    private final TripService tripService;
    private final PaymentService paymentService;
    private final RatingService ratingService;
    private final Scanner scanner;

    public RideMenu(RiderService riderService,
                    DriverService driverService,
                    TripService tripService,
                    PaymentService paymentService,
                    RatingService ratingService) {
        this.riderService = riderService;
        this.driverService = driverService;
        this.tripService = tripService;
        this.paymentService = paymentService;
        this.ratingService = ratingService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        int choice = -1;

        do {
            System.out.println("\n========== RIDE BOOKING SYSTEM ==========");
            System.out.println("1. Register Rider");
            System.out.println("2. Register Driver");
            System.out.println("3. Book a Ride");
            System.out.println("4. Assign Driver");
            System.out.println("5. Accept Ride");
            System.out.println("6. Start Trip");
            System.out.println("7. Complete Trip");
            System.out.println("8. Make Payment");
            System.out.println("9. Rate Your Ride");
            System.out.println("10. View All Riders");
            System.out.println("11. View All Drivers");
            System.out.println("12. View All Trips");
            System.out.println("13. Search Trip");
            System.out.println("14. View Payment Details");
            System.out.println("15. View Rating Details");
            System.out.println("0. Exit");
            System.out.println("=========================================");

            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    registerRider();
                    break;
                case 2:
                    registerDriver();
                    break;
                case 3:
                    bookRide();
                    break;
                case 4:
                    assignDriver();
                    break;
                case 5:
                    acceptRide();
                    break;
                case 6:
                    startTrip();
                    break;
                case 7:
                    completeTrip();
                    break;
                case 8:
                    makePayment();
                    break;
                case 9:
                    rateYourRide();
                    break;
                case 10:
                    viewAllRiders();
                    break;
                case 11:
                    viewAllDrivers();
                    break;
                case 12:
                    viewAllTrips();
                    break;
                case 13:
                    searchTrip();
                    break;
                case 14:
                    viewPaymentDetails();
                    break;
                case 15:
                    viewRatingDetails();
                    break;
                case 0:
                    System.out.println("\nThank you for using Ride Booking System. Goodbye!");
                    System.out.println("Note: Application data is stored in memory and will reset upon closing.");
                    break;
                default:
                    System.out.println("Invalid choice. Please choose an option between 0 and 15.");
            }
        } while (choice != 0);
    }

    // ==========================================
    // Option 1: Register Rider
    // ==========================================
    private void registerRider() {
        System.out.println("\n----- REGISTER RIDER -----");
        String name = readString("Enter rider name: ");
        String phone = readString("Enter phone number: ");

        try {
            Rider rider = riderService.registerRider(name, phone);
            System.out.println("\nRider registered successfully!");
            System.out.println("Rider ID : " + rider.getRiderId());
            System.out.println("Name     : " + rider.getName());
            System.out.println("Phone    : " + rider.getPhoneNumber());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 2: Register Driver
    // ==========================================
    private void registerDriver() {
        System.out.println("\n----- REGISTER DRIVER -----");
        String name = readString("Enter driver name: ");
        String phone = readString("Enter phone number: ");

        try {
            Driver driver = driverService.registerDriver(name, phone);
            System.out.println("\nDriver registered successfully!");
            System.out.println("Driver ID    : " + driver.getDriverId());
            System.out.println("Name         : " + driver.getName());
            System.out.println("Phone        : " + driver.getPhoneNumber());
            System.out.println("Availability : " + (driver.isAvailable() ? "Available" : "Unavailable"));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 3: Book a Ride
    // ==========================================
    private void bookRide() {
        System.out.println("\n----- BOOK A RIDE -----");
        int riderId = readInt("Enter rider ID: ");
        String pickup = readString("Enter pickup location: ");
        String drop = readString("Enter drop location: ");
        double distance = readDouble("Enter distance in kilometres: ");

        try {
            Trip trip = tripService.bookRide(riderId, pickup, drop, distance);
            System.out.println("\nRide booked successfully!");
            System.out.println("Trip ID        : " + trip.getTripId());
            System.out.println("Rider Name     : " + trip.getRider().getName());
            System.out.println("Pickup         : " + trip.getPickupLocation());
            System.out.println("Drop           : " + trip.getDropLocation());
            System.out.println("Distance       : " + formatDistance(trip.getDistance()));
            System.out.println("Fare           : " + formatFare(trip.getFare()));
            System.out.println("Trip Status    : " + trip.getTripStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 4: Assign Driver
    // ==========================================
    private void assignDriver() {
        System.out.println("\n----- ASSIGN DRIVER -----");
        int tripId = readInt("Enter trip ID: ");

        try {
            Driver driver = tripService.assignDriver(tripId);
            System.out.println("\nDriver assigned successfully!");
            System.out.println("Trip ID         : " + tripId);
            System.out.println("Assigned Driver : " + driver.getName() + " (ID: " + driver.getDriverId() + ")");
            System.out.println("Driver Phone    : " + driver.getPhoneNumber());
            System.out.println("Driver is now marked as UNAVAILABLE.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 5: Accept Ride
    // ==========================================
    private void acceptRide() {
        System.out.println("\n----- ACCEPT RIDE -----");
        int tripId = readInt("Enter trip ID: ");

        try {
            tripService.acceptRide(tripId);
            System.out.println("\nRide accepted successfully!");
            System.out.println("Trip ID     : " + tripId);
            System.out.println("Trip Status : ACCEPTED");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 6: Start Trip
    // ==========================================
    private void startTrip() {
        System.out.println("\n----- START TRIP -----");
        int tripId = readInt("Enter trip ID: ");

        try {
            tripService.startTrip(tripId);
            System.out.println("\nTrip started successfully!");
            System.out.println("Trip ID     : " + tripId);
            System.out.println("Trip Status : IN_PROGRESS");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 7: Complete Trip
    // ==========================================
    private void completeTrip() {
        System.out.println("\n----- COMPLETE TRIP -----");
        int tripId = readInt("Enter trip ID: ");

        try {
            tripService.completeTrip(tripId);
            System.out.println("\nTrip completed successfully!");
            System.out.println("Trip ID     : " + tripId);
            System.out.println("Trip Status : COMPLETED");
            System.out.println("Assigned driver is now marked as AVAILABLE.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 8: Make Payment
    // ==========================================
    private void makePayment() {
        System.out.println("\n----- PAYMENT -----");
        int tripId = readInt("Enter Trip ID: ");

        try {
            Trip trip = paymentService.getTripForPayment(tripId);

            System.out.println("\nFare: " + formatFare(trip.getFare()));
            System.out.println("Payment Status: PENDING");

            int confirmChoice = readInt("\nConfirm payment? (1. Yes / 2. No): ");

            if (confirmChoice == 1) {
                Payment payment = paymentService.processPayment(tripId, true);
                System.out.println("\nPayment successful!");
                System.out.println("Payment ID     : " + payment.getPaymentId());
                System.out.println("Trip ID        : " + payment.getTripId());
                System.out.println("Amount Paid    : " + formatFare(payment.getAmount()));
                System.out.println("Payment Status : " + payment.getPaymentStatus());
            } else {
                Payment payment = paymentService.processPayment(tripId, false);
                System.out.println("\nPayment cancelled.");
                System.out.println("Payment Status: " + payment.getPaymentStatus());
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 9: Rate Your Ride
    // ==========================================
    private void rateYourRide() {
        System.out.println("\n----- RATE YOUR RIDE -----");
        int tripId = readInt("Enter Trip ID: ");
        int ratingScore = readInt("Enter rating (1-5): ");

        try {
            Rating rating = ratingService.rateTrip(tripId, ratingScore);
            System.out.println("\nThank you for your feedback!");
            System.out.println("Rating: " + rating.getScore() + "/5");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 10: View All Riders
    // ==========================================
    private void viewAllRiders() {
        List<Rider> riders = riderService.getAllRiders();
        if (riders.isEmpty()) {
            System.out.println("\nNo riders registered yet.");
            return;
        }

        System.out.println("\n----- ALL REGISTERED RIDERS -----");
        for (Rider r : riders) {
            System.out.println("ID: " + r.getRiderId() + " | Name: " + r.getName() + " | Phone: " + r.getPhoneNumber());
        }
    }

    // ==========================================
    // Option 11: View All Drivers
    // ==========================================
    private void viewAllDrivers() {
        List<Driver> drivers = driverService.getAllDrivers();
        if (drivers.isEmpty()) {
            System.out.println("\nNo drivers registered yet.");
            return;
        }

        System.out.println("\n----- ALL REGISTERED DRIVERS -----");
        for (Driver d : drivers) {
            String status = d.isAvailable() ? "AVAILABLE" : "UNAVAILABLE";
            System.out.println("ID: " + d.getDriverId() + " | Name: " + d.getName() + " | Phone: " + d.getPhoneNumber() + " | Status: " + status);
        }
    }

    // ==========================================
    // Option 12: View All Trips
    // ==========================================
    private void viewAllTrips() {
        List<Trip> trips = tripService.getAllTrips();
        if (trips.isEmpty()) {
            System.out.println("\nNo trips have been booked yet.");
            return;
        }

        System.out.println("\n----- ALL TRIPS -----");
        for (Trip trip : trips) {
            displayTripDetails(trip);
        }
    }

    // ==========================================
    // Option 13: Search Trip
    // ==========================================
    private void searchTrip() {
        System.out.println("\n----- SEARCH TRIP -----");
        int tripId = readInt("Enter trip ID: ");
        Trip trip = tripService.getTripById(tripId);

        if (trip == null) {
            System.out.println("Error: Trip not found with ID: " + tripId);
            return;
        }

        displayTripDetails(trip);
    }

    // ==========================================
    // Option 14: View Payment Details
    // ==========================================
    private void viewPaymentDetails() {
        System.out.println("\n----- VIEW PAYMENT DETAILS -----");
        int tripId = readInt("Enter Trip ID: ");
        Payment payment = paymentService.getPaymentByTripId(tripId);

        if (payment == null) {
            System.out.println("No payment record found for Trip ID: " + tripId);
            return;
        }

        System.out.println("\nPayment ID     : " + payment.getPaymentId());
        System.out.println("Trip ID        : " + payment.getTripId());
        System.out.println("Amount         : " + formatFare(payment.getAmount()));
        System.out.println("Payment Status : " + payment.getPaymentStatus());
    }

    // ==========================================
    // Option 15: View Rating Details
    // ==========================================
    private void viewRatingDetails() {
        System.out.println("\n----- VIEW RATING DETAILS -----");
        int tripId = readInt("Enter Trip ID: ");
        Rating rating = ratingService.getRatingByTripId(tripId);

        if (rating == null) {
            System.out.println("No rating record found for Trip ID: " + tripId);
            return;
        }

        System.out.println("\nRating ID : " + rating.getRatingId());
        System.out.println("Trip ID   : " + rating.getTripId());
        System.out.println("Rating    : " + rating.getScore() + "/5");
    }

    // ==========================================
    // Helper Display Methods
    // ==========================================
    private void displayTripDetails(Trip trip) {
        System.out.println("\n----------------------------------------");
        System.out.println("Trip ID: " + trip.getTripId());
        System.out.println("Rider: " + trip.getRider().getName());

        if (trip.getDriver() != null) {
            System.out.println("Driver: " + trip.getDriver().getName());
        } else {
            System.out.println("Driver: Not assigned");
        }

        System.out.println("Pickup: " + trip.getPickupLocation());
        System.out.println("Drop: " + trip.getDropLocation());
        System.out.println("Distance: " + formatDistance(trip.getDistance()));
        System.out.println("Fare: " + formatFare(trip.getFare()));
        System.out.println("Trip Status: " + trip.getTripStatus());

        Payment payment = paymentService.getPaymentByTripId(trip.getTripId());
        if (payment != null) {
            System.out.println("Payment Status: " + payment.getPaymentStatus());
        } else {
            System.out.println("Payment Status: PENDING");
        }

        Rating rating = ratingService.getRatingByTripId(trip.getTripId());
        if (rating != null) {
            System.out.println("Rating: " + rating.getScore() + "/5");
        } else {
            System.out.println("Rating: Not rated yet");
        }
        System.out.println("----------------------------------------");
    }

    private String formatFare(double fare) {
        if (fare == (long) fare) {
            return String.format("Rs.%d", (long) fare);
        } else {
            return String.format("Rs.%.2f", fare);
        }
    }

    private String formatDistance(double distance) {
        if (distance == (long) distance) {
            return String.format("%d km", (long) distance);
        } else {
            return String.format("%.2f km", distance);
        }
    }

    private String readString(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid whole number.");
            }
        }
    }

    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Double.parseDouble(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid decimal number.");
            }
        }
    }
}

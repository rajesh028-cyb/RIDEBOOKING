package com.ridebooking.cli;

import com.ridebooking.models.Driver;
import com.ridebooking.models.Rider;
import com.ridebooking.models.Trip;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.RiderRepository;
import com.ridebooking.repository.TripRepository;
import com.ridebooking.service.RideService;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final RideService rideService;
    private final Scanner scanner;

    public Main() {
        RiderRepository riderRepository = new RiderRepository();
        DriverRepository driverRepository = new DriverRepository();
        TripRepository tripRepository = new TripRepository();
        this.rideService = new RideService(riderRepository, driverRepository, tripRepository);
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    public void start() {
        int choice = -1;

        do {
            System.out.println("\n==================================");
            System.out.println("     RIDE BOOKING SYSTEM");
            System.out.println("==================================");
            System.out.println("1. Register Rider");
            System.out.println("2. Register Driver");
            System.out.println("3. Book Ride");
            System.out.println("4. Assign Driver");
            System.out.println("5. Accept Ride");
            System.out.println("6. Start Trip");
            System.out.println("7. Complete Trip");
            System.out.println("8. Make Payment");
            System.out.println("9. Rate Trip");
            System.out.println("10. View Details");
            System.out.println("0. Exit");
            System.out.println("==================================");

            choice = readInt("Enter your choice: ");

            switch (choice) {
                case 1:
                    registerRiderMenu();
                    break;
                case 2:
                    registerDriverMenu();
                    break;
                case 3:
                    bookRideMenu();
                    break;
                case 4:
                    assignDriverMenu();
                    break;
                case 5:
                    acceptRideMenu();
                    break;
                case 6:
                    startTripMenu();
                    break;
                case 7:
                    completeTripMenu();
                    break;
                case 8:
                    makePaymentMenu();
                    break;
                case 9:
                    rateTripMenu();
                    break;
                case 10:
                    viewDetailsMenu();
                    break;
                case 0:
                    System.out.println("\nThank you for using Ride Booking System. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid choice. Please choose an option between 0 and 10.");
            }
        } while (choice != 0);

        scanner.close();
    }

    // ==========================================
    // Option 1: Register Rider
    // ==========================================
    private void registerRiderMenu() {
        System.out.println("\n--- Register Rider ---");
        System.out.print("Enter rider name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine().trim();

        try {
            Rider rider = rideService.registerRider(name, phone);
            System.out.println("\nRider registered successfully!");
            System.out.println("Rider ID : " + rider.getId());
            System.out.println("Name     : " + rider.getName());
            System.out.println("Phone    : " + rider.getPhone());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 2: Register Driver
    // ==========================================
    private void registerDriverMenu() {
        System.out.println("\n--- Register Driver ---");
        System.out.print("Enter driver name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter phone number: ");
        String phone = scanner.nextLine().trim();

        try {
            Driver driver = rideService.registerDriver(name, phone);
            System.out.println("\nDriver registered successfully!");
            System.out.println("Driver ID    : " + driver.getId());
            System.out.println("Name         : " + driver.getName());
            System.out.println("Phone        : " + driver.getPhone());
            System.out.println("Availability : " + (driver.isAvailable() ? "Available" : "Unavailable"));
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 3: Book Ride
    // ==========================================
    private void bookRideMenu() {
        System.out.println("\n--- Book Ride ---");
        int riderId = readInt("Enter rider ID: ");
        double distance = readDouble("Enter ride distance in km: ");

        try {
            Trip trip = rideService.bookRide(riderId, distance);
            System.out.println("\nRide booked successfully!");
            System.out.println("Trip ID        : " + trip.getId());
            System.out.println("Rider Name     : " + trip.getRider().getName());
            System.out.println("Distance       : " + trip.getDistance() + " km");
            System.out.println("Estimated Fare : Rs. " + trip.getFare() + " (Base Rs. 50 + Rs. 12/km)");
            System.out.println("Status         : " + trip.getStatus());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 4: Assign Driver
    // ==========================================
    private void assignDriverMenu() {
        System.out.println("\n--- Assign Driver ---");
        int tripId = readInt("Enter trip ID: ");

        try {
            Driver driver = rideService.assignDriver(tripId);
            System.out.println("\nDriver assigned successfully!");
            System.out.println("Trip ID         : " + tripId);
            System.out.println("Assigned Driver : " + driver.getName() + " (ID: " + driver.getId() + ")");
            System.out.println("Driver Phone    : " + driver.getPhone());
            System.out.println("Driver is now marked as UNAVAILABLE.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 5: Accept Ride
    // ==========================================
    private void acceptRideMenu() {
        System.out.println("\n--- Accept Ride ---");
        int tripId = readInt("Enter trip ID: ");

        try {
            rideService.acceptRide(tripId);
            System.out.println("\nRide accepted by driver successfully!");
            System.out.println("Trip ID : " + tripId);
            System.out.println("Status  : ACCEPTED");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 6: Start Trip
    // ==========================================
    private void startTripMenu() {
        System.out.println("\n--- Start Trip ---");
        int tripId = readInt("Enter trip ID: ");

        try {
            rideService.startTrip(tripId);
            System.out.println("\nTrip started successfully!");
            System.out.println("Trip ID : " + tripId);
            System.out.println("Status  : IN_PROGRESS");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 7: Complete Trip
    // ==========================================
    private void completeTripMenu() {
        System.out.println("\n--- Complete Trip ---");
        int tripId = readInt("Enter trip ID: ");

        try {
            rideService.completeTrip(tripId);
            System.out.println("\nTrip completed successfully!");
            System.out.println("Trip ID : " + tripId);
            System.out.println("Status  : COMPLETED");
            System.out.println("The assigned driver is now marked as AVAILABLE again.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 8: Make Payment
    // ==========================================
    private void makePaymentMenu() {
        System.out.println("\n--- Make Payment ---");
        int tripId = readInt("Enter trip ID: ");

        try {
            double fare = rideService.makePayment(tripId);
            System.out.println("\nPayment successful!");
            System.out.println("Trip ID        : " + tripId);
            System.out.println("Amount Paid    : Rs. " + fare);
            System.out.println("Payment Status : PAID");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 9: Rate Trip
    // ==========================================
    private void rateTripMenu() {
        System.out.println("\n--- Rate Trip ---");
        int tripId = readInt("Enter trip ID: ");
        int rating = readInt("Enter rating (1 to 5): ");

        try {
            rideService.rateTrip(tripId, rating);
            System.out.println("\nRating recorded successfully!");
            System.out.println("Trip ID : " + tripId);
            System.out.println("Rating  : " + rating + " / 5 stars");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // ==========================================
    // Option 10: View Details
    // ==========================================
    private void viewDetailsMenu() {
        System.out.println("\n--- View Details ---");
        System.out.println("1. View Rider Details");
        System.out.println("2. View Driver Details");
        System.out.println("3. View Specific Trip Details");
        System.out.println("4. View All Trips");
        System.out.println("0. Back to Main Menu");

        int subChoice = readInt("Enter choice: ");

        switch (subChoice) {
            case 1:
                viewRiderDetails();
                break;
            case 2:
                viewDriverDetails();
                break;
            case 3:
                viewSpecificTripDetails();
                break;
            case 4:
                viewAllTrips();
                break;
            case 0:
                break;
            default:
                System.out.println("Invalid sub-menu choice.");
        }
    }

    private void viewRiderDetails() {
        int riderId = readInt("Enter rider ID (or 0 to list all riders): ");
        if (riderId == 0) {
            List<Rider> riders = rideService.getAllRiders();
            if (riders.isEmpty()) {
                System.out.println("No riders registered yet.");
                return;
            }
            System.out.println("\n--- All Registered Riders ---");
            for (Rider r : riders) {
                System.out.println("ID: " + r.getId() + " | Name: " + r.getName() + " | Phone: " + r.getPhone());
            }
        } else {
            Rider rider = rideService.getRiderById(riderId);
            if (rider == null) {
                System.out.println("Error: Rider not found with ID: " + riderId);
            } else {
                System.out.println("\n--- Rider Details ---");
                System.out.println("ID    : " + rider.getId());
                System.out.println("Name  : " + rider.getName());
                System.out.println("Phone : " + rider.getPhone());
            }
        }
    }

    private void viewDriverDetails() {
        int driverId = readInt("Enter driver ID (or 0 to list all drivers): ");
        if (driverId == 0) {
            List<Driver> drivers = rideService.getAllDrivers();
            if (drivers.isEmpty()) {
                System.out.println("No drivers registered yet.");
                return;
            }
            System.out.println("\n--- All Registered Drivers ---");
            for (Driver d : drivers) {
                String availability = d.isAvailable() ? "AVAILABLE" : "UNAVAILABLE";
                System.out.println("ID: " + d.getId() + " | Name: " + d.getName() + " | Phone: " + d.getPhone() + " | Status: " + availability);
            }
        } else {
            Driver driver = rideService.getDriverById(driverId);
            if (driver == null) {
                System.out.println("Error: Driver not found with ID: " + driverId);
            } else {
                System.out.println("\n--- Driver Details ---");
                System.out.println("ID           : " + driver.getId());
                System.out.println("Name         : " + driver.getName());
                System.out.println("Phone        : " + driver.getPhone());
                System.out.println("Availability : " + (driver.isAvailable() ? "AVAILABLE" : "UNAVAILABLE"));
            }
        }
    }

    private void viewSpecificTripDetails() {
        int tripId = readInt("Enter trip ID: ");
        Trip trip = rideService.getTripById(tripId);
        if (trip == null) {
            System.out.println("Error: Trip not found with ID: " + tripId);
            return;
        }

        printTripCard(trip);
    }

    private void viewAllTrips() {
        List<Trip> trips = rideService.getAllTrips();
        if (trips.isEmpty()) {
            System.out.println("No trips have been booked yet.");
            return;
        }

        System.out.println("\n--- All Booked Trips ---");
        for (Trip trip : trips) {
            printTripCard(trip);
        }
    }

    private void printTripCard(Trip trip) {
        System.out.println("\n----------------------------------------");
        System.out.println("Trip ID        : " + trip.getId());
        System.out.println("Rider          : " + trip.getRider().getName() + " (ID: " + trip.getRider().getId() + ", Phone: " + trip.getRider().getPhone() + ")");
        if (trip.getDriver() != null) {
            System.out.println("Driver         : " + trip.getDriver().getName() + " (ID: " + trip.getDriver().getId() + ", Phone: " + trip.getDriver().getPhone() + ")");
        } else {
            System.out.println("Driver         : Not yet assigned");
        }
        System.out.println("Distance       : " + trip.getDistance() + " km");
        System.out.println("Fare           : Rs. " + trip.getFare());
        System.out.println("Trip Status    : " + trip.getStatus());
        System.out.println("Payment Status : " + trip.getPaymentStatus());
        if (trip.getRating() > 0) {
            System.out.println("Rating         : " + trip.getRating() + " / 5 stars");
        } else {
            System.out.println("Rating         : Not rated yet");
        }
        System.out.println("----------------------------------------");
    }

    // Helper methods for crash-proof numeric inputs
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

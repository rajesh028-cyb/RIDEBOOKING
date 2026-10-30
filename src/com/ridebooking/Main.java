package com.ridebooking;

import com.ridebooking.cli.RideMenu;
import com.ridebooking.repository.DriverRepository;
import com.ridebooking.repository.PaymentRepository;
import com.ridebooking.repository.RatingRepository;
import com.ridebooking.repository.RiderRepository;
import com.ridebooking.repository.TripRepository;
import com.ridebooking.service.DriverService;
import com.ridebooking.service.PaymentService;
import com.ridebooking.service.RatingService;
import com.ridebooking.service.RiderService;
import com.ridebooking.service.TripService;

public class Main {
    public static void main(String[] args) {
        // Initialize repositories
        RiderRepository riderRepository = new RiderRepository();
        DriverRepository driverRepository = new DriverRepository();
        TripRepository tripRepository = new TripRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        RatingRepository ratingRepository = new RatingRepository();

        // Initialize separate services with constructor injection
        RiderService riderService = new RiderService(riderRepository);
        DriverService driverService = new DriverService(driverRepository);
        TripService tripService = new TripService(tripRepository, riderRepository, driverRepository);
        PaymentService paymentService = new PaymentService(paymentRepository, tripRepository);
        RatingService ratingService = new RatingService(ratingRepository, tripRepository);

        // Start CLI menu with injected services
        RideMenu rideMenu = new RideMenu(
            riderService,
            driverService,
            tripService,
            paymentService,
            ratingService
        );
        rideMenu.start();
    }
}

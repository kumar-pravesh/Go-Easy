package com.ride.goeasy.service;


import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.dto.FareBreakdownDTO;
import com.ride.goeasy.dto.GoogleDistanceResponse;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;

@Service
public class BookingService {

	    @Autowired
	    private BookingRepo bookingRepo;

	    @Autowired
	    private CustomerRepo customerRepo;

	    @Autowired
	    private DriverRepo driverRepo;

	    private static final double BASE_FARE = 50.0;
	    private static final double PER_KM_RATE = 10.0;
	    private static final String GOOGLE_MAPS_API_KEY = "YOUR_API_KEY";

	    // Preview fare calculation
	    public FareBreakdownDTO calculateFare(BookingRequestDTO dto) {
	        Customer customer = customerRepo.findById(dto.getCustomerId())
	                .orElseThrow(() -> new RuntimeException("Customer not found"));

	        Driver driver = driverRepo.findById(dto.getDriverId())
	                .orElseThrow(() -> new RuntimeException("Driver not found"));

	        if (!"AVAILABLE".equalsIgnoreCase(driver.getVehicle().getAvlStatus())) {
	            throw new RuntimeException("Driver is not available");
	        }


	        return getFareBreakdown(dto.getPickupLocation(), dto.getDropLocation(), driver);
	    }

	    // Book ride
	    public Booking createBooking(BookingRequestDTO dto) {
	        Customer customer = customerRepo.findById(dto.getCustomerId())
	                .orElseThrow(() -> new RuntimeException("Customer not found"));

	        Driver driver = driverRepo.findById(dto.getDriverId())
	                .orElseThrow(() -> new RuntimeException("Driver not found"));

	        if (!"AVAILABLE".equalsIgnoreCase(driver.getDstatus())) {
	            throw new RuntimeException("Driver is not available");
	        }

	        // Calculate fare & ETA
	        FareBreakdownDTO fareData = getFareBreakdown(dto.getPickupLocation(), dto.getDropLocation(), driver);

	        Booking booking = new Booking();
	        booking.setCustomer(customer);
	        booking.setDriver(driver);
	        booking.setSourceLocation(dto.getPickupLocation());
	        booking.setDestinationLocation(dto.getDropLocation());
	        booking.setDistance(fareData.getDistanceKm());
	        booking.setFare(fareData.getTotalFare());
	        booking.setEstimatedTime(fareData.getEstimatedTime());
	        booking.setBookingStatus("BOOKED");

	        // Update driver status to BUSY
	        driver.setDstatus("BUSY");
	        driverRepo.save(driver);

	        return bookingRepo.save(booking);
	    }

	    // Available vehicles
	    public List<Driver> seeAllAvailableVehicles() {
	        return driverRepo.findByDstatus("AVAILABLE");
	    }

	    // Helper method: Calculate fare using Maps API
	    private FareBreakdownDTO getFareBreakdown(String source, String destination, Driver driver) {
	        try {
	            String url = String.format(
	                    "https://maps.googleapis.com/maps/api/distancematrix/json?origins=%s&destinations=%s&key=%s",
	                    source, destination, GOOGLE_MAPS_API_KEY);

	            RestTemplate restTemplate = new RestTemplate();
	            GoogleDistanceResponse response = restTemplate.getForObject(new URI(url), GoogleDistanceResponse.class);

	            double distanceKm = response.getDistanceKm();  // implement POJO to parse JSON
	            String eta = response.getDurationText();

	            double vehicleMultiplier = getVehicleMultiplier(driver);
	            double dynamicFees = 0;

	            double totalFare = BASE_FARE + (PER_KM_RATE * distanceKm) * vehicleMultiplier + dynamicFees;

	            FareBreakdownDTO dto = new FareBreakdownDTO();
	            dto.setBaseFare(BASE_FARE);
	            dto.setDistanceKm(distanceKm);
	            dto.setPerKmCharge(PER_KM_RATE);
	            dto.setVehicleMultiplier(vehicleMultiplier);
	            dto.setDynamicFees(dynamicFees);
	            dto.setTotalFare(totalFare);
	            dto.setEstimatedTime(eta);

	            return dto;
	        } catch (Exception e) {
	            throw new RuntimeException("Error fetching distance from Maps API: " + e.getMessage());
	        }
	    }

	    private double getVehicleMultiplier(Driver driver) {
	        if (driver.getVehicle().getVehicleType().equalsIgnoreCase("SUV")) return 1.2;
	        return 1.0;
	    }
}

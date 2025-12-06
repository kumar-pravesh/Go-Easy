package com.ride.goeasy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.dto.FareBreakdownDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.service.BookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {

	  @Autowired
	    private BookingService bookingService;
	  
	  // Preview fare before booking
	    @PostMapping("/previewFare")
	    public FareBreakdownDTO previewFare(@RequestBody BookingRequestDTO dto) {
	        return bookingService.calculateFare(dto);
	    }
	  

	    @PostMapping("/bookRide")
	    public Booking bookRide(@RequestBody BookingRequestDTO dto) {
	        return bookingService.createBooking(dto);
	    }
	    
	    // See all available vehicles
	    @GetMapping("/availableVehicles")
	    public List<Driver> getAvailableVehicles() {
	        return bookingService.seeAllAvailableVehicles();
	    }
}

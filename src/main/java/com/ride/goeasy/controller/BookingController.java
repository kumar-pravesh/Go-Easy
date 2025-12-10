package com.ride.goeasy.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.BookingService;

@RestController
@RequestMapping("/booking")
public class BookingController {

	  @Autowired
	    private BookingService bookingService;
	  @PostMapping("/bookvehicle")
	  public ResponseStructure<Booking> bookVehicle(@RequestParam long mobno, @RequestBody BookingRequestDTO bookingRequestDTO){
	 return  bookingService.bookVehicle(mobno, bookingRequestDTO);
		  
		  
		  
	  }
	  
	 
	  

	    
}

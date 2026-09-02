package com.ride.goeasy.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.dto.RideReceiptDTO;
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
	  @PostMapping("/startRide")
	  public ResponseStructure<String> startRide(
	          @RequestParam int bookingId,
	          @RequestParam String otp) {

	      return bookingService.startRide(bookingId, otp);
	  }
	  @PostMapping("/generateEndOtp")
	  public ResponseStructure<String> generateEndOtp(
	          @RequestParam int bookingId) {

	      return bookingService.generateEndOtp(bookingId);
	  }
	  @PostMapping("/completeRide")
	  public ResponseStructure<String> completeRide(
	          @RequestParam int bookingId,
	          @RequestParam String otp) {
	      return bookingService.completeRide(bookingId, otp);
	  }

	  @PostMapping("/requestRecording")
	  public ResponseStructure<String> requestRecording(@RequestParam int bookingId) {
	      return bookingService.requestRecording(bookingId);
	  }

	  @GetMapping("/receipt")
	  public ResponseStructure<RideReceiptDTO> receipt(@RequestParam int bookingId) {
	      return bookingService.getRideReceipt(bookingId);
	  }

	  @GetMapping("/public/track")
	  public ResponseStructure<com.ride.goeasy.dto.PublicTrackingDTO> publicTrack(@RequestParam Integer bookingId) {
	      return bookingService.getPublicTracking(bookingId);
	  }

	  @PostMapping("/rateDriver")
	  public ResponseStructure<String> rateDriver(@RequestParam int bookingId, @RequestParam int rating) {
	      return bookingService.rateDriver(bookingId, rating);
	  }

	  @PostMapping("/rateCustomer")
	  public ResponseStructure<String> rateCustomer(@RequestParam int bookingId, @RequestParam int rating) {
	      return bookingService.rateCustomer(bookingId, rating);
	  }

	  @GetMapping("/validatePromo")
	  public ResponseStructure<java.util.Map<String, Object>> validatePromo(@RequestParam String code, @RequestParam double fare) {
	      return bookingService.validatePromoCode(code, fare);
	  }

	  @PostMapping("/sos")
	  public ResponseStructure<String> triggerSOS(@RequestParam int bookingId, @RequestParam double latitude, @RequestParam double longitude) {
	      return bookingService.triggerSOS(bookingId, latitude, longitude);
	  }
}

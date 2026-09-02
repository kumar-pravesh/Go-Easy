package com.ride.goeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.RideDetailsDTO;
import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.dto.PaymentByCashDTO;
import com.ride.goeasy.dto.PaymentByUpiDTO;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.BookingService;
import com.ride.goeasy.service.DriverService;

@RestController
@RequestMapping("/driver")
public class DriverController {

	@Autowired
	DriverService driverService;
		
	@Autowired
	private BookingService bookingService;
	

	// to perform save operation

	@PostMapping("/save")
	public ResponseStructure<Driver> saveDriverWithVehicle(@RequestBody Driver driver) {
		return driverService.saveDriverWithVehicle(driver);
	}

	@PostMapping("/updateLocation")
	public ResponseStructure<Vehicle> updateLocation(@RequestParam long mobNo, @RequestParam double lat, @RequestParam double lon) {
		return driverService.updateDriverLocation(mobNo, lat, lon);
	}

	@GetMapping("/earnings")
	public ResponseStructure<com.ride.goeasy.dto.EarningsDTO> getEarnings(@RequestParam long mobNo) {
		return driverService.getEarningsSummary(mobNo);
	}

	// find operation
	@GetMapping("/find")
	public ResponseStructure<Driver> find(@RequestParam int id) {
		return driverService.find(id);
	}

//	Delete Driver by id
	@DeleteMapping("/delete")
	public ResponseStructure<Driver> delete(@RequestParam int id) {
		return driverService.deleteDriverById(id);
	}

	@PutMapping("/update")
	public ResponseStructure<Driver> updateDriver(@RequestParam int id, @RequestBody Driver newData) {

		return driverService.updateDriver(id, newData);
		
	}
	
	
	
	@GetMapping("/seeBookingHistory")
	public ResponseStructure< BookingHistoryDTO> history(@RequestParam long mobNo) {
	    return driverService.getDriverBookingHistory(mobNo);
	}
	
	

	@GetMapping("/activeBooking")
	public ResponseStructure<com.ride.goeasy.entity.Booking> activeBooking(@RequestParam long mobNo) {
	    return  driverService.getDriverActiveBooking(mobNo);
	    
	}
	
	@PostMapping("/payByCash")
	public ResponseStructure<PaymentByCashDTO> confirmPaymentByCash(@RequestParam int bookingId, @RequestParam String paymentType) {
		return driverService.confirmPaymentByCash(bookingId, paymentType);
	}
	
	
	@PostMapping("/confirmUpiPayment")
	public ResponseStructure<PaymentByUpiDTO> confirmUpiPayment(@RequestParam int bookingId){
	    return driverService.confirmUpiPaymentSuccess(bookingId);
	}

	@GetMapping("/generateUpiQr")
	public ResponseStructure<PaymentByUpiDTO> generateUpiQr(@RequestParam int bookingId){
	    return driverService.confirmPaymentByUPI(bookingId);
	}

 // Driver cancel booking	
	@PutMapping("/cancel/{bookingId}")
	public ResponseStructure<String> driverCancel(
			@PathVariable int bookingId,
			@RequestParam(required = false) String reason) {
	    return bookingService.cancelBookingByDriver(bookingId, reason);
	}

	@PutMapping("/status")
	public ResponseStructure<String> updateStatus(@RequestParam long mobNo, @RequestParam String status) {
		return driverService.updateDriverStatus(mobNo, status);
	}

	@PostMapping("/rate")
	public ResponseStructure<String> rateDriver(@RequestParam int bookingId, @RequestParam double rating) {
		return driverService.rateDriver(bookingId, rating);
	}

	/**
	 * Admin / internal endpoint to update a driver's verification status.
	 * field: AADHAAR | LICENSE | BACKGROUND
	 */
	@PostMapping("/respondRecording")
	public ResponseStructure<String> respondRecording(@RequestParam int bookingId, @RequestParam boolean accept) {
		return bookingService.respondRecording(bookingId, accept);
	}

	@PutMapping("/verify")
	public ResponseStructure<String> updateVerification(
			@RequestParam long mobNo,
			@RequestParam String field,
			@RequestParam boolean value,
			@RequestParam(required = false) String cleanRecordDate) {
		java.time.LocalDate date = cleanRecordDate != null ? java.time.LocalDate.parse(cleanRecordDate) : null;
		return driverService.updateVerification(mobNo, field, value, date);
	}

}

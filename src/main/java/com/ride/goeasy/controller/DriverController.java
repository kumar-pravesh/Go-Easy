package com.ride.goeasy.controller;

import org.hibernate.CustomEntityDirtinessStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

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
import com.ride.goeasy.dto.PaymentDTO;
import com.ride.goeasy.entity.Booking;

import com.ride.goeasy.entity.Driver;
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
	
	
	
	
	
	

	@GetMapping("/activeBooking")
	public ResponseStructure<RideDetailsDTO> activeBooking(@RequestParam long mobNo) {
	    return  driverService.getDriverActiveBooking(mobNo);
	    
	}
	
	@PostMapping("/payByCash")
	 public ResponseStructure<PaymentByCashDTO> confirmPaymentByCash(@RequestParam int bookingId, @RequestParam String paymentType){
	    	return driverService.confirmPaymnetByCash(bookingId , paymentType);
	    }
	
//	
//	@PostMapping("/payByUPI")
//	public ResponseStructure<PaymentByUpiDTO> confirmPaymentByUPI(@RequestParam int bookingId){
//		return driverService.confirmPaymentByUPI(bookingId);
//	
//	}
	@PostMapping("/confirmPayByUpi")
	 public ResponseStructure<PaymentByCashDTO> confirmPaymentByUPI(@RequestParam int bookingId, @RequestParam String paymentType){
    	return driverService.confirmPaymnetByUPI(bookingId , paymentType);
    }
	@GetMapping("/seeBookingHistory")
	public ResponseStructure< BookingHistoryDTO> history(@RequestParam long mobNo) {
	    return driverService.getDriverBookingHistory(mobNo);
	}
	
}

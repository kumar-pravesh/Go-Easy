package com.ride.goeasy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.response.ResponseStructure;

@RestControllerAdvice
public class GlobalExceptionHandler {

//	customer Exception

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseStructure<Customer> CustomerNotFoundException(CustomerNotFoundException ex) {
		ResponseStructure<Customer> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.NOT_FOUND.value());
		rs.setMessage(ex.getMessage());
		rs.setData(null);

		return rs;

	}

//	Driver Exception

	@ExceptionHandler(DriverNotFoundException.class)
	public ResponseStructure<Driver> DriverNotFoundException(DriverNotFoundException ex) {

		ResponseStructure<Driver> rs = new ResponseStructure();
		rs.setStatusCode(HttpStatus.NOT_FOUND.value());
		rs.setMessage(ex.getMessage());
		rs.setData(null);
		return rs;

	}

//	Vehicle Exception
	@ExceptionHandler(VehicleNotFoundException.class)
	public ResponseStructure<Vehicle> VehicleNotFoundException(VehicleNotFoundException ex) {
		ResponseStructure<Vehicle> rs = new ResponseStructure();
		rs.setStatusCode(HttpStatus.NOT_FOUND.value());
		rs.setMessage(ex.getMessage());
		rs.setData(null);
		return rs;

	}

//	Booking Exception
	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseStructure<Booking> BookingNotFoundException(BookingNotFoundException ex) {
		ResponseStructure<Booking> rs = new ResponseStructure();
		rs.setStatusCode(HttpStatus.NOT_FOUND.value());
		rs.setMessage(ex.getMessage());
		rs.setData(null);

		return rs;

	}

	// Invalid Location Exception
	@ExceptionHandler(InvalidLocationException.class)
	public ResponseStructure<String> handleInvalidLocation(InvalidLocationException ex) {
		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.BAD_REQUEST.value());
		rs.setMessage(ex.getMessage());
		rs.setData(null);
		return rs;
	}
 
		 
}

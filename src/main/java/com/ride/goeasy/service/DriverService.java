package com.ride.goeasy.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.PaymentByCashDTO;
import com.ride.goeasy.dto.PaymentDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.exception.BookingNotFoundException;
import com.ride.goeasy.exception.DriverNotFoundException;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.PaymentRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class DriverService {

	@Autowired
	DriverRepo driverRepo;
    @Autowired
    BookingRepo bookingRepo;
//	Save Driver method
    @Autowired
    PaymentRepo paymentRepo;
    @Autowired
    CustomerRepo customerRepo;
    @Autowired
    VehicleRepo vehicleRepo;
    
	public ResponseStructure<Driver> saveDriverWithVehicle(Driver driver) {

		
		Driver savedDriver = driverRepo.save(driver);

		ResponseStructure<Driver> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.CREATED.value());
		rs.setMessage("Driver Saved Successfully");
		rs.setData(savedDriver);

		return rs;
	}

//	Find Diver By ID
	public ResponseStructure<Driver> find(int id) {
		Driver findDriver = driverRepo.findById(id).orElseThrow(() -> new DriverNotFoundException("Driver Not Found"));

		ResponseStructure<Driver> rs = new ResponseStructure<Driver>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Driver Found Successfully");
		rs.setData(findDriver);

		return rs;

	}

//	Delete Driver by Id
	public ResponseStructure<Driver> deleteDriverById(int id) {
		// Step 1: Find driver
		Driver driver = driverRepo.findById(id)
				.orElseThrow(() -> new DriverNotFoundException("Driver Not Found With ID: " + id));

		// Step 2: Delete driver
		driverRepo.delete(driver);

		// Step 3: Response
		ResponseStructure<Driver> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Driver Deleted Successfully");
		rs.setData(driver);

		return rs;

	}
	
	
//	update driver
	public ResponseStructure<Driver> updateDriver(int id, Driver newData) {

	    // Step 1️ Find old data
	    Driver oldData = driverRepo.findById(id)
	            .orElseThrow(() -> new DriverNotFoundException("Driver Not Found with ID: " + id));

	    // Step 2️ Update fields
	    oldData.setDname(newData.getDname());
	    oldData.setLicNo(newData.getLicNo());
	    oldData.setUpiId(newData.getUpiId());
	    oldData.setDstatus(newData.getDstatus());
	    oldData.setAge(newData.getAge());
	    oldData.setMobNo(newData.getMobNo());
	    oldData.setGender(newData.getGender());
	    oldData.setMailId(newData.getMailId());

	    // Step 3 Save updated data
	    Driver updatedDriver = driverRepo.save(oldData);

	    // Step 4️ Response structure
	    ResponseStructure<Driver> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Driver Updated Successfully");
	    rs.setData(updatedDriver);

	    return rs;
	}

	public ResponseStructure<PaymentByCashDTO> confirmPaymnetByCash(int bookingId) {
		 Booking b= bookingRepo.findById(bookingId).orElseThrow(( )->new BookingNotFoundException("Booking not found with id:"+ bookingId));
		 b.setBookingStatus("COMPLETED");
	Customer c=	 b.getCustomer() ;
	c.setActiveBookingFlag(false);
	
	Vehicle v=	 b.getVehicle() ;
	v.setAvlStatus("AVAILABLE");
		 b.setActiveBookingFlag(false);
		
		 Payment p=new Payment();
		   p.setVehicle(v);
		   p.setCustomer(c);
		   p.setBooking(b);
		   p.setPaymentStatus("PAID");
		   p.setPaymentType("CASH");
		   
	bookingRepo.save(b);
	customerRepo.save(c);
	vehicleRepo.save(v);
	paymentRepo.save(p);
		   
		   
	PaymentByCashDTO pdto=new PaymentByCashDTO();
	pdto.setBookingId(b.getId());
	pdto.setCustomerId(c.getId());
	pdto.setDriverId(v.getId());
	pdto.setAmountPaid(b.getFare());
	pdto.setPaymentType("CASH");
	pdto.setPaymentStatus("PAID");
	  
			
	 ResponseStructure<PaymentByCashDTO> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Ride completed ->Amount paid");
	    rs.setData(pdto);

	    return rs;
		   
		 
	
		 
		 
	}


}

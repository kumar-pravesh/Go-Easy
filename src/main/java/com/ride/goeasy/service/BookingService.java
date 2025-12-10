package com.ride.goeasy.service;




import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.BookingRequestDTO;
 
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class BookingService {

	    @Autowired
	    private BookingRepo bookingRepo;

	    @Autowired
	    private CustomerRepo customerRepo;

	    @Autowired
	    private DriverRepo driverRepo;
	    @Autowired
	    private VehicleService vs;
	    
	    @Autowired
	    private VehicleRepo vr;

		public ResponseStructure<Booking> bookVehicle(long mobno, BookingRequestDTO bookingRequestDTO) {
		 Booking b=new Booking();
		 Optional customer=customerRepo.findByMobno(mobno);
			 Customer cust=(Customer) customer.get();
		Optional<Vehicle> v=	vr.findByVehicleNumber(bookingRequestDTO.getVehicleNumber());
		 Vehicle vehicle= v.get();
		
	     b.setCustomer(cust);
	     b.setVehicle(vehicle);
	     b.setSourceLocation(bookingRequestDTO.getSourceLocation());
	     b.setDestinationLocation(bookingRequestDTO.getDestinationLocation());
	     b.setFare(bookingRequestDTO.getFare());
	     b.setDistance(bookingRequestDTO.getDistance());
	     b.setEstimatedTime(bookingRequestDTO.getEstimatedTime());
	     b.setBookingStatus("PENDING");
	     b.setPayment(null);
	     
		   
		 
		 
		 ResponseStructure<Booking> rs = new ResponseStructure<>();
	        rs.setStatusCode(HttpStatus.CREATED.value());
	        rs.setMessage("vehicle book Successfully");
	        rs.setData(b);

	        return rs;
		}

	   
	  
}

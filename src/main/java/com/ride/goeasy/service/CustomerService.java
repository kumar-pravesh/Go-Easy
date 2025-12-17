package com.ride.goeasy.service;
 import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.dto.CustomerDTO;
import com.ride.goeasy.dto.CustomerResponseDTO;
import com.ride.goeasy.dto.RideDetailsDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.enums.BookingStatus;
import com.ride.goeasy.exception.BookingNotFoundException;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class CustomerService {
	
	   @Autowired
	    private CustomerRepo customerRepo;
	   
	   @Autowired
	   private BookingRepo bookingRepo;
	   
	   @Autowired
	   private DriverRepo driverRepo;
	   
	   @Autowired
	   private VehicleRepo vehicleRepo;
	   

	   @Autowired
	   BookingService bs;
    @Autowired
        BookingRepo br;
	    // SAVE CUSTOMER
	    public ResponseStructure<CustomerResponseDTO> saveCustomer(CustomerDTO dto) {

	        Customer c = new Customer();
	        c.setName(dto.getName());
	        c.setAge(dto.getAge());
	        c.setGender(dto.getGender());
	        c.setMobno(dto.getMobno());
	        c.setEmail(dto.getEmail());
	        c.setCurrentLocation(dto.getLat() + "," + dto.getLon());

	        Customer saved = customerRepo.save(c);

	        // Convert Entity → ResponseDTO
	        CustomerResponseDTO res = convertToResponse(saved);

	        ResponseStructure<CustomerResponseDTO> rs = new ResponseStructure<>();
	        rs.setStatusCode(HttpStatus.CREATED.value());
	        rs.setMessage("Customer Registered Successfully");
	        rs.setData(res);

	        return rs;
	    }

	    // FIND CUSTOMER BY MOBILE
	    public ResponseStructure<CustomerResponseDTO> findCustomer(Long mobno) {

	        Customer c = customerRepo.findByMobno(mobno)
	                .orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobno));

	        CustomerResponseDTO res = convertToResponse(c);

	        ResponseStructure<CustomerResponseDTO> rs = new ResponseStructure<>();
	        rs.setStatusCode(HttpStatus.OK.value());
	        rs.setMessage("Customer Found");
	        rs.setData(res);

	        return rs;
	    }

	    // DELETE CUSTOMER
	    public ResponseStructure<CustomerResponseDTO> deleteCustomer(Long mobno) {

	        Customer c = customerRepo.findByMobno(mobno)
	                .orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobno));

	        customerRepo.delete(c);

	        CustomerResponseDTO res = convertToResponse(c);

	        ResponseStructure<CustomerResponseDTO> rs = new ResponseStructure<>();
	        rs.setStatusCode(HttpStatus.OK.value());
	        rs.setMessage("Customer Deleted Successfully");
	        rs.setData(res);

	        return rs;
	    }

	    // UPDATE CUSTOMER
	    public ResponseStructure<CustomerResponseDTO> updateCustomer(Long mobno, CustomerDTO dto) {

	        Customer c = customerRepo.findByMobno(mobno)
	                .orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobno));

	        c.setName(dto.getName());
	        c.setAge(dto.getAge());
	        c.setGender(dto.getGender());
	        c.setEmail(dto.getEmail());
	        c.setMobno(dto.getMobno());
	        c.setCurrentLocation(dto.getLat() + "," + dto.getLon());

	        Customer updated = customerRepo.save(c);

	        CustomerResponseDTO res = convertToResponse(updated);

	        ResponseStructure<CustomerResponseDTO> rs = new ResponseStructure<>();
	        rs.setStatusCode(HttpStatus.OK.value());
	        rs.setMessage("Customer Updated Successfully");
	        rs.setData(res);

	        return rs;
	    }
	    
	    
	    
	    private CustomerResponseDTO convertToResponse(Customer c) {

	        CustomerResponseDTO res = new CustomerResponseDTO();
	        res.setId(c.getId());
	        res.setName(c.getName());
	        res.setAge(c.getAge());
	        res.setGender(c.getGender());
	        res.setMobno(c.getMobno());
	        res.setEmail(c.getEmail());
	        res.setCurrentLocation(c.getCurrentLocation());

	        return res;
	    }

		public ResponseStructure<BookingHistoryDTO> getDriverBookingHistory(long mobno) {
		
			Customer c=	customerRepo.findByMobno(mobno).orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobno));
	        List<Booking> blist= c.getBookings();
	     return   bs.getBookingHistory(blist);
			 
		}

		public ResponseStructure<RideDetailsDTO> getDriverActiveBooking(long mobNo) {
			Customer c=	customerRepo.findByMobno(mobNo).orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobNo));
	        List<Booking> blist= c.getBookings();
		return	 bs.activeBookingHistory(blist);
		}

		public ResponseStructure<String> cancellRide(int bookingId) {
			 
			 Booking b= br.findById(bookingId).orElseThrow(() -> new BookingNotFoundException("No any booking with given id:"+ bookingId));
			 Customer c= b.getCustomer();
			 Vehicle v=b.getVehicle();
			 Driver d=v.getDriver();
					 c.setActiveBookingFlag(false);
					 b.setActiveBookingFlag(false);
					 b.setBookingStatus(BookingStatus.CANCELLED_BY_CUSTOMER);
					 d.setDstatus("AVAILABLE");
					
					
					 int count=c.getCancellationCount()+1;
			   c.setCancellationCount(count);
			   
			   customerRepo.save(c);
			   bookingRepo.save(b);
			   driverRepo.save(d);
			   vehicleRepo.save(v);
					 
			 ResponseStructure<String> rs= new ResponseStructure<String>();
			  rs.setStatusCode(HttpStatus.OK.value());
		        rs.setMessage("Booking Cencel Request Accepted");
		        rs.setData("Booking cancelled by customer");

		 
		}
 
		 


}

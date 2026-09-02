package com.ride.goeasy.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.dto.CustomerDTO;
import com.ride.goeasy.dto.CustomerResponseDTO;
import com.ride.goeasy.dto.PaymentDTO;
import com.ride.goeasy.dto.RideDetailsDTO;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.CustomerService;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	    @Autowired
	    private CustomerService customerService;

	    
	    @PostMapping("/register/save")
	    public ResponseStructure<CustomerResponseDTO> saveCustomer(@RequestBody CustomerDTO dto) {
	        return customerService.saveCustomer(dto);
	    }

	    @GetMapping("/find")
	    public ResponseStructure<CustomerResponseDTO> findCustomer(@RequestParam Long mobno) {
	        return customerService.findCustomer(mobno);
	    }

	    @DeleteMapping("/delete")
	    public ResponseStructure<CustomerResponseDTO> deleteCustomer(@RequestParam Long mobno) {
	        return customerService.deleteCustomer(mobno);
	    }

	    @PutMapping("/update")
	    public ResponseStructure<CustomerResponseDTO> updateCustomer(
	            @RequestParam Long mobno,
	            @RequestBody CustomerDTO dto) {
	        return customerService.updateCustomer(mobno, dto);
	    }

		@GetMapping("/seeBookingHistory")
		public ResponseStructure< BookingHistoryDTO> history(@RequestParam long mobNo) {
		    return customerService.getDriverBookingHistory(mobNo);
		}
		
		@GetMapping("/activeBooking")
		public ResponseStructure<com.ride.goeasy.entity.Booking> activeBooking(@RequestParam long mobNo) {
		    return  customerService.getDriverActiveBooking(mobNo);
		    
		}
	    @PostMapping("/cancelRide")
	    public ResponseStructure<String> cancelRide(
				@RequestParam int bookingId,
				@RequestParam(required = false) String reason) {
	    	return customerService.cancelRide(bookingId, reason);
	    }


	@GetMapping("/scheduledRides")
	public ResponseStructure<java.util.List<com.ride.goeasy.entity.Booking>> scheduledRides(@RequestParam long mobNo) {
		return customerService.getScheduledRides(mobNo);
	}

	@GetMapping("/getCity")
	public ResponseStructure<String> getCity(@RequestParam double lat, @RequestParam double lon) {
		String city = customerService.getCityFromCoordinates(lat, lon);
		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Current Location Fetched");
		rs.setData(city);
		return rs;
	}

	@PostMapping("/addTrustedContact")
	public ResponseStructure<com.ride.goeasy.entity.TrustedContact> addTrustedContact(
			@RequestParam long mobNo,
			@RequestParam String name,
			@RequestParam String phone) {
		return customerService.addTrustedContact(mobNo, name, phone);
	}

	@GetMapping("/getTrustedContacts")
	public ResponseStructure<java.util.List<com.ride.goeasy.entity.TrustedContact>> getTrustedContacts(@RequestParam long mobNo) {
		return customerService.getTrustedContacts(mobNo);
	}

	@DeleteMapping("/deleteTrustedContact")
	public ResponseStructure<String> deleteTrustedContact(
			@RequestParam long mobNo,
			@RequestParam Integer contactId) {
		return customerService.deleteTrustedContact(mobNo, contactId);
	}

	@PostMapping("/sos")
	public ResponseStructure<String> triggerSOS(
			@RequestParam Integer bookingId,
			@RequestParam Double latitude,
			@RequestParam Double longitude) {
		return customerService.triggerSOS(bookingId, latitude, longitude);
	}

	@PutMapping("/updateProfile")
	public ResponseStructure<com.ride.goeasy.entity.Customer> updateProfile(
			@RequestParam long mobNo,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) String email) {
		return customerService.updateProfile(mobNo, name, email);
	}
}

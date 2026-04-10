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
	    public ResponseStructure<String> cancelRide(@RequestParam int bookingId){
	    	return customerService.cancelRide(bookingId);
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
}

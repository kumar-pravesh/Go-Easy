package com.ride.goeasy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.dto.CustomerDTO;
import com.ride.goeasy.dto.CustomerResponseDTO;
import com.ride.goeasy.dto.RideDetailsDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.entity.Userr;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.enums.BookingStatus;
import com.ride.goeasy.exception.BookingNotFoundException;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.PaymentRepo;
import com.ride.goeasy.repository.UserrRepo;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.ride.goeasy.dto.LocationResponse;
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
	private UserrRepo userrRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private MailService mailService;

	@Autowired
	PaymentRepo pr;
	@Autowired
	BookingService bs;
	@Autowired
	BookingRepo br;
	
	@Autowired
	private RestTemplate restTemplate;

	@Value("${locationiq.api.key}")
	private String apiKey;

	private final String REVERSE_API = "https://us1.locationiq.com/v1/reverse";
	
	
	
	// SAVE CUSTOMER

	public ResponseStructure<CustomerResponseDTO> saveCustomer(CustomerDTO dto) {

		if (userrRepo.existsByMobNo(dto.getMobno()) || customerRepo.existsByMobno(dto.getMobno())) {
			throw new RuntimeException("User with this mobile number already exists. Please Login.");
		}
		if (customerRepo.existsByEmail(dto.getEmail())) {
			throw new RuntimeException("An account with this email already exists. Please Login.");
		}

		Customer c = new Customer();
		c.setName(dto.getName());
		c.setAge(dto.getAge());
		c.setGender(dto.getGender());
		c.setMobno(dto.getMobno());
		c.setEmail(dto.getEmail());
		c.setPassword(dto.getPassword());
		c.setCurrentLocation(dto.getLat() + "," + dto.getLon());

		Userr userr = new Userr();
		userr.setMobno(c.getMobno());
		userr.setPassword(passwordEncoder.encode(dto.getPassword()));

		userr.setRole("CUSTOMER");

		userrRepo.save(userr);
		c.setUserr(userr);
		c.setPassword(userr.getPassword());
		// 🔹 SAVE (ONLY ONCE)

		Customer saved = customerRepo.save(c);
		
		
		//  CUSTOMER REGISTRATION MAIL
		mailService.sendCustomerRegistrationMail(
		    saved.getEmail(),
		    saved.getName()
		);


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

		Customer c = customerRepo.findByMobno(mobno)
				.orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobno));
		List<Booking> blist = c.getBookings();
		return bs.getBookingHistory(blist);

	}

	public ResponseStructure<Booking> getDriverActiveBooking(long mobNo) {
		Customer c = customerRepo.findByMobno(mobNo)
				.orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobNo));
		List<Booking> blist = c.getBookings();
		return bs.activeBookingHistory(blist);
	}

//		cancel ride by customer
	public ResponseStructure<String> cancelRide(int bookingId) {

		Booking b = br.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException("No any booking with given id:" + bookingId));
		Customer c = b.getCustomer();
		Vehicle v = b.getVehicle();
		Driver d = v.getDriver();
		c.setActiveBookingFlag(false);
		b.setActiveBookingFlag(false);
		b.setBookingStatus(BookingStatus.CANCELLED_BY_CUSTOMER);

		mailService.sendRideCancellationMail(c.getEmail(), String.valueOf(b.getId()));

		d.setDstatus("AVAILABLE");
		v.setAvlStatus("AVAILABLE"); // BUG FIX: vehicle was never freed on customer cancel
		Payment p = b.getPayment();

		int count = c.getCancellationCount() + 1;
		c.setCancellationCount(count);
		
		// Calculate 10% penalty for next ride
		if (b.getFare() != null) {
			double penalty = b.getFare() * 0.10;
			c.setPenaltyAmount((c.getPenaltyAmount() != null ? c.getPenaltyAmount() : 0.0) + penalty);
		}
		
		p.setPaymentStatus("Cancelled By customer");
		p.setAmount(0);
		p.setPaymentType("Ride cancelled");
		p.setCustomer(c);
		p.setBooking(b);
		p.setVehicle(v);

		pr.save(p);

		customerRepo.save(c);
		bookingRepo.save(b);
		driverRepo.save(d);
		vehicleRepo.save(v);

		ResponseStructure<String> rs = new ResponseStructure<String>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Booking Cancel Request Accepted");
		rs.setData("Booking cancelled by customer");

		return rs;
	}

	public ResponseStructure<java.util.List<Booking>> getScheduledRides(long mobNo) {
		Customer c = customerRepo.findByMobno(mobNo)
				.orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + mobNo));
		java.util.List<Booking> scheduled = br.findByCustomerMobnoAndScheduledTrueAndBookingStatus(
				c.getMobno(), com.ride.goeasy.enums.BookingStatus.BOOKED);
		ResponseStructure<java.util.List<Booking>> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Scheduled rides fetched");
		rs.setData(scheduled);
		return rs;
	}

	public String getCityFromCoordinates(double lat, double lon) {
		try {
			String url = REVERSE_API + "?key=" + apiKey + "&lat=" + lat + "&lon=" + lon + "&format=json";
			LocationResponse location = restTemplate.getForObject(url, LocationResponse.class);

			if (location != null && location.getAddress() != null) {
				String apiCity = location.getAddress().getCity();
				if (apiCity == null) apiCity = location.getAddress().getTown();
				if (apiCity == null) apiCity = location.getAddress().getVillage();
				if (apiCity == null) apiCity = location.getAddress().getCounty();
				if (apiCity == null) apiCity = location.getAddress().getState();

				return apiCity;
			}
		} catch (Exception e) {
			System.err.println("Location IQ API failed (Customer): " + e.getMessage());
		}
		return "Unknown Location";
	}

}

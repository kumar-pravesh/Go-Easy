package com.ride.goeasy.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.dto.LocationResponse;
import com.ride.goeasy.dto.PaymentByCashDTO;
import com.ride.goeasy.dto.PaymentByUpiDTO;
import com.ride.goeasy.dto.RideDetailsDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.entity.Userr;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.enums.BookingStatus;
import com.ride.goeasy.exception.BookingNotFoundException;
import com.ride.goeasy.exception.DriverNotFoundException;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.PaymentRepo;
import com.ride.goeasy.repository.UserrRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class DriverService {

	@Autowired
	DriverRepo driverRepo;
	@Autowired
	BookingRepo bookingRepo;

	@Autowired
	PaymentRepo paymentRepo;
	@Autowired
	CustomerRepo customerRepo;
	@Autowired
	VehicleRepo vehicleRepo;
	@Autowired
	BookingService bs;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private UserrRepo userrRepo;
	@Autowired
	private PasswordEncoder passwordEncoder;
	
	@Autowired
	private MailService mailService;


	@Value("${locationiq.api.key}")
	private String apiKey;

	private final String LOCATION_API = "https://us1.locationiq.com/v1/search";

	private final String MATRIX_API = "https://us1.locationiq.com/v1/matrix/driving/";

	private final String REVERSE_API = "https://us1.locationiq.com/v1/reverse";

	public ResponseStructure<Driver> saveDriverWithVehicle(Driver driver) {
		try {
			Vehicle vehicle = driver.getVehicle();

			if (vehicle == null) {
				throw new RuntimeException("Vehicle details are missing");
			}

			// 🔴 VALIDATION
			if (userrRepo.existsByMobNo(driver.getMobNo())) {
				throw new RuntimeException("User with this mobile number already exists. Please Login.");
			}

			if (vehicle.getLatitude() == null || vehicle.getLongitude() == null) {
				throw new RuntimeException("Latitude and Longitude are required");
			}

			// 🔹 Reverse Geocoding (lat/lon -> city)
			try {
				String url = REVERSE_API + "?key=" + apiKey + "&lat=" + vehicle.getLatitude() + "&lon=" + vehicle.getLongitude()
						+ "&format=json";

				LocationResponse location = restTemplate.getForObject(url, LocationResponse.class);

				if (location != null && location.getAddress() != null) {
					String apiCity = location.getAddress().getCity();
					if (apiCity == null) apiCity = location.getAddress().getTown();
					if (apiCity == null) apiCity = location.getAddress().getCounty();
					if (apiCity == null) apiCity = location.getAddress().getState();
					
					if (apiCity != null) {
						vehicle.setCity(apiCity);
					}
				}
			} catch (Exception e) {
				System.err.println("Location IQ API failed: " + e.getMessage());
				// Fallback: If city was provided by frontend, keep it. 
				// If not, set default.
				if(vehicle.getCity() == null || vehicle.getCity().isEmpty()) {
					vehicle.setCity("Unknown City"); 
				}
			}

			// 🔹 RELATIONSHIP
			vehicle.setDriver(driver);
			vehicle.setAvlStatus("AVAILABLE"); // Fix: Ensure vehicle is discoverable
			
			// Use user provided speed or default
			if (vehicle.getAvgspeed() == null || vehicle.getAvgspeed() <= 0) {
			    vehicle.setAvgspeed(45.0);
			}
			
			driver.setVehicle(vehicle);

			Userr userr = new Userr();
			userr.setMobno(driver.getMobNo());
			userr.setPassword(passwordEncoder.encode(driver.getPassword()));
			userr.setRole("DRIVER");

			userrRepo.save(userr);
			driver.setPassword(userr.getPassword());
			driver.setUserr(userr);
			
			// 🔹 SAVE (ONLY ONCE)
			Driver savedDriver = driverRepo.save(driver);
			
			//  DRIVER REGISTRATION MAIL
			try {
				mailService.sendDriverRegistrationMail(
					savedDriver.getMailId(),
					savedDriver.getDname()
				);
			} catch (Exception e) {
				System.err.println("Mail sending failed: " + e.getMessage());
			}

			ResponseStructure<Driver> rs = new ResponseStructure<>();
			rs.setStatusCode(HttpStatus.CREATED.value());
			rs.setMessage("Driver Saved Successfully");
			rs.setData(savedDriver);

			return rs;
			
		} catch (RuntimeException re) {
			throw re; // Propagate RuntimeExceptions (including our "User exists")
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Internal Error: " + e.getMessage());
		}
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

	private byte[] generateQrCode(String upiString) {
		try {
			String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + upiString;

			RestTemplate restTemplate = new RestTemplate();

			byte[] qr = restTemplate.getForObject(qrUrl, byte[].class);

			return qr;
//	        return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("QR generation failed");
		}
	}

	// payment by cash

	public ResponseStructure<PaymentByCashDTO> confirmPaymnetByCash(int bookingId, String paymentType) {
		return confirmPay(bookingId, paymentType);

	}

	// payment confirmation method

	private ResponseStructure<PaymentByCashDTO> confirmPay(int bookingId, String paymentType) {
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found with id:" + bookingId));
		b.setBookingStatus(BookingStatus.COMPLETED);

		Customer c = b.getCustomer();
		c.setActiveBookingFlag(false);
		c.setCancellationCount(0);
		c.setPenaltyAmount(0.0);

		Vehicle v = b.getVehicle();
		v.setAvlStatus("AVAILABLE");
		b.setActiveBookingFlag(false);

		Payment p = b.getPayment();
		p.setVehicle(v);
		p.setCustomer(c);
		p.setBooking(b);
		p.setPaymentStatus("PAID");
		p.setPaymentType(paymentType);
		p.setAmount(b.getFare());

		b.setPayment(p);
		paymentRepo.save(p);
		bookingRepo.save(b);
		customerRepo.save(c);
		vehicleRepo.save(v);

		PaymentByCashDTO pdto = new PaymentByCashDTO();
		pdto.setBookingId(b.getId());
		pdto.setCustomerId(c.getId());
		pdto.setDriverId(v.getId());
		pdto.setAmountPaid(b.getFare());
		pdto.setPaymentType(paymentType);
		pdto.setPaymentStatus("PAID");
		
		
//		payment confirmation mail logic
		try {
			mailService.sendPaymentConfirmationMail(
			    c.getEmail(),
			    b.getFare(),
			    "CASH-" + b.getId()
			);
		} catch (Exception e) {
			System.err.println("Mail sending failed: " + e.getMessage());
		}


		ResponseStructure<PaymentByCashDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Ride completed ->Amount paid");
		rs.setData(pdto);

		return rs;
	}

	public ResponseStructure<BookingHistoryDTO> getDriverBookingHistory(long mobNo) {

		Driver d = driverRepo.findByMobNo(mobNo)
				.orElseThrow(() -> new DriverNotFoundException("Driver Not Found with Mobile: " + mobNo));
		List<Booking> blist = d.getDblist();
		if (blist == null || blist.isEmpty()) {
			throw new BookingNotFoundException("No bookings found for driver");
		}
		return bs.getBookingHistory(blist);

	}

	public ResponseStructure<Booking> getDriverActiveBooking(long mobNo) {
		Driver d = driverRepo.findByMobNo(mobNo)
				.orElseThrow(() -> new DriverNotFoundException("Driver Not Found with Mobile: " + mobNo));
		List<Booking> blist = d.getDblist();
		return bs.activeBookingHistory(blist);
	}
	
	// qr generation logic

	public ResponseStructure<PaymentByUpiDTO> confirmPaymentByUPI(int bookingId) {

	    Booking b = bookingRepo.findById(bookingId)
	            .orElseThrow(() -> new BookingNotFoundException("Booking not found"));

	    Vehicle v = b.getVehicle();
	    Driver d = v.getDriver();
	    Customer c = b.getCustomer();

	    c.setActiveBookingFlag(false);
	    c.setCancellationCount(0);

	    double fare = b.getFare();

	    String upiString = "upi://pay?pa=" + d.getUpiId()
	            + "&pn=" + URLEncoder.encode(d.getDname(), StandardCharsets.UTF_8)
	            + "&am=" + fare
	            + "&cu=INR";

	    byte[] qrBytes = generateQrCode(upiString);
	    String base64Qr = Base64.getEncoder().encodeToString(qrBytes);

	    Payment p = b.getPayment();
	    if (p == null) {
	        p = new Payment();
	        p.setBooking(b);
	        b.setPayment(p);
	    }

	    if ("PAID".equalsIgnoreCase(p.getPaymentStatus())) {
	        throw new RuntimeException("Payment already completed");
	    }

	    p.setPaymentType("UPI");
	    p.setPaymentStatus("PENDING");
	    p.setAmount(fare);

	    paymentRepo.save(p);

	    PaymentByUpiDTO dto = new PaymentByUpiDTO();
	    dto.setBookingId(b.getId());
	    dto.setCustomerId(c.getId());
	    dto.setDriverId(d.getId());
	    dto.setAmount(fare);
	    dto.setPaymentType("UPI");
	    dto.setPaymentStatus("PENDING");
	    dto.setQr(base64Qr);

	    ResponseStructure<PaymentByUpiDTO> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Scan QR to pay via UPI");
	    rs.setData(dto);

	    return rs;
	}

	
	
	
	// confirm upi payment success logic
	public ResponseStructure<PaymentByUpiDTO> confirmUpiPaymentSuccess(int bookingId) {

		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new BookingNotFoundException("Booking not found"));

		b.setBookingStatus(BookingStatus.COMPLETED);

		Customer c = b.getCustomer();
		c.setActiveBookingFlag(false);
		b.setActiveBookingFlag(false); // Clear the active booking flag!
		c.setCancellationCount(0); // Reset count on success
		c.setPenaltyAmount(0.0); // Reset penalty on success

		Vehicle v = b.getVehicle();
		v.setAvlStatus("AVAILABLE");

		Payment p = b.getPayment();
		p.setPaymentStatus("PAID");
		p.setPaymentType("UPI");
		p.setAmount(b.getFare());

		paymentRepo.save(p);
		bookingRepo.save(b);
		customerRepo.save(c);
		vehicleRepo.save(v);

		PaymentByUpiDTO dto = new PaymentByUpiDTO();
		dto.setBookingId(b.getId());
		dto.setCustomerId(c.getId());
		dto.setDriverId(v.getDriver().getId());
		dto.setAmount(b.getFare());
		dto.setPaymentType("UPI");
		dto.setPaymentStatus("PAID");
		
		
//		mail
		try {
			mailService.sendPaymentConfirmationMail(
			    c.getEmail(),
			    b.getFare(),
			    "UPI-" + b.getId()
			);
		} catch (Exception e) {
			System.err.println("Mail sending failed: " + e.getMessage());
		}


		ResponseStructure<PaymentByUpiDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("UPI Payment Successful");
		rs.setData(dto);

		return rs;
	}

	public ResponseStructure<String> updateDriverStatus(long mobNo, String status) {
		Driver d = driverRepo.findByMobNo(mobNo)
				.orElseThrow(() -> new DriverNotFoundException("Driver Not Found with Mobile: " + mobNo));

		d.setDstatus(status);
		
		Vehicle v = d.getVehicle();
		if (v != null) {
			if ("ONLINE".equalsIgnoreCase(status)) {
				v.setAvlStatus("AVAILABLE");
			} else if ("OFFLINE".equalsIgnoreCase(status)) {
				v.setAvlStatus("OFFLINE");
			}
			vehicleRepo.save(v);
		}
		
		driverRepo.save(d);

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Driver status updated to " + status);
		rs.setData(status);

		return rs;
	}

}

package com.ride.goeasy.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.RideDetailsDTO;
import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.dto.LocationResponse;
import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.enums.BookingStatus;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.exception.InvalidLocationException;
import com.ride.goeasy.exception.VehicleNotFoundException;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.PaymentRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class BookingService {

	@Autowired
	private BookingRepo bookingRepo;

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private VehicleRepo vr;
	@Autowired
	private DriverRepo dr;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private PaymentRepo paymentRepo;
	@Value("${locationiq.api.key}")
	private String apiKey;
	
	
	@Autowired
	private MailService mailService;
	
	
	@Autowired
	private OtpService otpService;

	@Autowired
	private com.ride.goeasy.service.CompanyService companyService;


	public ResponseStructure<Booking> bookVehicle(long mobno, BookingRequestDTO bookingRequestDTO) {

		// STEP 1: Prevent booking if same city
		if (bookingRequestDTO.getSourceLocation().trim()
				.equalsIgnoreCase(bookingRequestDTO.getDestinationLocation().trim())) {
			throw new InvalidLocationException("Source and destination cannot be the same.");
		}

		// STEP 2: Fetch Customer
		Customer cust = customerRepo.findByMobno(mobno)
				.orElseThrow(() -> new CustomerNotFoundException("Customer Not Found"));

		// STEP 3: Fetch Vehicle
		Vehicle vehicle = vr.findByVehicleNumber(bookingRequestDTO.getVehicleNumber())
				.orElseThrow(() -> new VehicleNotFoundException("Vehicle Not Found"));

		// ⭐⭐ STEP 4: Convert Source City → Coordinates ⭐⭐
		String sourceCity = bookingRequestDTO.getSourceLocation().trim();
		String srcUrl = "https://us1.locationiq.com/v1/search?key=" + apiKey + "&q=" + sourceCity + "&format=json";

		// Safe to use coordinates now
		double latitude = 12.9716; // Default Bangalore
		double longitude = 77.5946;

		try {
			LocationResponse[] srcResponse = restTemplate.getForObject(srcUrl, LocationResponse[].class);

			// If response is null or empty array, use default
			if (srcResponse != null && srcResponse.length > 0) {
				// Accept only if type = "city"
				boolean validCity = false;
				for (LocationResponse loc : srcResponse) {
					String type = loc.getType();
					if ("city".equalsIgnoreCase(type) || "town".equalsIgnoreCase(type)
							|| "administrative".equalsIgnoreCase(type)) {
						validCity = true;
						break;
					}
				}
				
				if(validCity) {
					latitude = Double.parseDouble(srcResponse[0].getLat());
					longitude = Double.parseDouble(srcResponse[0].getLon());
				}
			}
		} catch (Exception e) {
			System.err.println("Location API Timeout/Error: " + e.getMessage() + ". Using default coordinates.");
			// Fallback to default (Bangalore) matches initialized values
		}

		// STEP 5: Create Booking

		if (cust.isActiveBookingFlag()) {
			throw new RuntimeException("Your current ride is not completed. You cannot book a new one until the active trip is finished.");
		}

		Booking b = new Booking();
		Payment p = new Payment();

		p.setPaymentStatus("PENDING");
		p.setPaymentType("Not Decided"); // or ONLINE

		paymentRepo.save(p);

		b.setCustomer(cust);
		b.setVehicle(vehicle);
		b.setSourceLocation(sourceCity);
		b.setDestinationLocation(bookingRequestDTO.getDestinationLocation());
		b.setFare(bookingRequestDTO.getFare());
		b.setDistance(bookingRequestDTO.getDistance());
		b.setEstimatedTime(bookingRequestDTO.getEstimatedTime());
		// Scheduled ride handling
		boolean isScheduled = bookingRequestDTO.getScheduledTime() != null
				&& !bookingRequestDTO.getScheduledTime().isBlank();
		if (isScheduled) {
			java.time.LocalDateTime scheduledAt = java.time.LocalDateTime.parse(bookingRequestDTO.getScheduledTime());
			if (scheduledAt.isBefore(java.time.LocalDateTime.now().plusMinutes(30))) {
				throw new RuntimeException("Scheduled time must be at least 30 minutes in the future.");
			}
			b.setScheduled(true);
			b.setScheduledTime(scheduledAt);
			b.setFareLockedAtBooking(true);
		}

		b.setBookingStatus(BookingStatus.BOOKED);
		b.setPayment(p);
		b.setActiveBookingFlag(true);
		b.setPaymentMode(bookingRequestDTO.getPaymentMode());
		b.setRideDate(java.time.LocalDate.now());
		cust.getBookings().add(b);
		Driver d = vehicle.getDriver();

		// Check if vehicle is actually available (Double Check)
		if(!"AVAILABLE".equalsIgnoreCase(vehicle.getAvlStatus())) {
			throw new RuntimeException("Vehicle is currently busy or unavailable.");
		}
		
		d.getDblist().add(b);
		
		// For immediate rides: lock vehicle now. For scheduled: lock 30 min before via scheduler.
		if (!isScheduled) {
			vehicle.setAvlStatus("ON_RIDE");
		}
		
		
		// ===== START OTP GENERATION =====
		String startOtp = otpService.generateOtp();
		b.setStartOtp(startOtp);
		b.setStartOtpVerified(false);

		// Send Start OTP to CUSTOMER
		mailService.sendMail(
		    cust.getEmail(),
		    "Ride Start OTP - GoEasy",
		    "Hello " + cust.getName() + ",\n\n"
		  + "Your Ride Start OTP is: " + startOtp + "\n\n"
		  + "Please share this OTP with the driver to start the ride.\n\n"
		  + "– GoEasy Team"
		);


		// Corporate wallet deduction (best-effort, does not block booking)
		if (bookingRequestDTO.isUseCorporateWallet() && b.getFare() != null) {
			try {
				companyService.deductCorporateFare(cust, b.getFare());
			} catch (Exception e) {
				System.err.println("Corporate wallet deduction failed: " + e.getMessage());
			}
		}

		// STEP 6: Save
		Booking savedBooking = bookingRepo.save(b);
		customerRepo.save(cust);
		vr.save(vehicle);
		dr.save(d);
		
		//  BOOKING CONFIRMATION MAIL 
		mailService.sendBookingConfirmationMail(
			    cust.getEmail(),
			    cust.getName(),
			    savedBooking.getId(),
			    d.getDname(),
			    d.getMobNo()
			);



		// STEP 7: Response
		ResponseStructure<Booking> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.CREATED.value());
		rs.setMessage(" Vehicle booked successfully");
		rs.setData(savedBooking);
		return rs;

	}

/// booking history for driver and customer
	public ResponseStructure<BookingHistoryDTO> getBookingHistory(List<Booking> blist) {
		List<RideDetailsDTO> list = new ArrayList<RideDetailsDTO>();
		double totalAmount = 0;
		for (Booking b : blist) {
			if (b.getBookingStatus() != BookingStatus.COMPLETED) {
				continue;
			}

			RideDetailsDTO rdto = new RideDetailsDTO();
			rdto.setBookingId(b.getId());
			rdto.setSourceLocation(b.getSourceLocation());
			rdto.setDestinationLocation(b.getDestinationLocation());
			rdto.setBookingStatus(b.getBookingStatus().name());
			rdto.setDistance(b.getDistance());
			rdto.setFare(b.getFare());
			rdto.setRideDate(b.getRideDate());
			totalAmount += b.getFare();
			list.add(rdto);
		}
		BookingHistoryDTO bookingHistoryDTO = new BookingHistoryDTO();
		bookingHistoryDTO.setRlist(list);
		bookingHistoryDTO.setTotalAmt(totalAmount);

		ResponseStructure<BookingHistoryDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Booking History");
		rs.setData(bookingHistoryDTO);

		return rs;

	}

	// active booking for driver and customer

	public ResponseStructure<Booking> activeBookingHistory(List<Booking> blist) {

		Booking latestActive = null;
		
		for (Booking b : blist) {
			if (b.isActiveBookingFlag()) {
			    // Always keep reference to the found booking, effectively finding the last one (latest)
			    // assuming list is ordered. Ideally compare IDs.
			    if(latestActive == null || b.getId() > latestActive.getId()) {
			        latestActive = b;
			    }
			}
		}
		
		if (latestActive != null) {
            ResponseStructure<Booking> rs = new ResponseStructure<>();
            rs.setStatusCode(HttpStatus.OK.value());
            rs.setMessage("Ongoing- not completed yet");
            rs.setData(latestActive); // Return FULL object so frontend has OTP/Driver
            return rs;
		}
		
		ResponseStructure<Booking> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("No active booking");
		rs.setData(null);

		return rs;
	}

	// Booking Cancel by Driver

	public ResponseStructure<String> cancelBookingByDriver(int bookingId) {
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));

		Driver driver = null;
		if (booking.getVehicle() != null) {
			driver = booking.getVehicle().getDriver();
			// Free up vehicle immediately
			booking.getVehicle().setAvlStatus("AVAILABLE");
		}

		Customer customer = booking.getCustomer();

		// Track daily cancellations
		if (driver != null) {
			java.time.LocalDate today = java.time.LocalDate.now();
			if (driver.getLastCancelDate() == null || !driver.getLastCancelDate().equals(today)) {
				driver.setDailyCancelCount(0);
				driver.setLastCancelDate(today);
			}
			driver.setDailyCancelCount(driver.getDailyCancelCount() + 1);
		}

		// Cancel booking
		booking.setBookingStatus(BookingStatus.CANCELLED_BY_DRIVER);
		booking.setActiveBookingFlag(false);

		// Reset customer active booking if exists
		if (customer != null) {
			customer.setActiveBookingFlag(false);
		}

		// Block driver if more than 4 cancels in a day (only if driver exists)
		if (driver != null && driver.getDailyCancelCount() > 4) {
			driver.setDstatus("TEMPORARY_BLOCKED");
			if (booking.getVehicle() != null) {
				booking.getVehicle().setAvlStatus("TEMPORARY_BLOCKED");
			}
		}

		// Save updates
		bookingRepo.save(booking);
		if (driver != null) dr.save(driver);
		if (customer != null) customerRepo.save(customer);

		// Cancellation email (Best Effort)
		if (customer != null) {
			try {
				mailService.sendRideCancellationMail(customer.getEmail(), String.valueOf(booking.getId()));
			} catch (Exception e) {
				System.out.println("Cancellation mail failed: " + e.getMessage());
			}
		}

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Booking cancelled by driver");
		rs.setData(null);

		return rs;
	}
	
	
//	start ride otp logic
	public ResponseStructure<String> startRide(int bookingId, String otp) {

	    Booking booking = bookingRepo.findById(bookingId)
	        .orElseThrow(() -> new RuntimeException("Booking not found"));
	    
	    
	    if (booking.getBookingStatus() != BookingStatus.BOOKED) {
	        throw new RuntimeException("Ride cannot be started");
	    }

	    // DEBUGGING LOG (Print to console)
	    System.out.println("DEBUG OTP: DB='" + booking.getStartOtp() + "' | INPUT='" + otp + "'");
	    
	    String dbOtp = booking.getStartOtp();
	    if (dbOtp == null) dbOtp = "";
	    
	    if (!dbOtp.trim().equals(otp.trim())) {
	        throw new RuntimeException("Invalid Start OTP");
	    }

	    booking.setStartOtpVerified(true);
	    booking.setBookingStatus(BookingStatus.ONGOING);

	    bookingRepo.save(booking);

	    ResponseStructure<String> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Ride started successfully");
	    rs.setData(null);

	    return rs;
	}
	
	
	// end ride with otp logic
	public ResponseStructure<String> generateEndOtp(int bookingId) {

	    Booking booking = bookingRepo.findById(bookingId)
	        .orElseThrow(() -> new RuntimeException("Booking not found"));
	    
	    // Validate if ride is ongoing
	    if (booking.getBookingStatus() != BookingStatus.ONGOING) {
	        throw new RuntimeException("Ride cannot be ended now");
	    }

	    String endOtp = otpService.generateOtp();
	    booking.setEndOtp(endOtp);
	    booking.setEndOtpVerified(false);

	    bookingRepo.save(booking);

	    Customer cust = booking.getCustomer();

    // Send End OTP email (best effort)
    try {
        mailService.sendMail(
            cust.getEmail(),
            "Ride Completion OTP - GoEasy",
            "Hello " + cust.getName() + ",\n\n"
          + "Your Ride Completion OTP is: " + endOtp + "\n\n"
          + "Please share this OTP with the driver to complete the ride.\n\n"
          + "– GoEasy Team"
        );
        System.out.println("End OTP email sent to: " + cust.getEmail() + " | OTP: " + endOtp);
    } catch (Exception e) {
        System.err.println("Failed to send End OTP email: " + e.getMessage());
    }

	    ResponseStructure<String> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Ride completion OTP sent to customer");
	    rs.setData(endOtp);

	    return rs;
	}

	
//	complete ride using otp
	public ResponseStructure<String> completeRide(int bookingId, String otp) {

	    Booking booking = bookingRepo.findById(bookingId)
	        .orElseThrow(() -> new RuntimeException("Booking not found"));

	    // Validate if ride is ongoing
	    if (booking.getBookingStatus() != BookingStatus.ONGOING) {
	        throw new RuntimeException("Ride cannot be completed now");
	    }

	    // OTP validation
	    String dbOtp = booking.getEndOtp();
	    if (dbOtp == null) dbOtp = "";

	    if (!dbOtp.trim().equals(otp != null ? otp.trim() : "")) {
	        throw new RuntimeException("Invalid Completion OTP");
	    }

	    booking.setEndOtpVerified(true);
	    booking.setBookingStatus(BookingStatus.COMPLETED);
	    booking.setActiveBookingFlag(true); // Keep active until payment confirmed by driver

	    // Keep customer active
	    Customer cust = booking.getCustomer();
	    cust.setActiveBookingFlag(true); // Keep active until payment confirmed

	    bookingRepo.save(booking);
	    customerRepo.save(cust);

	    ResponseStructure<String> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Ride completed! Proceed to payment.");
	    rs.setData(null);

	    return rs;
	}

	// ===================== RECORDING CONSENT =====================

	public ResponseStructure<String> requestRecording(int bookingId) {
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));

		if (!BookingStatus.BOOKED.equals(b.getBookingStatus()) &&
			!BookingStatus.ONGOING.equals(b.getBookingStatus())) {
			throw new RuntimeException("Recording can only be requested during an active ride.");
		}
		if (!"NONE".equals(b.getRecordingConsent())) {
			throw new RuntimeException("Recording consent already requested.");
		}

		b.setRecordingConsent("REQUESTED");
		b.setRecordingRequestedAt(java.time.LocalDateTime.now());
		bookingRepo.save(b);

		try {
			mailService.sendMail(
				b.getVehicle().getDriver().getMailId(),
				"Recording Consent Request — GoEasy",
				"Passenger " + b.getCustomer().getName() + " has requested to audio-record this ride for safety.\n"
			  + "Both parties must consent. Please respond via the app.\n"
			  + "Recording is stored for 24 hrs, then auto-deleted.\n\n– GoEasy Team"
			);
		} catch (Exception ignored) {}

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Recording consent requested. Waiting for driver approval.");
		rs.setData("REQUESTED");
		return rs;
	}

	public ResponseStructure<String> respondRecording(int bookingId, boolean accept) {
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));

		if (!"REQUESTED".equals(b.getRecordingConsent())) {
			throw new RuntimeException("No pending recording request for this booking.");
		}

		b.setRecordingConsent(accept ? "ACTIVE" : "REJECTED");
		bookingRepo.save(b);

		if (accept) {
			try {
				mailService.sendMail(
					b.getCustomer().getEmail(),
					"Recording Consent Accepted — GoEasy",
					"Your driver accepted the safety recording request.\n"
				  + "The recording is stored securely for 24 hours and permanently deleted after.\n\n– GoEasy Team"
				);
			} catch (Exception ignored) {}
		}

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage(accept ? "Recording is ACTIVE. Stored 24 hrs, then auto-deleted." : "Driver declined recording.");
		rs.setData(b.getRecordingConsent());
		return rs;
	}
}

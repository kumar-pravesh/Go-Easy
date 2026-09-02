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

	@Autowired
	private com.ride.goeasy.repository.PromoCodeRepo promoCodeRepo;

	@Autowired
	private com.ride.goeasy.repository.SOSEventRepository sosEventRepo;

	@Autowired
	private com.ride.goeasy.repository.TrustedContactRepository trustedContactRepo;


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
		// ⭐ Server-Side Fare Calculation (Sprint 1) ⭐
		double lockedDistance = bookingRequestDTO.getDistance();
		double lockedRate = (vehicle.getPricePerKm() != null && vehicle.getPricePerKm() < 25) ? vehicle.getPricePerKm() : 12.0;
		double baseFare = 100.0;
		double distanceFare = lockedDistance * lockedRate;
		double penaltyAmount = (cust.getPenaltyAmount() != null) ? cust.getPenaltyAmount() : 0.0;
		double finalFare = baseFare + distanceFare + penaltyAmount;

		b.setBaseFare(baseFare);
		b.setDistanceFare(distanceFare);
		b.setPenaltyAmount(penaltyAmount);
		b.setPricePerKm(lockedRate);
		b.setFare(finalFare);
		b.setFareLocked(true);
		b.setDistance(lockedDistance);
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

		Driver d = vehicle.getDriver();
		b.setBookingStatus(BookingStatus.BOOKED);
		b.setPayment(p);
        b.setDriver(d);
		b.setPaymentMode(bookingRequestDTO.getPaymentMode());
		b.setRideDate(java.time.LocalDate.now());
		cust.getBookings().add(b);

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

	public ResponseStructure<String> cancelBookingByDriver(int bookingId, String reason) {
		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));

		Driver driver = null;
		if (booking.getVehicle() != null) {
			driver = booking.getVehicle().getDriver();
			// Free up vehicle immediately
			booking.getVehicle().setAvlStatus("AVAILABLE");
			vr.save(booking.getVehicle());
			
			if (driver != null) {
				driver.getDblist().remove(booking);
			}
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

		// Deduct 5 points from reliability score
		if (driver != null) {
			double score = driver.getReliabilityScore() != null ? driver.getReliabilityScore() : 100.0;
			driver.setReliabilityScore(Math.max(0.0, score - 5.0));
		}

		// Block driver if more than 4 cancels in a day
		if (driver != null && driver.getDailyCancelCount() > 4) {
			driver.setDstatus("TEMPORARY_BLOCKED");
			if (driver.getVehicle() != null) {
				driver.getVehicle().setAvlStatus("TEMPORARY_BLOCKED");
				vr.save(driver.getVehicle());
			}
		}
		
		if (driver != null) dr.save(driver);

		// AUTO-REMATCH LOGIC
		// Keep booking as BOOKED to allow another driver to accept it (or nearest driver logic)
		booking.setBookingStatus(BookingStatus.BOOKED);
		booking.setDriver(null);
		booking.setVehicle(null);
		booking.setCancelReason(reason);
		booking.setCancelledBy("DRIVER");
		// Do not disable customer activeBookingFlag, they are still waiting
		
		bookingRepo.save(booking);

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Booking cancelled by driver. Auto-rematching customer.");
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
	    booking.setRideStartedAt(java.time.LocalDateTime.now());

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

		// Reward driver reliability (+1 point, max 100)
		Driver driver = booking.getVehicle() != null ? booking.getVehicle().getDriver() : null;
		if (driver != null) {
			double score = driver.getReliabilityScore() != null ? driver.getReliabilityScore() : 100.0;
			driver.setReliabilityScore(Math.min(100.0, score + 1.0));
			dr.save(driver);
		}

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

	public ResponseStructure<com.ride.goeasy.dto.RideReceiptDTO> getRideReceipt(int bookingId) {
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));

		com.ride.goeasy.dto.RideReceiptDTO dto = new com.ride.goeasy.dto.RideReceiptDTO();
		dto.setBookingId(b.getId());
		dto.setReceiptNumber("GE-" + b.getRideDate() + "-" + b.getId());
		dto.setRideDate(b.getRideDate());
		dto.setSourceLocation(b.getSourceLocation());
		dto.setDestinationLocation(b.getDestinationLocation());
		dto.setDistance(b.getDistance());
		dto.setEstimatedTime(b.getEstimatedTime());
		dto.setBookingStatus(b.getBookingStatus() != null ? b.getBookingStatus().name() : null);

		Customer customer = b.getCustomer();
		if (customer != null) {
			dto.setCustomerName(customer.getName());
			dto.setCustomerMobile(customer.getMobno());
		}

		Vehicle vehicle = b.getVehicle();
		if (vehicle != null) {
			dto.setVehicleNumber(vehicle.getVehicleNumber());
			dto.setVehicleModel(vehicle.getVehicleModel());
			Driver driver = vehicle.getDriver();
			if (driver != null) {
				dto.setDriverName(driver.getDname());
				dto.setDriverMobile(driver.getMobNo());
			}
		}

		dto.setBaseFare(b.getBaseFare());
		dto.setDistanceFare(b.getDistanceFare());
		dto.setPenaltyAmount(b.getPenaltyAmount());
		dto.setWaitingCharge(b.getWaitingCharge());
		dto.setNightCharge(b.getNightCharge());
		dto.setPlatformFee(b.getPlatformFee());
		dto.setTax(b.getTax());
		dto.setDiscount(b.getDiscount());
		dto.setPricePerKm(b.getPricePerKm());
		dto.setTotalFare(b.getFare());
		dto.setFareLocked(b.isFareLocked());
		dto.setPaymentMode(b.getPaymentMode());

		Payment payment = b.getPayment();
		if (payment != null) {
			dto.setPaymentType(payment.getPaymentType());
			dto.setPaymentStatus(payment.getPaymentStatus());
			dto.setAmountPaid(payment.getAmount());
		}

		ResponseStructure<com.ride.goeasy.dto.RideReceiptDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Ride receipt fetched");
		rs.setData(dto);
		return rs;
	}

	public ResponseStructure<com.ride.goeasy.dto.PublicTrackingDTO> getPublicTracking(Integer bookingId) {
		Booking b = bookingRepo.findById(bookingId).orElseThrow(() -> new com.ride.goeasy.exception.BookingNotFoundException("Booking not found"));
		
		com.ride.goeasy.dto.PublicTrackingDTO dto = new com.ride.goeasy.dto.PublicTrackingDTO();
		dto.setBookingId(b.getId());
		dto.setSourceLocation(b.getSourceLocation());
		dto.setDestinationLocation(b.getDestinationLocation());
		if (b.getBookingStatus() != null) {
			dto.setBookingStatus(b.getBookingStatus().name());
		}
		
		if (b.getVehicle() != null) {
			dto.setVehicleModel(b.getVehicle().getVehicleModel());
			dto.setVehicleNumber(b.getVehicle().getVehicleNumber());
			dto.setDriverLatitude(b.getVehicle().getLatitude());
			dto.setDriverLongitude(b.getVehicle().getLongitude());
			if (b.getVehicle().getDriver() != null) {
				dto.setDriverName(b.getVehicle().getDriver().getDname());
			}
		}

		ResponseStructure<com.ride.goeasy.dto.PublicTrackingDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Tracking details fetched safely");
		rs.setData(dto);
		return rs;
	}

	// ─── Rate Driver (by customer) ─────────────────────────────────────
	public ResponseStructure<String> rateDriver(int bookingId, int rating) {
		if (rating < 1 || rating > 5) throw new RuntimeException("Rating must be between 1 and 5");
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));
		if (b.getBookingStatus() != BookingStatus.COMPLETED) {
			throw new RuntimeException("Can only rate after ride is completed");
		}
		if (b.getDriverRating() != null) {
			throw new RuntimeException("You have already rated this ride");
		}
		b.setDriverRating(rating);
		bookingRepo.save(b);

		// Update driver's running average on the Driver entity
		Driver driver = b.getDriver();
		if (driver == null && b.getVehicle() != null) driver = b.getVehicle().getDriver();
		if (driver != null) {
			int count = driver.getTotalRatings() == null ? 0 : driver.getTotalRatings();
			double current = driver.getDriverRating() == null ? 0.0 : driver.getDriverRating();
			double newAvg = ((current * count) + rating) / (count + 1);
			driver.setDriverRating(Math.round(newAvg * 10.0) / 10.0);
			driver.setTotalRatings(count + 1);
			dr.save(driver);
		}

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Driver rated successfully");
		rs.setData("Rating: " + rating + "/5");
		return rs;
	}

	// ─── Rate Customer (by driver) ─────────────────────────────────────
	public ResponseStructure<String> rateCustomer(int bookingId, int rating) {
		if (rating < 1 || rating > 5) throw new RuntimeException("Rating must be between 1 and 5");
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));
		if (b.getBookingStatus() != BookingStatus.COMPLETED) {
			throw new RuntimeException("Can only rate after ride is completed");
		}
		if (b.getCustomerRating() != null) {
			throw new RuntimeException("You have already rated this customer");
		}
		b.setCustomerRating(rating);
		bookingRepo.save(b);

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Customer rated successfully");
		rs.setData("Rating: " + rating + "/5");
		return rs;
	}

	// ─── Validate Promo Code ────────────────────────────────────────────
	public ResponseStructure<java.util.Map<String, Object>> validatePromoCode(String code, double fare) {
		com.ride.goeasy.entity.PromoCode promo = promoCodeRepo.findByCodeIgnoreCase(code)
				.orElseThrow(() -> new RuntimeException("Invalid promo code"));

		if (!promo.isActive()) throw new RuntimeException("Promo code is no longer active");
		if (promo.getValidUntil() != null && promo.getValidUntil().isBefore(java.time.LocalDate.now())) {
			throw new RuntimeException("Promo code has expired");
		}
		if (promo.getUsedCount() >= promo.getUsageLimit()) {
			throw new RuntimeException("Promo code usage limit reached");
		}
		if (fare < promo.getMinFare()) {
			throw new RuntimeException("Minimum fare of ₹" + promo.getMinFare() + " required");
		}

		double discountAmount = (fare * promo.getDiscountPercent()) / 100.0;
		if (promo.getMaxDiscountAmount() > 0) {
			discountAmount = Math.min(discountAmount, promo.getMaxDiscountAmount());
		}
		discountAmount = Math.round(discountAmount * 100.0) / 100.0;

		java.util.Map<String, Object> data = new java.util.HashMap<>();
		data.put("code", promo.getCode());
		data.put("discountAmount", discountAmount);
		data.put("discountPercent", promo.getDiscountPercent());
		data.put("finalFare", Math.max(0, fare - discountAmount));
		data.put("description", promo.getDescription());

		ResponseStructure<java.util.Map<String, Object>> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("Promo code valid");
		rs.setData(data);
		return rs;
	}

	// ─── SOS System ────────────────────────────────────────────
	public ResponseStructure<String> triggerSOS(int bookingId, double latitude, double longitude) {
		Booking b = bookingRepo.findById(bookingId)
				.orElseThrow(() -> new RuntimeException("Booking not found"));

		if (b.getBookingStatus() != BookingStatus.ONGOING) {
			throw new RuntimeException("SOS can only be triggered during an ongoing ride");
		}

		com.ride.goeasy.entity.SOSEvent event = new com.ride.goeasy.entity.SOSEvent(b, latitude, longitude);
		sosEventRepo.save(event);

		Customer customer = b.getCustomer();
		java.util.List<com.ride.goeasy.entity.TrustedContact> contacts = trustedContactRepo.findByCustomer(customer);

		if (contacts != null && !contacts.isEmpty()) {
			String trackLink = "http://localhost:5173/public-track/" + b.getId(); // Assuming standard frontend URL
			String subject = "🚨 EMERGENCY SOS: " + customer.getName() + " needs help! 🚨";
			String body = "Hello,\n\n"
					+ customer.getName() + " has triggered an SOS alert during their Go-Easy ride.\n\n"
					+ "Driver: " + b.getVehicle().getDriver().getDname() + "\n"
					+ "Vehicle: " + b.getVehicle().getVehicleModel() + " (" + b.getVehicle().getVehicleNumber() + ")\n"
					+ "Route: " + b.getSourceLocation() + " -> " + b.getDestinationLocation() + "\n\n"
					+ "Track their live location here: " + trackLink + "\n\n"
					+ "Please check on them immediately or contact authorities.\n\n"
					+ "– Go-Easy Safety Team";

			for (com.ride.goeasy.entity.TrustedContact c : contacts) {
				try {
					mailService.sendMail(c.getEmail(), subject, body);
				} catch (Exception ignored) {}
			}
		}

		ResponseStructure<String> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("SOS triggered. Trusted contacts notified.");
		rs.setData("SOS_ACTIVE");
		return rs;
	}
}

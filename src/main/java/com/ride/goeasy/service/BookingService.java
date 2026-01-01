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

		LocationResponse[] srcResponse = restTemplate.getForObject(srcUrl, LocationResponse[].class);

		// If response is null or empty array, city is invalid
		if (srcResponse == null || srcResponse.length == 0) {
			throw new InvalidLocationException("Invalid source city: " + sourceCity);
		}

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

		if (!validCity) {
			throw new InvalidLocationException("Invalid city: " + sourceCity);
		}

		// Safe to use coordinates now
		double latitude = Double.parseDouble(srcResponse[0].getLat());
		double longitude = Double.parseDouble(srcResponse[0].getLon());

		// STEP 5: Create Booking

		if (cust.isActiveBookingFlag() == true) {
			ResponseStructure<Booking> rs = new ResponseStructure<>();
			rs.setStatusCode(HttpStatus.CREATED.value());
			rs.setMessage("Your current ride has not comleted");
			rs.setData(null);
		
			return rs;
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
		b.setBookingStatus(BookingStatus.BOOKED);
		b.setPayment(p);
		b.setActiveBookingFlag(true);
		cust.getBookings().add(b);
		Driver d = vehicle.getDriver();

		d.getDblist().add(b);
		
		
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

	public ResponseStructure<RideDetailsDTO> activeBookingHistory(List<Booking> blist) {

		RideDetailsDTO rdto = new RideDetailsDTO();
		for (Booking b : blist) {
			if (b.isActiveBookingFlag() == true) {

				rdto.setBookingId(b.getId());
				rdto.setSourceLocation(b.getSourceLocation());
				rdto.setDestinationLocation(b.getDestinationLocation());
				rdto.setDistance(b.getDistance());
				rdto.setBookingStatus(b.getBookingStatus().name());

				rdto.setFare(b.getFare());

				ResponseStructure<RideDetailsDTO> rs = new ResponseStructure<>();
				rs.setStatusCode(HttpStatus.OK.value());
				rs.setMessage("Ongoing- not completed yet");
				rs.setData(rdto);

				return rs;

			}
		}
		ResponseStructure<RideDetailsDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.OK.value());
		rs.setMessage("No active booking");
		rs.setData(rdto);

		return rs;

	}

	// Booking Cancel by Driver

	public ResponseStructure<String> cancelBookingByDriver(int bookingId) {

		Booking booking = bookingRepo.findById(bookingId).orElseThrow(() -> new RuntimeException("Booking not found"));

		Driver driver = booking.getVehicle().getDriver();
		Customer customer = booking.getCustomer();

		// Count how many times driver cancelled
		List<Booking> cancelledList = bookingRepo.findByVehicleDriverAndBookingStatus(driver,
				BookingStatus.CANCELLED_BY_DRIVER);

		int cancelCount = cancelledList.size();

		// Cancel booking
		booking.setBookingStatus(BookingStatus.CANCELLED_BY_DRIVER);
		booking.setActiveBookingFlag(false);

		// Reset customer active booking
		customer.setActiveBookingFlag(false);

		// Block driver if more than 4 cancels
		if (cancelCount >= 4) {
			driver.setDstatus("BLOCKED");
		}

		bookingRepo.save(booking);
		dr.save(driver);
		customerRepo.save(customer);
		
		
		// cancellation send mail
		try {
		    mailService.sendRideCancellationMail(
		        customer.getEmail(),
		        String.valueOf(booking.getId())
		    );
		} catch (Exception e) {
		    System.out.println("Cancellation mail failed");
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

	    if (booking.getStartOtp() == null || !booking.getStartOtp().equals(otp)) {
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

	    mailService.sendMail(
	        cust.getEmail(),
	        "Ride Completion OTP - GoEasy",
	        "Hello " + cust.getName() + ",\n\n"
	      + "Your Ride Completion OTP is: " + endOtp + "\n\n"
	      + "Please share this OTP with the driver to complete the ride.\n\n"
	      + "– GoEasy Team"
	    );

	    ResponseStructure<String> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Ride completion OTP sent to customer");
	    rs.setData(null);

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
	    if (booking.getEndOtp() == null || !booking.getEndOtp().equals(otp)) {
	        throw new RuntimeException("Invalid Completion OTP");
	    }

	    booking.setEndOtpVerified(true);
	    booking.setBookingStatus(BookingStatus.COMPLETED);
	    booking.setActiveBookingFlag(false);

	    // reset customer active booking
	    Customer cust = booking.getCustomer();
	    cust.setActiveBookingFlag(false);

	    bookingRepo.save(booking);
	    customerRepo.save(cust);

	    ResponseStructure<String> rs = new ResponseStructure<>();
	    rs.setStatusCode(HttpStatus.OK.value());
	    rs.setMessage("Ride completed successfully");
	    rs.setData(null);

	    return rs;
	}



}

package com.ride.goeasy.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.BookingHistoryDTO;
import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.dto.LocationResponse;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.exception.InvalidLocationException;
import com.ride.goeasy.exception.VehicleNotFoundException;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;

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
	private RestTemplate restTemplate;

	@Value("${locationiq.api.key}")
	private String apiKey;

	public ResponseStructure<Booking> bookVehicle(long mobno, BookingRequestDTO bookingRequestDTO) {

		// STEP 1: Prevent booking if same city
		if (bookingRequestDTO.getSourceLocation().trim()
				.equalsIgnoreCase(bookingRequestDTO.getDestinationLocation().trim())) {
			throw new InvalidLocationException("Source and destination cannot be the same.");
		}

		// STEP 2: Fetch Customer
		Customer cust = customerRepo.findByMobno(mobno).orElseThrow(() -> new CustomerNotFoundException("Customer Not Found"));

		// STEP 3: Fetch Vehicle
		Vehicle vehicle = vr.findByVehicleNumber(bookingRequestDTO.getVehicleNumber())
				.orElseThrow(() -> new VehicleNotFoundException("Vehicle Not Found"));
		
		

		// ⭐⭐ STEP 4: Convert Source City → Coordinates ⭐⭐
		String sourceCity = bookingRequestDTO.getSourceLocation().trim();
		String srcUrl = "https://us1.locationiq.com/v1/search?key=" + apiKey +
		                "&q=" + sourceCity + "&format=json";

		LocationResponse[] srcResponse = restTemplate.getForObject(srcUrl, LocationResponse[].class);

		// If response is null or empty array, city is invalid
		if (srcResponse == null || srcResponse.length == 0) {
		    throw new InvalidLocationException("Invalid source city: " + sourceCity);
		}

		// Accept only if type = "city"
		boolean validCity = false;
		for (LocationResponse loc : srcResponse) {
		    String type = loc.getType();
		    if ("city".equalsIgnoreCase(type) || 
		        "town".equalsIgnoreCase(type) || 
		        "administrative".equalsIgnoreCase(type)) {
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
		 
		 Optional<Customer> c=customerRepo.findByMobno(mobno);
		 Customer cust1= c.get();
		 if(cust1.isActiveBookingFlag()==true) {
			 ResponseStructure<Booking> rs = new ResponseStructure<>();
				rs.setStatusCode(HttpStatus.CREATED.value());
				rs.setMessage("Your current ride has not comleted");
				rs.setData(null);

				return rs;
		 }
		 
		Booking b = new Booking();
		b.setCustomer(cust);
		b.setVehicle(vehicle);
		b.setSourceLocation(sourceCity);
		b.setDestinationLocation(bookingRequestDTO.getDestinationLocation());
		b.setFare(bookingRequestDTO.getFare());
		b.setDistance(bookingRequestDTO.getDistance());
		b.setEstimatedTime(bookingRequestDTO.getEstimatedTime());
		b.setBookingStatus("Booked");
		b.setPayment(null);
		b.setActiveBookingFlag(true);
		cust.setActiveBookingFlag(true);

		// STEP 6: Save
		Booking savedBooking = bookingRepo.save(b);

		// STEP 7: Response
		ResponseStructure<Booking> rs = new ResponseStructure<>();
		rs.setStatusCode(HttpStatus.CREATED.value());
		rs.setMessage(" Vehicle booked successfully");
		rs.setData(savedBooking);

		return rs;

	}

//	Customer Booking History
	public ResponseStructure<List<BookingHistoryDTO>> getCustomerBookingHistory(long mobno) {

		List<Booking> list = bookingRepo.findByCustomerMobno(mobno);
		 return bookingHistory(list);
	}

//	Active Booking 
	public ResponseStructure<BookingHistoryDTO> getCustomerActiveBooking(long mobno) {

		List<Booking> blist = (List<Booking>) bookingRepo.findByCustomerMobnoAndBookingStatus(mobno, "PENDING");
		for (Booking b : blist) {
			if (b.isActiveBookingFlag() == true) {
				BookingHistoryDTO bhdto = new BookingHistoryDTO();
				bhdto.setBookingId(b.getId());
				bhdto.setCustomerName(b.getCustomer().getName());
				bhdto.setBookingStatus(b.getBookingStatus());
				bhdto.setDestinationLocation(b.getDestinationLocation());
				bhdto.setSourceLocation(b.getSourceLocation());
				bhdto.setDistance(b.getDistance());
				bhdto.setEstimatedTime(b.getEstimatedTime());
				bhdto.setFare(b.getFare());
				bhdto.setPaymentStatus("Done");
				bhdto.setVehicleType(b.getVehicle().getVehicleType());

				ResponseStructure<BookingHistoryDTO> rs = new ResponseStructure<>();
				rs.setStatusCode(200);
				rs.setMessage("Customer Active Booking");
				rs.setData(bhdto);

				return rs;
			}
		}

		BookingHistoryDTO bhdto = new BookingHistoryDTO();
		ResponseStructure<BookingHistoryDTO> rs = new ResponseStructure<>();
		rs.setStatusCode(200);
		rs.setMessage("Customer Active Booking");
		rs.setData(null);
		return rs;
	}

//	Driver booking history
	public ResponseStructure<List<BookingHistoryDTO>> getDriverBookingHistory(long mobNo) {

		List<Booking> list = bookingRepo.findByVehicleDriverMobNo(mobNo);

		 return bookingHistory(list);
	}
	public ResponseStructure<List<BookingHistoryDTO>> bookingHistory(List<Booking> list){ 

		List<BookingHistoryDTO> l = new ArrayList<BookingHistoryDTO>();
		for (Booking b : list) {
			BookingHistoryDTO bhdto = new BookingHistoryDTO();
			bhdto.setBookingId(b.getId());
			bhdto.setCustomerName(b.getCustomer().getName());
			bhdto.setBookingStatus(b.getBookingStatus());
			bhdto.setDestinationLocation(b.getDestinationLocation());
			bhdto.setSourceLocation(b.getSourceLocation());
			bhdto.setDistance(b.getDistance());
			bhdto.setEstimatedTime(b.getEstimatedTime());
			bhdto.setFare(b.getFare());
			bhdto.setPaymentStatus("Payment not yet completed");
			bhdto.setVehicleType(b.getVehicle().getVehicleType());

			l.add(bhdto);
		}

		ResponseStructure<List<BookingHistoryDTO>> rs = new ResponseStructure<>();
		rs.setMessage("Driver Booking History");
		rs.setStatusCode(200);
		rs.setData(l);

		return rs;
	}

}

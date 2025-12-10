package com.ride.goeasy.service;






import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.BookingRequestDTO;
import com.ride.goeasy.dto.ReverseGeoResponse;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Vehicle;
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
            throw new IllegalArgumentException("Source and destination cannot be the same.");
        }

        // STEP 2: Fetch Customer
        Customer cust = customerRepo.findByMobno(mobno)
                .orElseThrow(() -> new RuntimeException("Customer Not Found"));

        // STEP 3: Fetch Vehicle
        Vehicle vehicle = vr.findByVehicleNumber(bookingRequestDTO.getVehicleNumber())
                .orElseThrow(() -> new RuntimeException("Vehicle Not Found"));

        // ⭐⭐ STEP 4: Convert Source Lat/Lon → City Name ⭐⭐
        String[] srcArr = bookingRequestDTO.getSourceLocation().split(",");
        double srcLat = Double.parseDouble(srcArr[0]);
        double srcLon = Double.parseDouble(srcArr[1]);

        String reverseUrl = "https://us1.locationiq.com/v1/reverse?key=" + apiKey
                + "&lat=" + srcLat
                + "&lon=" + srcLon
                + "&format=json";

        ReverseGeoResponse reverseResponse = restTemplate.getForObject(reverseUrl, ReverseGeoResponse.class);
        String sourceCity = reverseResponse.getAddress().getCity();

        // STEP 5: Create Booking
        Booking b = new Booking();
        b.setCustomer(cust);
        b.setVehicle(vehicle);
        b.setSourceLocation(sourceCity); // ⭐ NOW CITY NAME ⭐
        b.setDestinationLocation(bookingRequestDTO.getDestinationLocation());
        b.setFare(bookingRequestDTO.getFare());
        b.setDistance(bookingRequestDTO.getDistance());
        b.setEstimatedTime(bookingRequestDTO.getEstimatedTime());
        b.setBookingStatus("PENDING");
        b.setPayment(null);

        // STEP 6: Save
        Booking savedBooking = bookingRepo.save(b);

        // STEP 7: Response
        ResponseStructure<Booking> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.CREATED.value());
        rs.setMessage("Vehicle booked successfully");
        rs.setData(savedBooking);

        return rs;
    }
}


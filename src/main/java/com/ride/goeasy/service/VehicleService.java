package com.ride.goeasy.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.AvailableVehicleDTO;
import com.ride.goeasy.dto.LocationResponse;
import com.ride.goeasy.dto.MatrixResponse;
import com.ride.goeasy.dto.ReverseGeoResponse;
import com.ride.goeasy.dto.VehicleDetailDTO;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.exception.InvalidLocationException;
import com.ride.goeasy.exception.VehicleNotFoundException;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class VehicleService {

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private VehicleRepo vehicleRepo;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${locationiq.api.key}")
	private String apiKey;

	private final String LOCATION_API = "https://us1.locationiq.com/v1/search";

	private final String MATRIX_API = "https://us1.locationiq.com/v1/matrix/driving/";

	public AvailableVehicleDTO getAvailableVehicles(Long mobile, String destinationLocation) {

		// STEP 1: Validate & Fetch Coordinates of Destination
		// STEP 1: Validate & Fetch Coordinates of Destination
		double destLat = 12.9716; // Default Bangalore
		double destLon = 77.5946;
		
		try {
			String url = LOCATION_API + "?key=" + apiKey + "&q=" + destinationLocation + "&format=json";
			LocationResponse[] locationRes = restTemplate.getForObject(url, LocationResponse[].class);

			if (locationRes != null && locationRes.length > 0) {
				destLat = Double.parseDouble(locationRes[0].getLat());
				destLon = Double.parseDouble(locationRes[0].getLon());
			}
		} catch (Exception e) {
			System.err.println("Location API Timeout (VehicleService): " + e.getMessage() + ". Using default.");
			// Fallback to default
		}

		// STEP 2: Verify Customer
		Customer customer = customerRepo.findByMobno(mobile)
				.orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobile));

		String[] src = customer.getCurrentLocation().split(",");
		double srcLat = Double.parseDouble(src[0]);
		double srcLon = Double.parseDouble(src[1]);

		// STEP 3: Get Distance & Time (Matrix API)
		// STEP 3: Get Distance & Time (Matrix API)
		double distance = 5.0; // Default fallback distance (5 km)
		
		try {
			String finalURL = MATRIX_API + srcLon + "," + srcLat + ";" + destLon + "," + destLat + "?key=" + apiKey
					+ "&annotations=distance,duration";
	
			MatrixResponse matrixResponse = restTemplate.getForObject(finalURL, MatrixResponse.class);
			
			if (matrixResponse != null && matrixResponse.getDistances() != null) {
				distance = matrixResponse.getDistances().get(0).get(1) / 1000.0; // meters → km
			}
		} catch (Exception e) {
			System.err.println("Matrix API Timeout (VehicleService): " + e.getMessage() + ". Using default distance.");
			// Keep default distance = 5.0
		}

		String city = "Bangalore"; // Default Fallback
		try {
			String reverseUrl = "https://us1.locationiq.com/v1/reverse?key=" + apiKey + "&lat=" + srcLat + "&lon="
					+ srcLon + "&format=json";

			ReverseGeoResponse reverseResponse = restTemplate.getForObject(reverseUrl, ReverseGeoResponse.class);

			if (reverseResponse != null && reverseResponse.getAddress() != null) {
				String apiCity = reverseResponse.getAddress().getCity();
				if (apiCity != null) {
					city = apiCity;
				}
			}
		} catch (Exception e) {
			System.err.println("Reverse Geocoding Failed (Rate Limit or Error): " + e.getMessage());
			// Keep default "Bangalore"
		}

		

		// Fetch Available Vehicles in the city
		List<Vehicle> availableVehicles = vehicleRepo.findAvailableVehiclesInCity(city);
		System.out.println("DEBUG: Searching for vehicles in city: " + city);
		System.out.println("DEBUG: Found " + availableVehicles.size() + " available vehicles");
		System.out.println("DEBUG: Distance calculated: " + distance + " km");
		
		Double penaltyAmount = customer.getPenaltyAmount();
		if (penaltyAmount == null) penaltyAmount = 0.0;

		List<VehicleDetailDTO> vehicleDetails = new ArrayList<>();
		
		// STEP 5: Fare and Time Calculation
		for (Vehicle v : availableVehicles) {
			VehicleDetailDTO dto = new VehicleDetailDTO();
			dto.setModel(v.getVehicleModel());
			dto.setVehicleNumber(v.getVehicleNumber());
			dto.setPricePerKm(v.getPricePerKm());
			dto.setDistance(distance); // Set the distance for each vehicle
			
			// Fix: Handle Null Unboxing
			Double dbSpeed = v.getAvgspeed();
			dto.setAverageSpeed(dbSpeed != null ? dbSpeed : 45.0);

			double fare = v.getPricePerKm() * distance;
			double totalAmount = fare + penaltyAmount;
			
			// Safety check for avgSpeed
            Double avgSpeed = v.getAvgspeed();
            if (avgSpeed == null || avgSpeed <= 0) {
                avgSpeed = 45.0; // Default fallback
            }
            double timeInHours = distance / avgSpeed;
            double timeInMinutes = timeInHours * 60; // Convert to minutes

			dto.setEstimatedFare(fare);
			dto.setPenalty(penaltyAmount);
			dto.setTotalAmout(totalAmount);
			
			dto.setEstimatedTime(timeInMinutes); // Time in minutes

			vehicleDetails.add(dto);
		}
		
	
		
		
		
		
		

		// STEP 6: Prepare Final Response
		AvailableVehicleDTO response = new AvailableVehicleDTO();
		response.setCustomer(customer);
		response.setDistance(distance);
		response.setSource(customer.getCurrentLocation());
		response.setDestination(destinationLocation);
		response.setVehicles(vehicleDetails);

		return response;
	}
	
	// finding vehicle by vehicle number
	public ResponseStructure<Vehicle> findByVno(String vehicleNumber){

	    Vehicle vehicle = vehicleRepo.findByVehicleNumber(vehicleNumber)
	            .orElseThrow(() -> new VehicleNotFoundException("Vehicle Not Found with vehicleNumber: " + vehicleNumber));

	    ResponseStructure<Vehicle> rs = new ResponseStructure<>();
	    rs.setData(vehicle);
	    rs.setMessage("Vehicle found successfully");
	    rs.setStatusCode(HttpStatus.OK.value());

	    return rs;
	}

}

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
		String url = LOCATION_API + "?key=" + apiKey + "&q=" + destinationLocation + "&format=json";

		LocationResponse[] locationRes = restTemplate.getForObject(url, LocationResponse[].class);

		if (locationRes == null || locationRes.length == 0) {
			throw new InvalidLocationException("Invalid Destination Location");
		}

		double destLat = Double.parseDouble(locationRes[0].getLat());
		double destLon = Double.parseDouble(locationRes[0].getLon());

		// STEP 2: Verify Customer
		Customer customer = customerRepo.findByMobno(mobile)
				.orElseThrow(() -> new CustomerNotFoundException("Customer Not Found with Mobile: " + mobile));

		String[] src = customer.getCurrentLocation().split(",");
		double srcLat = Double.parseDouble(src[0]);
		double srcLon = Double.parseDouble(src[1]);

		// STEP 3: Get Distance & Time (Matrix API)
		String finalURL = MATRIX_API + srcLon + "," + srcLat + ";" + destLon + "," + destLat + "?key=" + apiKey
				+ "&annotations=distance,duration";

		MatrixResponse matrixResponse = restTemplate.getForObject(finalURL, MatrixResponse.class);

		double distance = matrixResponse.getDistances().get(0).get(1) / 1000.0; // meters → km

		String reverseUrl = "https://us1.locationiq.com/v1/reverse?key=" + apiKey + "&lat=" + srcLat + "&lon=" + srcLon
				+ "&format=json";

		ReverseGeoResponse reverseResponse = restTemplate.getForObject(reverseUrl, ReverseGeoResponse.class);

		String city = reverseResponse.getAddress().getCity();

		

		// Fetch Available Vehicles in the city
		List<Vehicle> availableVehicles = vehicleRepo.findAvailableVehiclesInCity(city);
       int cancellationCount= customer.getCancellationCount();
		List<VehicleDetailDTO> vehicleDetails = new ArrayList<>();
		System.out.println(availableVehicles);
		// STEP 5: Fare and Time Calculation
		for (Vehicle v : availableVehicles) {
			VehicleDetailDTO dto = new VehicleDetailDTO();
			dto.setModel(v.getVehicleModel());
			dto.setVehicleNumber(v.getVehicleNumber());
			dto.setPricePerKm(v.getPricePerKm());
			dto.setAverageSpeed(v.getAvgspeed());

			double fare = v.getPricePerKm() * distance;
			double penalty= (fare/10)*cancellationCount;
			double totalAmount= fare+penalty;
			double time = distance / v.getAvgspeed();

			dto.setEstimatedFare(fare);
			dto.setPenalty(penalty);
			dto.setTotalAmout(totalAmount);
			
			dto.setEstimatedTime(time);

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

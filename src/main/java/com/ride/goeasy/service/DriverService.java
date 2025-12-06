package com.ride.goeasy.service;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ride.goeasy.dto.LocationDetails;
import com.ride.goeasy.entity.Driver;

import com.ride.goeasy.repository.DriverRepo;

@Service
public class DriverService {

	@Autowired
	DriverRepo driverRepo;

	public Driver saveDriverWithVehicle(Driver driver) {

		return driverRepo.save(driver);
	}

	public Optional<Driver> find(int id) {
		return driverRepo.findById(id);

	}

//	this is for coordinate
	@Autowired
	private RestTemplate restTemplate;

	public LocationDetails getCityFromCoords(double lat, double lon) {
		String url = String.format("https://nominatim.openstreetmap.org/reverse?lat=%s&lon=%s&format=json", lat, lon);

		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", "rideapp"); // polite to set
		headers.set("Accept-Language", "en");
		HttpEntity<String> entity = new HttpEntity<>(headers);

		ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
		Map body = response.getBody();
		if (body == null || body.get("address") == null) {
			return new LocationDetails("Not Found", "Unknown", "Unknown");
		}

		Map address = (Map) body.get("address");

		String city = (String) (address.get("city") != null ? address.get("city")
				: address.get("town") != null ? address.get("town")
						: address.get("village") != null ? address.get("village")
								: address.get("suburb") != null ? address.get("suburb") : "Not Found");

		String state = (String) (address.get("state") != null ? address.get("state") : "Unknown");
		String pincode = (String) (address.get("postcode") != null ? address.get("postcode") : "Unknown");

		return new LocationDetails(city, state, pincode);
	}

//	this is for update vehicle city
	public String updateVehicleCity(double lat, double lon, int driverId) {

		// Step 1: Get city from coordinates
		LocationDetails details = getCityFromCoords(lat, lon);

		// Step 2: Find driver
		Optional<Driver> op = driverRepo.findById(driverId);

		if (op.isEmpty()) {
			return "Driver not found!";
		}

		Driver driver = op.get();

		// Step 3: Get vehicle from driver
		if (driver.getVehicle() == null) {
			return "Vehicle not found for this driver!";
		}

		// Step 4: Set only city (because Vehicle has only `city` field)
		driver.getVehicle().setCity(details.getCity());

		// Step 5: Save (vehicle auto-save due to cascade)
		driverRepo.save(driver);

		return "City updated successfully: " + details.getCity();
	}

}

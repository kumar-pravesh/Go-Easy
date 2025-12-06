package com.ride.goeasy.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.LocationDetails;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.service.DriverService;

@RestController
@RequestMapping("/driver")
public class DriverController {

	@Autowired
	DriverService driverService;

	// to perform save operation

	@PostMapping("/save")
	public Driver saveDriverVehicle(@RequestBody Driver driver) {
		return driverService.saveDriverWithVehicle(driver);
	}

	// find operation
	@GetMapping("/find")
	public Optional<Driver> find(@RequestParam int id) {
		Optional<Driver> d = driverService.find(id);
		return d;
	}

	// Get only city/state/pincode from coordinates
	@GetMapping("/location")
	public LocationDetails getLocation(@RequestParam double lat, @RequestParam double lon) {

		return driverService.getCityFromCoords(lat, lon);
	}

	// Update vehicle.city using lat/lon + driverId
	@PatchMapping("/updateVehicleCity")
	public String updateVehicleCity(@RequestParam double lat, @RequestParam double lon, @RequestParam int driverId) {

		return driverService.updateVehicleCity(lat, lon, driverId);
	}

}

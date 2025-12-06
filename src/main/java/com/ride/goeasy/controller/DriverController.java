package com.ride.goeasy.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


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
	@DeleteMapping("/delete")
	public String delete(@RequestParam int id) {

		boolean deleted = driverService.deleteDriverById(id);

		if (!deleted) {
			return " Driver not found with ID: " + id;
		}

		return " Driver deleted successfully with ID: " + id;
	}

	

}

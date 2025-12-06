package com.ride.goeasy.service;


import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	 public boolean deleteDriverById(int id) {
	        if (driverRepo.existsById(id)) {
	            driverRepo.deleteById(id);
	            return true;
	        }
	        return false;
	    }



}

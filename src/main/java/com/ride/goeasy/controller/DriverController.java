package com.ride.goeasy.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.service.DriverService;

@RestController
public class DriverController {

    @Autowired
      DriverService driverService;
    
    //to perform save operation

    @PostMapping("/save")
    public Driver saveDriverVehicle(@RequestBody Driver driver) {
        return driverService.saveDriverWithVehicle(driver);
    }
    //find operation
    @GetMapping("/find")
    public Optional<Driver> find(@RequestParam int id){
		Optional<Driver> d= driverService.find( id);
    	return d;
    }
    
    
    
}

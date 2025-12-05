package com.ride.goeasy.controller;

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

    @PostMapping("/save")
    public Driver saveDriverVehicle(@RequestBody Driver driver) {
        return driverService.saveDriverWithVehicle(driver);
    }
    @GetMapping
    public Driver find(@RequestParam int id){
		return null;
    	
    }
    
    
}

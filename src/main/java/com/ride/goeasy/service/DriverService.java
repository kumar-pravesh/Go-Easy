package com.ride.goeasy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.repository.DriverRepo;

@Service
public class DriverService {

    @Autowired
     DriverRepo driverRepo;
 
    public Driver saveDriverWithVehicle(Driver driver) {
 
        
        return driverRepo.save(driver);
    }
}

package com.ride.goeasy.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.VehicleRepo;

@Service
public class DriverResetTask {

    @Autowired
    private DriverRepo driverRepo;

    @Autowired
    private VehicleRepo vehicleRepo;

    /**
     * Resets driver cancellation counts and blocks at midnight every day.
     * Cron expression: "0 0 0 * * *" (Second Minute Hour Day Month DayOfWeek)
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyCancellations() {
        System.out.println("Executing Midnight Driver Reset Task...");
        
        List<Driver> drivers = driverRepo.findAll();
        LocalDate yesterday = LocalDate.now().minusDays(1);

        for (Driver driver : drivers) {
            boolean updated = false;

            // Reset cancel count if it hasn't been reset today
            if (driver.getDailyCancelCount() != null && driver.getDailyCancelCount() > 0) {
                driver.setDailyCancelCount(0);
                updated = true;
            }

            // Unblock if they were temporarily blocked
            if ("TEMPORARY_BLOCKED".equalsIgnoreCase(driver.getDstatus())) {
                driver.setDstatus("ONLINE");
                updated = true;
                
                Vehicle vehicle = driver.getVehicle();
                if (vehicle != null && "TEMPORARY_BLOCKED".equalsIgnoreCase(vehicle.getAvlStatus())) {
                    vehicle.setAvlStatus("AVAILABLE");
                    vehicleRepo.save(vehicle);
                }
            }
            
            // Also reset OFFLINE drivers to ONLINE as per user request (visible for customers after 12)
            if ("OFFLINE".equalsIgnoreCase(driver.getDstatus())) {
                driver.setDstatus("ONLINE");
                updated = true;
                
                Vehicle vehicle = driver.getVehicle();
                if (vehicle != null && "OFFLINE".equalsIgnoreCase(vehicle.getAvlStatus())) {
                    vehicle.setAvlStatus("AVAILABLE");
                    vehicleRepo.save(vehicle);
                }
            }

            if (updated) {
                driverRepo.save(driver);
            }
        }
        
        System.out.println("Midnight Driver Reset Task completed.");
    }
}

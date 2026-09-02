package com.ride.goeasy.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.AdminStatsDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.SOSEvent;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.SOSEventRepository;
import com.ride.goeasy.repository.UserrRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class AdminService {

    @Autowired
    DriverRepo driverRepo;
    
    @Autowired
    CustomerRepo customerRepo;
    
    @Autowired
    BookingRepo bookingRepo;
    
    @Autowired
    SOSEventRepository sosEventRepo;
    
    @Autowired
    UserrRepo userrRepo;

    public ResponseStructure<AdminStatsDTO> getPlatformStats() {
        long totalDrivers = driverRepo.count();
        long totalCustomers = customerRepo.count();
        long totalBookings = bookingRepo.count();
        long sosAlerts = sosEventRepo.count();
        
        List<Booking> allBookings = bookingRepo.findAll();
        double totalRevenue = 0.0;
        long activeRides = 0;
        
        for (Booking b : allBookings) {
            if ("STARTED".equals(b.getBookingStatus().toString())) {
                activeRides++;
            }
            if ("COMPLETED".equals(b.getBookingStatus().toString()) && b.getFare() != null) {
                totalRevenue += b.getFare();
            }
        }
        
        AdminStatsDTO stats = new AdminStatsDTO(totalDrivers, totalCustomers, totalBookings, totalRevenue, activeRides, sosAlerts);
        
        ResponseStructure<AdminStatsDTO> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Platform stats retrieved successfully");
        rs.setData(stats);
        
        return rs;
    }

    public ResponseStructure<List<Driver>> getAllDrivers() {
        List<Driver> drivers = driverRepo.findAll();
        ResponseStructure<List<Driver>> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Drivers retrieved successfully");
        rs.setData(drivers);
        return rs;
    }

    public ResponseStructure<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepo.findAll(); // In a real app, you'd paginate this
        ResponseStructure<List<Booking>> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Bookings retrieved successfully");
        rs.setData(bookings);
        return rs;
    }

    public ResponseStructure<List<SOSEvent>> getAllSOSEvents() {
        List<SOSEvent> events = sosEventRepo.findAll();
        ResponseStructure<List<SOSEvent>> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("SOS events retrieved successfully");
        rs.setData(events);
        return rs;
    }

    public ResponseStructure<Driver> blockDriver(Integer driverId, boolean block) {
        Driver driver = driverRepo.findById(driverId)
                .orElseThrow(() -> new RuntimeException("Driver not found"));
        
        if (driver.getUserr() != null) {
            driver.getUserr().setRole(block ? "BLOCKED" : "DRIVER");
            userrRepo.save(driver.getUserr());
        }
        
        driverRepo.save(driver);
        
        ResponseStructure<Driver> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Driver " + (block ? "blocked" : "unblocked") + " successfully");
        rs.setData(driver);
        return rs;
    }
}

package com.ride.goeasy.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.VehicleRepo;

@Component
public class RideSchedulerTask {

    @Autowired private BookingRepo bookingRepo;
    @Autowired private VehicleRepo vehicleRepo;
    @Autowired private MailService mailService;

    /**
     * Runs every 5 minutes.
     * Finds scheduled rides whose pickup is within the next 30 min,
     * locks the vehicle (ON_RIDE) and sends reminder emails.
     */
    @Scheduled(fixedRate = 300_000)
    public void notifyUpcomingScheduledRides() {
        LocalDateTime now    = LocalDateTime.now();
        LocalDateTime cutoff = now.plusMinutes(30);

        List<Booking> due = bookingRepo.findScheduledRidesDue(now, cutoff);

        for (Booking b : due) {
            try {
                Vehicle v = b.getVehicle();
                if ("AVAILABLE".equalsIgnoreCase(v.getAvlStatus())) {
                    v.setAvlStatus("ON_RIDE");
                    vehicleRepo.save(v);
                }

                // Notify customer
                mailService.sendMail(
                    b.getCustomer().getEmail(),
                    "Your Scheduled GoEasy Ride is in 30 Minutes",
                    "Hello " + b.getCustomer().getName() + ",\n\n"
                  + "Your scheduled ride from " + b.getSourceLocation()
                  + " to " + b.getDestinationLocation() + " starts at "
                  + b.getScheduledTime() + ".\n\n"
                  + "Your fare of ₹" + b.getFare() + " is locked — no surge pricing.\n"
                  + "Start OTP: " + b.getStartOtp() + "\n\n"
                  + "– GoEasy Team"
                );

                // Notify driver
                if (v.getDriver() != null && v.getDriver().getMailId() != null) {
                    mailService.sendMail(
                        v.getDriver().getMailId(),
                        "Scheduled Ride Alert — Pickup in 30 Minutes",
                        "Hello " + v.getDriver().getDname() + ",\n\n"
                      + "You have a scheduled pickup in 30 minutes.\n"
                      + "From: " + b.getSourceLocation() + "\n"
                      + "To: " + b.getDestinationLocation() + "\n"
                      + "Customer: " + b.getCustomer().getName() + "\n"
                      + "Fare: ₹" + b.getFare() + " (price-locked)\n\n"
                      + "– GoEasy Team"
                    );
                }

                b.setScheduledNotifSent(true);
                bookingRepo.save(b);

            } catch (Exception e) {
                System.err.println("Scheduler error for booking " + b.getId() + ": " + e.getMessage());
            }
        }
    }
}

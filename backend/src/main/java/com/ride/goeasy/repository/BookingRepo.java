package com.ride.goeasy.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.enums.BookingStatus;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Integer> {

    List<Booking> findByCustomerMobno(Long mobno);
    List<Booking> findByCustomerMobnoAndBookingStatus(long mobno, String status);
    List<Booking> findByVehicleDriverMobNo(Long mobNo);
    Booking findByVehicleDriverMobNoAndBookingStatus(Long mobNo, String bookingStatus);
    List<Booking> findByVehicleDriverAndBookingStatus(Driver driver, BookingStatus bookingStatus);

    // Scheduled rides whose pickup is within the next 30 minutes and haven't been notified yet
    @Query("SELECT b FROM Booking b WHERE b.scheduled = true AND b.scheduledNotifSent = false " +
           "AND b.scheduledTime BETWEEN :now AND :cutoff AND b.bookingStatus = 'BOOKED'")
    List<Booking> findScheduledRidesDue(@Param("now") LocalDateTime now, @Param("cutoff") LocalDateTime cutoff);

    // Upcoming scheduled rides for a customer
    List<Booking> findByCustomerMobnoAndScheduledTrueAndBookingStatus(Long mobno, BookingStatus status);
}

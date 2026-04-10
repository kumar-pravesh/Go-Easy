package com.ride.goeasy.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.enums.BookingStatus;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Integer>{
	
	

	    //  CUSTOMER BOOKING HISTORY
	    List<Booking> findByCustomerMobno(Long mobno);

	    //  CUSTOMER ACTIVE BOOKING
	    List<Booking> findByCustomerMobnoAndBookingStatus(long mobno, String status);
	    //  DRIVER BOOKING HISTORY
	    List<Booking> findByVehicleDriverMobNo(Long mobNo);

	    //  DRIVER ACTIVE BOOKING
	    Booking findByVehicleDriverMobNoAndBookingStatus(Long mobNo, String bookingStatus);
	    
	    
	    
	    
	    
	
	    List<Booking> findByVehicleDriverAndBookingStatus(
	            Driver driver,
	            BookingStatus bookingStatus
	    );


}

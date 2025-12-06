package com.ride.goeasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Booking;

@Repository
public interface BookingRepo extends JpaRepository<Booking, Integer>{

}

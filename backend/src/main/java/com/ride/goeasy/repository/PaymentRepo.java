package com.ride.goeasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ride.goeasy.entity.Payment;

public interface PaymentRepo extends JpaRepository<Payment, Integer>{
	

}

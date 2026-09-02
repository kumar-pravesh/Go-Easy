package com.ride.goeasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.time.LocalDateTime;

import com.ride.goeasy.entity.Payment;

public interface PaymentRepo extends JpaRepository<Payment, Integer>{
	
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'PENDING' AND " +
           "((p.driverConfirmedCash = true AND p.customerConfirmedCash = false) OR " +
           "(p.driverConfirmedCash = false AND p.customerConfirmedCash = true)) AND " +
           "p.lastConfirmedAt IS NOT NULL AND p.lastConfirmedAt < :cutoff")
    List<Payment> findPendingCashConfirmationsBefore(@Param("cutoff") LocalDateTime cutoff);
}

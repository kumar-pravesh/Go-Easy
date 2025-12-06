package com.ride.goeasy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Integer> {
	 // Custom method to find a customer by mobile number
    Optional<Customer> findByMobno(Long mobno);
}

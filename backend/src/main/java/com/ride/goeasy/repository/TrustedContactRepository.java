package com.ride.goeasy.repository;

import com.ride.goeasy.entity.TrustedContact;
import com.ride.goeasy.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrustedContactRepository extends JpaRepository<TrustedContact, Integer> {
    List<TrustedContact> findByCustomer(Customer customer);
}

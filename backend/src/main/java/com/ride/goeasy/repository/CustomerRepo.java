package com.ride.goeasy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Integer> {
    // findFirst guards against duplicate rows from past registrations
    Optional<Customer> findFirstByMobno(Long mobno);
    Optional<Customer> findFirstByEmail(String email);

    boolean existsByEmail(String email);
    boolean existsByMobno(Long mobno);

    // keep old names as delegates so callers compile without changes
    default Optional<Customer> findByMobno(Long mobno) { return findFirstByMobno(mobno); }
    default Optional<Customer> findByEmail(String email) { return findFirstByEmail(email); }
}

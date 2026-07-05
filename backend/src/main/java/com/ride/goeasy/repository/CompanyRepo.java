package com.ride.goeasy.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ride.goeasy.entity.Company;

public interface CompanyRepo extends JpaRepository<Company, Integer> {
    Optional<Company> findByCompanyEmail(String email);
    boolean existsByCompanyEmail(String email);
}

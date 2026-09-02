package com.ride.goeasy.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.ride.goeasy.entity.PromoCode;

@Repository
public interface PromoCodeRepo extends JpaRepository<PromoCode, Integer> {
    Optional<PromoCode> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
}

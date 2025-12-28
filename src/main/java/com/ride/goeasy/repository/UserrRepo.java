package com.ride.goeasy.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Userr;
@Repository
public interface UserrRepo extends JpaRepository<Userr, Integer> {
	boolean existsByMobno(long mobno);
	 Optional<Userr> findByMobNo(long mobNo);
}

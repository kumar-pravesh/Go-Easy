package com.ride.goeasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Userr;
@Repository
public interface UserrRepo extends JpaRepository<Userr, Integer> {

}

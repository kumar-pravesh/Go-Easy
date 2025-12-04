package com.ride.goeasy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Driver;

@Repository
public interface DriverRepo extends JpaRepository<Driver, Integer>{

}

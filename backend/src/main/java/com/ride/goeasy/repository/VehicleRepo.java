package com.ride.goeasy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.ride.goeasy.entity.Vehicle;

public interface VehicleRepo extends JpaRepository<Vehicle, Integer> {
	
	
	// Fetch all vehicles that are Available
	// Fetch all vehicles that are Available
	@Query("SELECT v FROM Vehicle v WHERE v.avlStatus = 'AVAILABLE'")
	List<Vehicle> findAvailableVehicles();
	
	

	// Find by city (Case Insensitive)
	@Query("SELECT v FROM Vehicle v WHERE v.avlStatus = 'AVAILABLE' AND LOWER(v.city) = LOWER(:city)")
	List<Vehicle> findAvailableVehiclesInCity(String city);

	
	Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}

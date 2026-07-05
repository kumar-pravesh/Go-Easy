package com.ride.goeasy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ride.goeasy.entity.Vehicle;

public interface VehicleRepo extends JpaRepository<Vehicle, Integer> {

    @Query("SELECT v FROM Vehicle v WHERE v.avlStatus = 'AVAILABLE'")
    List<Vehicle> findAvailableVehicles();

    @Query("SELECT v FROM Vehicle v WHERE v.avlStatus = 'AVAILABLE' AND LOWER(v.city) = LOWER(:city)")
    List<Vehicle> findAvailableVehiclesInCity(@Param("city") String city);

    // Female drivers only
    @Query("SELECT v FROM Vehicle v JOIN v.driver d WHERE v.avlStatus = 'AVAILABLE' AND LOWER(v.city) = LOWER(:city) AND LOWER(d.gender) = 'female'")
    List<Vehicle> findAvailableFemaleDriverVehiclesInCity(@Param("city") String city);

    // Green vehicles only (CNG or ELECTRIC)
    @Query("SELECT v FROM Vehicle v WHERE v.avlStatus = 'AVAILABLE' AND LOWER(v.city) = LOWER(:city) AND LOWER(v.fuelType) IN ('cng', 'electric')")
    List<Vehicle> findAvailableGreenVehiclesInCity(@Param("city") String city);

    // Female drivers + green
    @Query("SELECT v FROM Vehicle v JOIN v.driver d WHERE v.avlStatus = 'AVAILABLE' AND LOWER(v.city) = LOWER(:city) AND LOWER(d.gender) = 'female' AND LOWER(v.fuelType) IN ('cng', 'electric')")
    List<Vehicle> findAvailableFemaleDriverGreenVehiclesInCity(@Param("city") String city);

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}

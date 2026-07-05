package com.ride.goeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.AvailableVehicleDTO;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.VehicleService;

@RestController
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping("/availableVehicles")
    public ResponseEntity<ResponseStructure<AvailableVehicleDTO>> getAvailableVehicles(
            @RequestParam Long mobile,
            @RequestParam String destination,
            @RequestParam(required = false) Boolean preferFemaleDriver,
            @RequestParam(required = false) Boolean greenOnly,
            @RequestParam(required = false) String ridePreference) {

        AvailableVehicleDTO dto = vehicleService.getAvailableVehicles(
                mobile, destination, preferFemaleDriver, greenOnly, ridePreference);

        ResponseStructure<AvailableVehicleDTO> structure = new ResponseStructure<>();
        structure.setStatusCode(200);
        structure.setMessage("Available Vehicles Fetched Successfully");
        structure.setData(dto);

        return new ResponseEntity<>(structure, HttpStatus.OK);
    }
}

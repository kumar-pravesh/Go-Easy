package com.ride.goeasy.dto;

import java.util.List;

import com.ride.goeasy.entity.Customer;

public class AvailableVehicleDTO {
	private Customer customer;
    private double distance;
    private String source;
    private String destination;

    private List<VehicleDetailDTO> vehicles;

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public List<VehicleDetailDTO> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<VehicleDetailDTO> vehicles) {
		this.vehicles = vehicles;
	}
    
    

}

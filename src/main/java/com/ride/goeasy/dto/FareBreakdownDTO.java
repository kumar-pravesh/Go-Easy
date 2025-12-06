package com.ride.goeasy.dto;

public class FareBreakdownDTO {

	private double baseFare;
	private double distanceKm;
	private double perKmCharge;
	private double vehicleMultiplier;
	private double dynamicFees;
	private double totalFare;
	private String estimatedTime;

	// Getters & Setters
	public double getBaseFare() {
		return baseFare;
	}

	public void setBaseFare(double baseFare) {
		this.baseFare = baseFare;
	}

	public double getDistanceKm() {
		return distanceKm;
	}

	public void setDistanceKm(double distanceKm) {
		this.distanceKm = distanceKm;
	}

	public double getPerKmCharge() {
		return perKmCharge;
	}

	public void setPerKmCharge(double perKmCharge) {
		this.perKmCharge = perKmCharge;
	}

	public double getVehicleMultiplier() {
		return vehicleMultiplier;
	}

	public void setVehicleMultiplier(double vehicleMultiplier) {
		this.vehicleMultiplier = vehicleMultiplier;
	}

	public double getDynamicFees() {
		return dynamicFees;
	}

	public void setDynamicFees(double dynamicFees) {
		this.dynamicFees = dynamicFees;
	}

	public double getTotalFare() {
		return totalFare;
	}

	public void setTotalFare(double totalFare) {
		this.totalFare = totalFare;
	}

	public String getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(String estimatedTime) {
		this.estimatedTime = estimatedTime;
	}
}

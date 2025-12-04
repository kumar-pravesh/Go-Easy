package com.ride.goeasy.entity;

import org.springframework.stereotype.Service;

import jakarta.persistence.OneToOne;

@Service
public class Vehicle {
  private int vehicleId;
  private String vehicleNumber;
  private String vehicleName;
  private String vehicleType;
  private String vehicleModel;
  private int vehicleCapacity;
  private double lat;
  private double lon;
  private String avlStatus;
  private double pricePerKm;
  
  @OneToOne
  private Driver driver;

  public int getVehicleId() {
	return vehicleId;
  }

  public void setVehicleId(int vehicleId) {
	this.vehicleId = vehicleId;
  }

  public String getVehicleNumber() {
	return vehicleNumber;
  }

  public void setVehicleNumber(String vehicleNumber) {
	this.vehicleNumber = vehicleNumber;
  }

  public String getVehicleName() {
	return vehicleName;
  }

  public void setVehicleName(String vehicleName) {
	this.vehicleName = vehicleName;
  }

  public String getVehicleType() {
	return vehicleType;
  }

  public void setVehicleType(String vehicleType) {
	this.vehicleType = vehicleType;
  }

  public String getVehicleModel() {
	return vehicleModel;
  }

  public void setVehicleModel(String vehicleModel) {
	this.vehicleModel = vehicleModel;
  }

  public int getVehicleCapacity() {
	return vehicleCapacity;
  }

  public void setVehicleCapacity(int vehicleCapacity) {
	this.vehicleCapacity = vehicleCapacity;
  }

  public double getLat() {
	return lat;
  }

  public void setLat(double lat) {
	this.lat = lat;
  }

  public double getLon() {
	return lon;
  }

  public void setLon(double lon) {
	this.lon = lon;
  }

  public String getAvlStatus() {
	return avlStatus;
  }

  public void setAvlStatus(String avlStatus) {
	this.avlStatus = avlStatus;
  }

  public double getPricePerKm() {
	return pricePerKm;
  }

  public void setPricePerKm(double pricePerKm) {
	this.pricePerKm = pricePerKm;
  }

  public Driver getDriver() {
	return driver;
  }

  public void setDriver(Driver driver) {
	this.driver = driver;
  }

  public Vehicle(int vehicleId, String vehicleNumber, String vehicleName, String vehicleType, String vehicleModel,
		int vehicleCapacity, double lat, double lon, String avlStatus, double pricePerKm, Driver driver) {
	super();
	this.vehicleId = vehicleId;
	this.vehicleNumber = vehicleNumber;
	this.vehicleName = vehicleName;
	this.vehicleType = vehicleType;
	this.vehicleModel = vehicleModel;
	this.vehicleCapacity = vehicleCapacity;
	this.lat = lat;
	this.lon = lon;
	this.avlStatus = avlStatus;
	this.pricePerKm = pricePerKm;
	this.driver = driver;
  }

  public Vehicle() {
	super();
	// TODO Auto-generated constructor stub
  }

  @Override
  public String toString() {
	return "Vehicle [vehicleId=" + vehicleId + ", vehicleNumber=" + vehicleNumber + ", vehicleName=" + vehicleName
			+ ", vehicleType=" + vehicleType + ", vehicleModel=" + vehicleModel + ", vehicleCapacity=" + vehicleCapacity
			+ ", lat=" + lat + ", lon=" + lon + ", avlStatus=" + avlStatus + ", pricePerKm=" + pricePerKm + ", driver="
			+ driver + "]";
  }

  
  
  
}

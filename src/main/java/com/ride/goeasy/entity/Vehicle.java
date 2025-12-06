package com.ride.goeasy.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
public class Vehicle {

	@Id
	private Integer id; // Same as driver ID

	private String vehicleName;
	private String vehicleType;
	private String vehicleNumber;
	private String vehicleModel;
	private Integer vehicleCapacity;
	private String city;
	private String avlStatus;
	private Double pricePerKm;

	@OneToOne
	@MapsId
	@JsonIgnore
	@JoinColumn(name = "id")
	private Driver driver;

	public Vehicle() {

	}

	public Vehicle(Integer id, String vehicleName, String vehicleType, String vehicleNumber, String vehicleModel,
			Integer vehicleCapacity, String city, String avlStatus, Double pricePerKm, Driver driver) {
		super();
		this.id = id;
		this.vehicleName = vehicleName;
		this.vehicleType = vehicleType;
		this.vehicleNumber = vehicleNumber;
		this.vehicleModel = vehicleModel;
		this.vehicleCapacity = vehicleCapacity;
		this.city = city;
		this.avlStatus = avlStatus;
		this.pricePerKm = pricePerKm;
		this.driver = driver;
	}

	public Integer getId() {
		return id;
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

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getVehicleModel() {
		return vehicleModel;
	}

	public void setVehicleModel(String vehicleModel) {
		this.vehicleModel = vehicleModel;
	}

	public Integer getVehicleCapacity() {
		return vehicleCapacity;
	}

	public void setVehicleCapacity(Integer vehicleCapacity) {
		this.vehicleCapacity = vehicleCapacity;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getAvlStatus() {
		return avlStatus;
	}

	public void setAvlStatus(String avlStatus) {
		this.avlStatus = avlStatus;
	}

	public Double getPricePerKm() {
		return pricePerKm;
	}

	public void setPricePerKm(Double pricePerKm) {
		this.pricePerKm = pricePerKm;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	@Override
	public String toString() {
		return "Vehicle [id=" + id + ", vehicleName=" + vehicleName + ", vehicleType=" + vehicleType
				+ ", vehicleNumber=" + vehicleNumber + ", vehicleModel=" + vehicleModel + ", vehicleCapacity="
				+ vehicleCapacity + ", city=" + city + ", avlStatus=" + avlStatus + ", pricePerKm=" + pricePerKm
				+ ", driver=" + driver + "]";
	}

}

package com.ride.goeasy.entity;

import org.antlr.v4.runtime.misc.NotNull;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
public class Vehicle {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id; // Same as driver ID

	private String vehicleName;
	private String vehicleType;
	private String vehicleNumber;
	private String vehicleModel;
	@NotNull
	private Integer vehicleCapacity;
	@Column(nullable = false)
	private String city;

	private Double latitude;
	private Double longitude;

	private String avlStatus;
	private Double pricePerKm;

	@Column(nullable = true)
	private Double avgspeed;

	@OneToOne
	@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"vehicle", "dblist", "userr"})
	@JoinColumn(name = "driver_id", nullable = false)
	private Driver driver;

	public Vehicle() {

	}

	public Vehicle(Integer id, String vehicleName, String vehicleType, String vehicleNumber, String vehicleModel,
			Integer vehicleCapacity, String city, Double latitude, Double longitude, String avlStatus,
			Double pricePerKm, Double avgspeed, Driver driver) {
		super();
		this.id = id;
		this.vehicleName = vehicleName;
		this.vehicleType = vehicleType;
		this.vehicleNumber = vehicleNumber;
		this.vehicleModel = vehicleModel;
		this.vehicleCapacity = vehicleCapacity;
		this.city = city;
		this.latitude = latitude;
		this.longitude = longitude;
		this.avlStatus = avlStatus;
		this.pricePerKm = pricePerKm;
		this.avgspeed = avgspeed;
		this.driver = driver;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
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

	public Double getAvgspeed() {
		return avgspeed;
	}

	public void setAvgspeed(Double avgspeed) {
		this.avgspeed = avgspeed;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

}

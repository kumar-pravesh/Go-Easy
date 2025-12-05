package com.ride.goeasy.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;

@Entity
public class Vehicle {

    @Id
    private int id; // Same as driver ID

    
    
    private String vehicleName;
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleModel;
    private Integer vehicleCapacity;
    private Double lat;
    private Double lon;
    private String avlStatus;
    private Double pricePerKm;

    @OneToOne
    @MapsId
    @JsonBackReference
    @JoinColumn(name = "v_id")
    private Driver driver;

    public Vehicle() {
    }

    public Vehicle(String vehicleName, String vehicleType, String vehicleNumber, String vehicleModel,
                   Integer vehicleCapacity, Double lat, Double lon, String avlStatus, Double pricePerKm ) {
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
        this.vehicleCapacity = vehicleCapacity;
        this.lat = lat;
        this.lon = lon;
        this.avlStatus = avlStatus;
        this.pricePerKm = pricePerKm;
       
    }

    public int getId() {
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
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
        return "Vehicle{" +
                "id=" + id +
                ", vehicleName='" + vehicleName + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", vehicleNumber='" + vehicleNumber + '\'' +
                ", vehicleModel='" + vehicleModel + '\'' +
                ", vehicleCapacity=" + vehicleCapacity +
                ", lat=" + lat +
                ", lon=" + lon +
                ", avlStatus='" + avlStatus + '\'' +
                ", pricePerKm=" + pricePerKm +
                 
                '}';
    }
}

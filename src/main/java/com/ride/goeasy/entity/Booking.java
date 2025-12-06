package com.ride.goeasy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	// MANY BOOKINGS → ONE CUSTOMER
	@ManyToOne
	@JoinColumn(name = "customer_id")
	private Customer customer;

	// MANY BOOKINGS → ONE DRIVER
	@ManyToOne
	@JoinColumn(name = "driver_id")
	private Driver driver;

	private String sourceLocation;
	private String destinationLocation;

	private Double distance; // kilometers
	private Double fare; // price
	private String estimatedTime; // e.g. "20 mins"

	private String bookingStatus; // BOOKED, CANCELLED, COMPLETED

	public Booking() {
	}

	public Booking(int id, Customer customer, Driver driver, String sourceLocation, String destinationLocation,
			Double distance, Double fare, String estimatedTime, String bookingStatus) {

		this.id = id;
		this.customer = customer;
		this.driver = driver;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.distance = distance;
		this.fare = fare;
		this.estimatedTime = estimatedTime;
		this.bookingStatus = bookingStatus;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Customer getCustomer() {
		return customer;

	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Driver getDriver() {
		return driver;
	}

	public void setDriver(Driver driver) {
		this.driver = driver;
	}

	public String getSourceLocation() {
		return sourceLocation;
	}

	public void setSourceLocation(String sourceLocation) {
		this.sourceLocation = sourceLocation;
	}

	public String getDestinationLocation() {
		return destinationLocation;
	}

	public void setDestinationLocation(String destinationLocation) {
		this.destinationLocation = destinationLocation;
	}

	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	public Double getFare() {
		return fare;
	}

	public void setFare(Double fare) {
		this.fare = fare;
	}

	public String getEstimatedTime() {
		return estimatedTime;
	}

	public void setEstimatedTime(String estimatedTime) {
		this.estimatedTime = estimatedTime;
	}

	public String getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}
}

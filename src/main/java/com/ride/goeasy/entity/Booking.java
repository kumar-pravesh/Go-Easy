package com.ride.goeasy.entity;

import com.ride.goeasy.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	// MANY BOOKINGS → ONE CUSTOMER
	@ManyToOne

	private Customer customer;

	// MANY BOOKINGS → ONE Vehicle
	@ManyToOne

	private Vehicle vehicle;

	private String sourceLocation;
	private String destinationLocation;

	private Double distance;
	private Double fare;
	private String estimatedTime;

	private String startOtp; // before ride
	private String endOtp; // after ride

	private boolean startOtpVerified;
	private boolean endOtpVerified;

	@Enumerated(EnumType.STRING)
	private BookingStatus bookingStatus;

	private boolean activeBookingFlag;

	@OneToOne(cascade = CascadeType.ALL)
	private Payment payment;
	
	

	public String getStartOtp() {
		return startOtp;
	}

	public void setStartOtp(String startOtp) {
		this.startOtp = startOtp;
	}

	public String getEndOtp() {
		return endOtp;
	}

	public void setEndOtp(String endOtp) {
		this.endOtp = endOtp;
	}

	public boolean isStartOtpVerified() {
		return startOtpVerified;
	}

	public void setStartOtpVerified(boolean startOtpVerified) {
		this.startOtpVerified = startOtpVerified;
	}

	public boolean isEndOtpVerified() {
		return endOtpVerified;
	}

	public void setEndOtpVerified(boolean endOtpVerified) {
		this.endOtpVerified = endOtpVerified;
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

	public Vehicle getVehicle() {
		return vehicle;
	}

	public void setVehicle(Vehicle vehicle) {
		this.vehicle = vehicle;
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

	public BookingStatus getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(BookingStatus bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public boolean isActiveBookingFlag() {
		return activeBookingFlag;
	}

	public void setActiveBookingFlag(boolean activeBookingFlag) {
		this.activeBookingFlag = activeBookingFlag;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public Booking(int id, Customer customer, Vehicle vehicle, String sourceLocation, String destinationLocation,
			Double distance, Double fare, String estimatedTime, BookingStatus bookingStatus, boolean activeBookingFlag,
			Payment payment) {
		super();
		this.id = id;
		this.customer = customer;
		this.vehicle = vehicle;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.distance = distance;
		this.fare = fare;
		this.estimatedTime = estimatedTime;
		this.bookingStatus = bookingStatus;
		this.activeBookingFlag = activeBookingFlag;
		this.payment = payment;
	}

	public Booking() {
		super();
		// TODO Auto-generated constructor stub
	}

}

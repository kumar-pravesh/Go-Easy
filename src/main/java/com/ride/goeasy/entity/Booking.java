package com.ride.goeasy.entity;
import jakarta.persistence.Entity;
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

	private Double distance; // kilometers
	private Double fare; // price
	private String estimatedTime; // e.g. "20 mins"

	private String bookingStatus; // BOOKED, CANCELLED, COMPLETED
	
	private boolean activeBookingFlag;
	
	@OneToOne
	private Payment payment;

	public Booking(Customer customer, Vehicle vehicle, String sourceLocation, String destinationLocation,
			Double distance, Double fare, String estimatedTime, String bookingStatus, Payment payment) {
		super();
		this.customer = customer;
		this.vehicle = vehicle;
		this.sourceLocation = sourceLocation;
		this.destinationLocation = destinationLocation;
		this.distance = distance;
		this.fare = fare;
		this.estimatedTime = estimatedTime;
		this.bookingStatus = bookingStatus;
		this.payment = payment;
	}

	

	public int getId() {
		return id;
	}

	 

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	



	public boolean isActiveBookingFlag() {
		return activeBookingFlag;
	}



	public void setActiveBookingFlag(boolean activeBookingFlag) {
		this.activeBookingFlag = activeBookingFlag;
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

	public String getBookingStatus() {
		return bookingStatus;
	}

	public void setBookingStatus(String bookingStatus) {
		this.bookingStatus = bookingStatus;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public Booking() {
		super();
		// TODO Auto-generated constructor stub
	}
	

	 

	
}

package com.ride.goeasy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

@Entity
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  
  @ManyToOne
  private Customer customer;
  
  @ManyToOne
  private Vehicle vehicle;
  @OneToOne
  private Booking booking;
  
  private double amount;
  private String paymentType;
  
  
  public Payment(Customer customer, Vehicle vehicle, Booking booking, double amount, String paymentType) {
	super();
	this.customer = customer;
	this.vehicle = vehicle;
	this.booking = booking;
	this.amount = amount;
	this.paymentType = paymentType;
	
	
  }


  public Payment() {
	super();
	// TODO Auto-generated constructor stub
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


  public Vehicle getVehicle() {
	return vehicle;
  }


  public void setVehicle(Vehicle vehicle) {
	this.vehicle = vehicle;
  }


  public Booking getBooking() {
	return booking;
  }


  public void setBooking(Booking booking) {
	this.booking = booking;
  }


  public double getAmount() {
	return amount;
  }


  public void setAmount(double amount) {
	this.amount = amount;
  }


  public String getPaymentType() {
	return paymentType;
  }


  public void setPaymentType(String paymentType) {
	this.paymentType = paymentType;
  }
  
  
  
}

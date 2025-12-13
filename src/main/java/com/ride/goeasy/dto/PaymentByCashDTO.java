package com.ride.goeasy.dto;

public class PaymentByCashDTO {
  private int bookingId;
  private int customerId;
  private int driverId;
  
  private double amountPaid;
  private String paymentType;
  
  private String paymentStatus;

  public int getBookingId() {
	return bookingId;
  }

  public void setBookingId(int bookingId) {
	this.bookingId = bookingId;
  }

  public int getCustomerId() {
	return customerId;
  }

  public void setCustomerId(int customerId) {
	this.customerId = customerId;
  }

  public int getDriverId() {
	return driverId;
  }

  public void setDriverId(int driverId) {
	this.driverId = driverId;
  }

  public double getAmountPaid() {
	return amountPaid;
  }

  public void setAmountPaid(double amountPaid) {
	this.amountPaid = amountPaid;
  }

  public String getPaymentType() {
	return paymentType;
  }

  public void setPaymentType(String paymentType) {
	this.paymentType = paymentType;
  }

  public String getPaymentStatus() {
	return paymentStatus;
  }

  public void setPaymentStatus(String paymentStatus) {
	this.paymentStatus = paymentStatus;
  }
  
}

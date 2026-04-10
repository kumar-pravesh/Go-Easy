package com.ride.goeasy.dto;

public class PaymentDTO {
 private double fare;
 private byte[] qr;
 public double getFare() {
	return fare;
 }
 public void setFare(double fare) {
	this.fare = fare;
 }
 public byte[] getQr() {
	return qr;
 }
 public void setQr(byte[] qr) {
	this.qr = qr;
 }
 
}

package com.ride.goeasy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;



@Entity
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    // Many payments can belong to one customer
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    
    
    // Many payments can belong to one vehicle
    
    @ManyToOne
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    // One booking has exactly one payment
    
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private double amount;

    private String paymentType;   //  "UPI", "CASH"

    private String paymentStatus; //  "SUCCESS", "FAILED", "PENDING"

    public Payment() {}

    public Payment(Customer customer, Vehicle vehicle, Booking booking, 
                   double amount, String paymentType, String paymentStatus) {
        this.customer = customer;
        this.vehicle = vehicle;
        this.booking = booking;
        this.amount = amount;
        this.paymentType = paymentType;
        this.paymentStatus = paymentStatus;
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

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
}


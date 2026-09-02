package com.ride.goeasy.dto;

import java.time.LocalDate;

public class RideReceiptDTO {
    private int bookingId;
    private String receiptNumber;
    private LocalDate rideDate;
    private String sourceLocation;
    private String destinationLocation;
    private Double distance;
    private String estimatedTime;
    private String bookingStatus;

    private String customerName;
    private Long customerMobile;
    private String driverName;
    private Long driverMobile;
    private String vehicleNumber;
    private String vehicleModel;

    private Double baseFare;
    private Double distanceFare;
    private Double penaltyAmount;
    private Double waitingCharge;
    private Double nightCharge;
    private Double platformFee;
    private Double tax;
    private Double discount;
    private Double pricePerKm;
    private Double totalFare;
    private boolean fareLocked;

    private String paymentMode;
    private String paymentType;
    private String paymentStatus;
    private Double amountPaid;

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public String getReceiptNumber() { return receiptNumber; }
    public void setReceiptNumber(String receiptNumber) { this.receiptNumber = receiptNumber; }

    public LocalDate getRideDate() { return rideDate; }
    public void setRideDate(LocalDate rideDate) { this.rideDate = rideDate; }

    public String getSourceLocation() { return sourceLocation; }
    public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

    public String getDestinationLocation() { return destinationLocation; }
    public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

    public Double getDistance() { return distance; }
    public void setDistance(Double distance) { this.distance = distance; }

    public String getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

    public String getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(String bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public Long getCustomerMobile() { return customerMobile; }
    public void setCustomerMobile(Long customerMobile) { this.customerMobile = customerMobile; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public Long getDriverMobile() { return driverMobile; }
    public void setDriverMobile(Long driverMobile) { this.driverMobile = driverMobile; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public Double getBaseFare() { return baseFare; }
    public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }

    public Double getDistanceFare() { return distanceFare; }
    public void setDistanceFare(Double distanceFare) { this.distanceFare = distanceFare; }

    public Double getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(Double penaltyAmount) { this.penaltyAmount = penaltyAmount; }

    public Double getWaitingCharge() { return waitingCharge; }
    public void setWaitingCharge(Double waitingCharge) { this.waitingCharge = waitingCharge; }

    public Double getNightCharge() { return nightCharge; }
    public void setNightCharge(Double nightCharge) { this.nightCharge = nightCharge; }

    public Double getPlatformFee() { return platformFee; }
    public void setPlatformFee(Double platformFee) { this.platformFee = platformFee; }

    public Double getTax() { return tax; }
    public void setTax(Double tax) { this.tax = tax; }

    public Double getDiscount() { return discount; }
    public void setDiscount(Double discount) { this.discount = discount; }

    public Double getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(Double pricePerKm) { this.pricePerKm = pricePerKm; }

    public Double getTotalFare() { return totalFare; }
    public void setTotalFare(Double totalFare) { this.totalFare = totalFare; }

    public boolean isFareLocked() { return fareLocked; }
    public void setFareLocked(boolean fareLocked) { this.fareLocked = fareLocked; }

    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(Double amountPaid) { this.amountPaid = amountPaid; }
}

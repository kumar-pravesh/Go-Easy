package com.ride.goeasy.dto;

public class PublicTrackingDTO {
    private Integer bookingId;
    private String sourceLocation;
    private String destinationLocation;
    private String bookingStatus;
    private String driverName;
    private String vehicleModel;
    private String vehicleNumber;
    private Double driverLatitude;
    private Double driverLongitude;

    public PublicTrackingDTO() {}

    public PublicTrackingDTO(Integer bookingId, String sourceLocation, String destinationLocation, String bookingStatus,
                             String driverName, String vehicleModel, String vehicleNumber, 
                             Double driverLatitude, Double driverLongitude) {
        this.bookingId = bookingId;
        this.sourceLocation = sourceLocation;
        this.destinationLocation = destinationLocation;
        this.bookingStatus = bookingStatus;
        this.driverName = driverName;
        this.vehicleModel = vehicleModel;
        this.vehicleNumber = vehicleNumber;
        this.driverLatitude = driverLatitude;
        this.driverLongitude = driverLongitude;
    }

    public Integer getBookingId() {
        return bookingId;
    }
    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
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

    public String getBookingStatus() {
        return bookingStatus;
    }
    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public String getDriverName() {
        return driverName;
    }
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }
    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }
    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public Double getDriverLatitude() {
        return driverLatitude;
    }
    public void setDriverLatitude(Double driverLatitude) {
        this.driverLatitude = driverLatitude;
    }

    public Double getDriverLongitude() {
        return driverLongitude;
    }
    public void setDriverLongitude(Double driverLongitude) {
        this.driverLongitude = driverLongitude;
    }
}

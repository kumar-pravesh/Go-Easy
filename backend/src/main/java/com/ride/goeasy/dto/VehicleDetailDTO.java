package com.ride.goeasy.dto;

public class VehicleDetailDTO {
    private String model;
    private String vehicleNumber;
    private double pricePerKm;
    private double averageSpeed;
    private double penalty;
    private double totalAmout;
    private double estimatedFare;
    private double estimatedTime;
    private double distance;
    private String vehicleModel;
    private Integer vehicleCapacity;

    // Driver info
    private String driverName;
    private String driverGender;
    private String driverRidePreference;  // ANY / SILENT / FRIENDLY
    private String driverVerificationTier; // BRONZE / SILVER / GOLD
    private Double driverRating;
    private Integer driverTotalRatings;
    private Double driverReliabilityScore;

    // Vehicle info
    private String fuelType; // PETROL / DIESEL / CNG / ELECTRIC

    // Transparent fare breakdown
    private double baseFare;
    private double distanceFare;

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public Integer getVehicleCapacity() { return vehicleCapacity; }
    public void setVehicleCapacity(Integer vehicleCapacity) { this.vehicleCapacity = vehicleCapacity; }

    public double getPenalty() { return penalty; }
    public void setPenalty(double penalty) { this.penalty = penalty; }

    public double getTotalAmout() { return totalAmout; }
    public void setTotalAmout(double totalAmout) { this.totalAmout = totalAmout; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public double getPricePerKm() { return pricePerKm; }
    public void setPricePerKm(double pricePerKm) { this.pricePerKm = pricePerKm; }

    public double getAverageSpeed() { return averageSpeed; }
    public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }

    public double getEstimatedFare() { return estimatedFare; }
    public void setEstimatedFare(double estimatedFare) { this.estimatedFare = estimatedFare; }

    public double getEstimatedTime() { return estimatedTime; }
    public void setEstimatedTime(double estimatedTime) { this.estimatedTime = estimatedTime; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverGender() { return driverGender; }
    public void setDriverGender(String driverGender) { this.driverGender = driverGender; }

    public String getDriverRidePreference() { return driverRidePreference; }
    public void setDriverRidePreference(String driverRidePreference) { this.driverRidePreference = driverRidePreference; }

    public String getDriverVerificationTier() { return driverVerificationTier; }
    public void setDriverVerificationTier(String driverVerificationTier) { this.driverVerificationTier = driverVerificationTier; }

    public Double getDriverRating() { return driverRating; }
    public void setDriverRating(Double driverRating) { this.driverRating = driverRating; }

    public Integer getDriverTotalRatings() { return driverTotalRatings; }
    public void setDriverTotalRatings(Integer driverTotalRatings) { this.driverTotalRatings = driverTotalRatings; }

    public Double getDriverReliabilityScore() { return driverReliabilityScore; }
    public void setDriverReliabilityScore(Double driverReliabilityScore) { this.driverReliabilityScore = driverReliabilityScore; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public double getBaseFare() { return baseFare; }
    public void setBaseFare(double baseFare) { this.baseFare = baseFare; }

    public double getDistanceFare() { return distanceFare; }
    public void setDistanceFare(double distanceFare) { this.distanceFare = distanceFare; }
}

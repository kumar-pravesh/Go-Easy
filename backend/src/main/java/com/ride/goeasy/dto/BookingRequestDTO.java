package com.ride.goeasy.dto;

public class BookingRequestDTO {
	  

    private String sourceLocation;
    private String destinationLocation;

    private Double distance;       // you already calculate from matrix API
    private Double fare;           // calculated using vehicle.pricePerKm
    private String estimatedTime;  // also calculated

    private String vehicleNumber;  // ⭐ customer selects one vehicle

    // OPTIONAL but recommended:
    private String paymentMode;    // UPI, CASH, CARD

    // null = immediate ride; non-null = scheduled ride (ISO-8601 string, e.g. "2025-06-15T09:30:00")
    private String scheduledTime;

    private boolean useCorporateWallet = false;

    public boolean isUseCorporateWallet() { return useCorporateWallet; }
    public void setUseCorporateWallet(boolean useCorporateWallet) { this.useCorporateWallet = useCorporateWallet; }

	public String getScheduledTime() { return scheduledTime; }
	public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }

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

	public String getVehicleNumber() {
		return vehicleNumber;
	}

	public void setVehicleNumber(String vehicleNumber) {
		this.vehicleNumber = vehicleNumber;
	}

	public String getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String paymentMode) {
		this.paymentMode = paymentMode;
	}
    
    
	
}

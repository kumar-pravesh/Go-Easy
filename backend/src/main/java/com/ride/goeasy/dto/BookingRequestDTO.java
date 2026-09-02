package com.ride.goeasy.dto;

public class BookingRequestDTO {
	  

    private String sourceLocation;
    private String destinationLocation;

    private Double distance;       // you already calculate from matrix API
    private Double fare;           // calculated using vehicle.pricePerKm
    private String estimatedTime;  // also calculated
    private Double baseFare;
    private Double distanceFare;
    private Double penalty;
    private Double totalAmount;
    private Double pricePerKm;

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

	public Double getBaseFare() { return baseFare; }
	public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }

	public Double getDistanceFare() { return distanceFare; }
	public void setDistanceFare(Double distanceFare) { this.distanceFare = distanceFare; }

	public Double getPenalty() { return penalty; }
	public void setPenalty(Double penalty) { this.penalty = penalty; }

	public Double getTotalAmount() { return totalAmount; }
	public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

	public Double getPricePerKm() { return pricePerKm; }
	public void setPricePerKm(Double pricePerKm) { this.pricePerKm = pricePerKm; }

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

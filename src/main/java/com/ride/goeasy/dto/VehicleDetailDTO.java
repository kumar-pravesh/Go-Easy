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
	    
	    
	    
	    
		public double getPenalty() {
			return penalty;
		}
		public void setPenalty(double penalty) {
			this.penalty = penalty;
		}
		public double getTotalAmout() {
			return totalAmout;
		}
		public void setTotalAmout(double totalAmout) {
			this.totalAmout = totalAmout;
		}
		public String getModel() {
			return model;
		}
		public void setModel(String model) {
			this.model = model;
		}
		public String getVehicleNumber() {
			return vehicleNumber;
		}
		public void setVehicleNumber(String vehicleNumber) {
			this.vehicleNumber = vehicleNumber;
		}
		public double getPricePerKm() {
			return pricePerKm;
		}
		public void setPricePerKm(double pricePerKm) {
			this.pricePerKm = pricePerKm;
		}
		public double getAverageSpeed() {
			return averageSpeed;
		}
		public void setAverageSpeed(double averageSpeed) {
			this.averageSpeed = averageSpeed;
		}
		public double getEstimatedFare() {
			return estimatedFare;
		}
		public void setEstimatedFare(double estimatedFare) {
			this.estimatedFare = estimatedFare;
		}
		public double getEstimatedTime() {
			return estimatedTime;
		}
		public void setEstimatedTime(double estimatedTime) {
			this.estimatedTime = estimatedTime;
		}
	    
	    

}

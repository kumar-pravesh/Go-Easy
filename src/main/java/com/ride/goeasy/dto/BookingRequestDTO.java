package com.ride.goeasy.dto;

public class BookingRequestDTO {
	 private int customerId;
	    private int driverId;
	    private String pickupLocation;   
	    private String dropLocation;     

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

	    public String getPickupLocation() {
	        return pickupLocation;
	    }
	    public void setPickupLocation(String pickupLocation) {
	        this.pickupLocation = pickupLocation;
	    }

	    public String getDropLocation() {
	        return dropLocation;
	    }
	    public void setDropLocation(String dropLocation) {
	        this.dropLocation = dropLocation;
	    }
}

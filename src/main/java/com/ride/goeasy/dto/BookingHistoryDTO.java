package com.ride.goeasy.dto;

import java.util.List;

public class BookingHistoryDTO {

	private double totalAmt;
	private List<RideDetailsDTO> rlist;
	
	
	public double getTotalAmt() {
		return totalAmt;
	}
	public void setTotalAmt(double totalAmt) {
		this.totalAmt = totalAmt;
	}
	public List<RideDetailsDTO> getRlist() {
		return rlist;
	}
	public void setRlist(List<RideDetailsDTO> rlist) {
		this.rlist = rlist;
	}
 
	
	
}

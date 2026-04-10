package com.ride.goeasy.dto;

import java.util.List;

public class MatrixResponse {
	 private List<List<Double>> distances;
	    private List<List<Double>> durations;

	    public List<List<Double>> getDistances() {
	        return distances;
	    }

	    public void setDistances(List<List<Double>> distances) {
	        this.distances = distances;
	    }

	    public List<List<Double>> getDurations() {
	        return durations;
	    }

	    public void setDurations(List<List<Double>> durations) {
	        this.durations = durations;
	    }

}

package com.ride.goeasy.dto;

import java.util.List;

public class GoogleDistanceResponse {

	 private List<String> destination_addresses;
	    private List<String> origin_addresses;
	    private List<Row> rows;
	    private String status;

	    public List<String> getDestination_addresses() { return destination_addresses; }
	    public void setDestination_addresses(List<String> destination_addresses) { this.destination_addresses = destination_addresses; }

	    public List<String> getOrigin_addresses() { return origin_addresses; }
	    public void setOrigin_addresses(List<String> origin_addresses) { this.origin_addresses = origin_addresses; }

	    public List<Row> getRows() { return rows; }
	    public void setRows(List<Row> rows) { this.rows = rows; }

	    public String getStatus() { return status; }
	    public void setStatus(String status) { this.status = status; }

	    // Nested classes
	    public static class Row {
	        private List<Element> elements;
	        public List<Element> getElements() { return elements; }
	        public void setElements(List<Element> elements) { this.elements = elements; }
	    }

	    public static class Element {
	        private Distance distance;
	        private Duration duration;
	        private String status;
	        public Distance getDistance() { return distance; }
	        public void setDistance(Distance distance) { this.distance = distance; }
	        public Duration getDuration() { return duration; }
	        public void setDuration(Duration duration) { this.duration = duration; }
	        public String getStatus() { return status; }
	        public void setStatus(String status) { this.status = status; }
	    }

	    public static class Distance {
	        private String text;
	        private double value; // in meters
	        public String getText() { return text; }
	        public void setText(String text) { this.text = text; }
	        public double getValue() { return value; }
	        public void setValue(double value) { this.value = value; }
	    }

	    public static class Duration {
	        private String text;
	        private int value; // in seconds
	        public String getText() { return text; }
	        public void setText(String text) { this.text = text; }
	        public int getValue() { return value; }
	        public void setValue(int value) { this.value = value; }
	    }

	    // Convenience methods
	    public double getDistanceKm() {
	        if (rows != null && !rows.isEmpty() && rows.get(0).getElements() != null && !rows.get(0).getElements().isEmpty()) {
	            return rows.get(0).getElements().get(0).getDistance().getValue() / 1000.0;
	        }
	        return 0;
	    }

	    public String getDurationText() {
	        if (rows != null && !rows.isEmpty() && rows.get(0).getElements() != null && !rows.get(0).getElements().isEmpty()) {
	            return rows.get(0).getElements().get(0).getDuration().getText();
	        }
	        return "";
	    }
}

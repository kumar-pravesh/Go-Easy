package com.ride.goeasy.dto;

public class EarningsDTO {
    private Double totalEarnings;
    private Double todayEarnings;
    private Double thisWeekEarnings;
    private Integer totalTrips;
    private Integer completedTrips;
    private Integer cancelledTrips;
    private Double averageFare;
    private Double reliabilityScore;
    private Double averageRating;
    private Integer totalRatings;

    public EarningsDTO() {
    }

    public EarningsDTO(Double totalEarnings, Double todayEarnings, Double thisWeekEarnings, Integer totalTrips,
                       Integer completedTrips, Integer cancelledTrips, Double averageFare, Double reliabilityScore,
                       Double averageRating, Integer totalRatings) {
        this.totalEarnings = totalEarnings;
        this.todayEarnings = todayEarnings;
        this.thisWeekEarnings = thisWeekEarnings;
        this.totalTrips = totalTrips;
        this.completedTrips = completedTrips;
        this.cancelledTrips = cancelledTrips;
        this.averageFare = averageFare;
        this.reliabilityScore = reliabilityScore;
        this.averageRating = averageRating;
        this.totalRatings = totalRatings;
    }

    public Double getTotalEarnings() {
        return totalEarnings;
    }

    public void setTotalEarnings(Double totalEarnings) {
        this.totalEarnings = totalEarnings;
    }

    public Double getTodayEarnings() {
        return todayEarnings;
    }

    public void setTodayEarnings(Double todayEarnings) {
        this.todayEarnings = todayEarnings;
    }

    public Double getThisWeekEarnings() {
        return thisWeekEarnings;
    }

    public void setThisWeekEarnings(Double thisWeekEarnings) {
        this.thisWeekEarnings = thisWeekEarnings;
    }

    public Integer getTotalTrips() {
        return totalTrips;
    }

    public void setTotalTrips(Integer totalTrips) {
        this.totalTrips = totalTrips;
    }

    public Integer getCompletedTrips() {
        return completedTrips;
    }

    public void setCompletedTrips(Integer completedTrips) {
        this.completedTrips = completedTrips;
    }

    public Integer getCancelledTrips() {
        return cancelledTrips;
    }

    public void setCancelledTrips(Integer cancelledTrips) {
        this.cancelledTrips = cancelledTrips;
    }

    public Double getAverageFare() {
        return averageFare;
    }

    public void setAverageFare(Double averageFare) {
        this.averageFare = averageFare;
    }

    public Double getReliabilityScore() {
        return reliabilityScore;
    }

    public void setReliabilityScore(Double reliabilityScore) {
        this.reliabilityScore = reliabilityScore;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }

    public Integer getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(Integer totalRatings) {
        this.totalRatings = totalRatings;
    }
}

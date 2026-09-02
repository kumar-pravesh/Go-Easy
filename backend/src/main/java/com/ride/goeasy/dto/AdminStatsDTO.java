package com.ride.goeasy.dto;

public class AdminStatsDTO {
    private Long totalDrivers;
    private Long totalCustomers;
    private Long totalBookings;
    private Double totalRevenue;
    private Long activeRides;
    private Long sosAlerts;

    public AdminStatsDTO() {}

    public AdminStatsDTO(Long totalDrivers, Long totalCustomers, Long totalBookings, Double totalRevenue, Long activeRides, Long sosAlerts) {
        this.totalDrivers = totalDrivers;
        this.totalCustomers = totalCustomers;
        this.totalBookings = totalBookings;
        this.totalRevenue = totalRevenue;
        this.activeRides = activeRides;
        this.sosAlerts = sosAlerts;
    }

    public Long getTotalDrivers() { return totalDrivers; }
    public void setTotalDrivers(Long totalDrivers) { this.totalDrivers = totalDrivers; }

    public Long getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }

    public Long getTotalBookings() { return totalBookings; }
    public void setTotalBookings(Long totalBookings) { this.totalBookings = totalBookings; }

    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }

    public Long getActiveRides() { return activeRides; }
    public void setActiveRides(Long activeRides) { this.activeRides = activeRides; }

    public Long getSosAlerts() { return sosAlerts; }
    public void setSosAlerts(Long sosAlerts) { this.sosAlerts = sosAlerts; }
}

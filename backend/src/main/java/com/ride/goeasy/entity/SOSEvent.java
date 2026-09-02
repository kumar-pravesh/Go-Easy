package com.ride.goeasy.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class SOSEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    private Double latitude;
    private Double longitude;

    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, RESOLVED

    public SOSEvent() {}

    public SOSEvent(Booking booking, Double latitude, Double longitude) {
        this.booking = booking;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = LocalDateTime.now();
        this.status = "ACTIVE";
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }
    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
}

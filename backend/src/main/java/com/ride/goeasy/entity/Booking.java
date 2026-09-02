package com.ride.goeasy.entity;

import com.ride.goeasy.enums.BookingStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
public class Booking {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	// MANY BOOKINGS → ONE CUSTOMER
	@ManyToOne
	@com.fasterxml.jackson.annotation.JsonIgnoreProperties({"bookings", "userr", "password"})
	private Customer customer;

	// MANY BOOKINGS → ONE Vehicle
	@ManyToOne
	private Vehicle vehicle;

	@ManyToOne
	@jakarta.persistence.JoinColumn(name = "driver_id")
	@com.fasterxml.jackson.annotation.JsonIgnoreProperties("dblist")
	private Driver driver;

	private String sourceLocation;
	private String destinationLocation;

	private Double distance;
	private Double fare;
	private String estimatedTime;

	private Double baseFare = 0.0;
	private Double distanceFare = 0.0;
	private Double penaltyAmount = 0.0;
	private Double waitingCharge = 0.0;
	private Double nightCharge = 0.0;
	private Double platformFee = 0.0;
	private Double tax = 0.0;
	private Double discount = 0.0;
	private Double pricePerKm = 0.0;
	@jakarta.persistence.Column(name = "fare_locked", columnDefinition = "boolean DEFAULT false")
	private boolean fareLocked = false;

	private String startOtp; // before ride
	private String endOtp;   // after ride

	private boolean startOtpVerified;
	private boolean endOtpVerified;

	@Enumerated(EnumType.STRING)
	private BookingStatus bookingStatus;

	private boolean activeBookingFlag;

	@OneToOne(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
	@com.fasterxml.jackson.annotation.JsonIgnore
	private Payment payment;

	private String paymentMode; // CASH or UPI

	private LocalDate rideDate;
	private LocalDateTime rideStartedAt; // when driver began the ride (OTP verified)

	// Recording consent — NONE | REQUESTED | ACCEPTED | ACTIVE | REJECTED | COMPLETED
	@jakarta.persistence.Column(columnDefinition = "VARCHAR(20)")
	private String recordingConsent = "NONE";
	private LocalDateTime recordingRequestedAt;

	// Scheduled ride fields
	@jakarta.persistence.Column(columnDefinition = "boolean DEFAULT false")
	private boolean scheduled = false;
	private LocalDateTime scheduledTime;
	@jakarta.persistence.Column(columnDefinition = "boolean DEFAULT false")
	private boolean fareLockedAtBooking = false;
	@jakarta.persistence.Column(columnDefinition = "boolean DEFAULT false")
	private boolean scheduledNotifSent = false;

	// Cancellation tracking
	@jakarta.persistence.Column(length = 500)
	private String cancelReason;
	@jakarta.persistence.Column(columnDefinition = "VARCHAR(20)")
	private String cancelledBy; // CUSTOMER | DRIVER

	// Ratings (1-5 stars)
	private Integer driverRating;   // customer's rating OF the driver
	private Integer customerRating; // driver's rating OF the customer
	private String promoCodeUsed;   // promo code applied at booking

	// ──────────── Getters & Setters ────────────

	public String getStartOtp() { return startOtp; }
	public void setStartOtp(String startOtp) { this.startOtp = startOtp; }

	public String getEndOtp() { return endOtp; }
	public void setEndOtp(String endOtp) { this.endOtp = endOtp; }

	public boolean isStartOtpVerified() { return startOtpVerified; }
	public void setStartOtpVerified(boolean startOtpVerified) { this.startOtpVerified = startOtpVerified; }

	public boolean isEndOtpVerified() { return endOtpVerified; }
	public void setEndOtpVerified(boolean endOtpVerified) { this.endOtpVerified = endOtpVerified; }

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public Customer getCustomer() { return customer; }
	public void setCustomer(Customer customer) { this.customer = customer; }

	public Vehicle getVehicle() { return vehicle; }
	public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

	public String getSourceLocation() { return sourceLocation; }
	public void setSourceLocation(String sourceLocation) { this.sourceLocation = sourceLocation; }

	public String getDestinationLocation() { return destinationLocation; }
	public void setDestinationLocation(String destinationLocation) { this.destinationLocation = destinationLocation; }

	public Double getDistance() { return distance; }
	public void setDistance(Double distance) { this.distance = distance; }

	public Double getFare() { return fare; }
	public void setFare(Double fare) { this.fare = fare; }

	public Double getBaseFare() { return baseFare; }
	public void setBaseFare(Double baseFare) { this.baseFare = baseFare; }

	public Double getDistanceFare() { return distanceFare; }
	public void setDistanceFare(Double distanceFare) { this.distanceFare = distanceFare; }

	public Double getPenaltyAmount() { return penaltyAmount; }
	public void setPenaltyAmount(Double penaltyAmount) { this.penaltyAmount = penaltyAmount; }

	public Double getWaitingCharge() { return waitingCharge; }
	public void setWaitingCharge(Double waitingCharge) { this.waitingCharge = waitingCharge; }

	public Double getNightCharge() { return nightCharge; }
	public void setNightCharge(Double nightCharge) { this.nightCharge = nightCharge; }

	public Double getPlatformFee() { return platformFee; }
	public void setPlatformFee(Double platformFee) { this.platformFee = platformFee; }

	public Double getTax() { return tax; }
	public void setTax(Double tax) { this.tax = tax; }

	public Double getDiscount() { return discount; }
	public void setDiscount(Double discount) { this.discount = discount; }

	public Double getPricePerKm() { return pricePerKm; }
	public void setPricePerKm(Double pricePerKm) { this.pricePerKm = pricePerKm; }

	public boolean isFareLocked() { return fareLocked; }
	public void setFareLocked(boolean fareLocked) { this.fareLocked = fareLocked; }

	public String getEstimatedTime() { return estimatedTime; }
	public void setEstimatedTime(String estimatedTime) { this.estimatedTime = estimatedTime; }

	public BookingStatus getBookingStatus() { return bookingStatus; }
	public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

	public boolean isActiveBookingFlag() { return activeBookingFlag; }
	public void setActiveBookingFlag(boolean activeBookingFlag) { this.activeBookingFlag = activeBookingFlag; }

	public Payment getPayment() { return payment; }
	public void setPayment(Payment payment) { this.payment = payment; }

	public String getPaymentMode() { return paymentMode; }
	public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }

	public LocalDate getRideDate() { return rideDate; }
	public void setRideDate(LocalDate rideDate) { this.rideDate = rideDate; }

	public LocalDateTime getRideStartedAt() { return rideStartedAt; }
	public void setRideStartedAt(LocalDateTime rideStartedAt) { this.rideStartedAt = rideStartedAt; }

	public String getRecordingConsent() { return recordingConsent; }
	public void setRecordingConsent(String recordingConsent) { this.recordingConsent = recordingConsent; }

	public LocalDateTime getRecordingRequestedAt() { return recordingRequestedAt; }
	public void setRecordingRequestedAt(LocalDateTime recordingRequestedAt) { this.recordingRequestedAt = recordingRequestedAt; }

	public boolean isScheduled() { return scheduled; }
	public void setScheduled(boolean scheduled) { this.scheduled = scheduled; }

	public LocalDateTime getScheduledTime() { return scheduledTime; }
	public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

	public boolean isFareLockedAtBooking() { return fareLockedAtBooking; }
	public void setFareLockedAtBooking(boolean fareLockedAtBooking) { this.fareLockedAtBooking = fareLockedAtBooking; }

	public boolean isScheduledNotifSent() { return scheduledNotifSent; }
	public void setScheduledNotifSent(boolean scheduledNotifSent) { this.scheduledNotifSent = scheduledNotifSent; }

	public String getCancelReason() { return cancelReason; }
	public void setCancelReason(String cancelReason) { this.cancelReason = cancelReason; }

	public String getCancelledBy() { return cancelledBy; }
	public void setCancelledBy(String cancelledBy) { this.cancelledBy = cancelledBy; }

	public Integer getDriverRating() { return driverRating; }
	public void setDriverRating(Integer driverRating) { this.driverRating = driverRating; }

	public Integer getCustomerRating() { return customerRating; }
	public void setCustomerRating(Integer customerRating) { this.customerRating = customerRating; }

	public String getPromoCodeUsed() { return promoCodeUsed; }
	public void setPromoCodeUsed(String promoCodeUsed) { this.promoCodeUsed = promoCodeUsed; }

	public Booking() {
		super();
	}
}

package com.ride.goeasy.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Customer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private String name;
	private int age;
	private String gender;
	private String password;

	@Column(unique = true)
	private Long mobno;

	private String email;
    
	// store as string "lat,lon"
	private String currentLocation;
	//one customer-->one userr
	@OneToOne
	private Userr userr;
	// One Customer -> Many Bookings
	@OneToMany
	 
	 @JsonIgnore
	private List<Booking> bookings;
	
	private  boolean activeBookingFlag;
    private int cancellationCount;
   
    
	
	

 
	public int getCancellationCount() {
		return cancellationCount;
	}

	public void setCancellationCount(int cancellationCount) {
		this.cancellationCount = cancellationCount;
	}

	public boolean isActiveBookingFlag() {
		return activeBookingFlag;
	}

	public void setActiveBookingFlag(boolean activeBookingFlag) {
		this.activeBookingFlag = activeBookingFlag;
	}

	public Customer() {
	}
	
	

	public Customer(String name, int age, String gender, String password, Long mobno, String email,
			String currentLocation, Userr userr, List<Booking> bookings, boolean activeBookingFlag,
			int cancellationCount) {
		super();
		this.name = name;
		this.age = age;
		this.gender = gender;
		this.password = password;
		this.mobno = mobno;
		this.email = email;
		this.currentLocation = currentLocation;
		this.userr = userr;
		this.bookings = bookings;
		this.activeBookingFlag = activeBookingFlag;
		this.cancellationCount = cancellationCount;
	}

	// getters & setters
	
	
	
	
	public int getId() {
		return id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Userr getUserr() {
		return userr;
	}

	public void setUserr(Userr userr) {
		this.userr = userr;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Long getMobno() {
		return mobno;
	}

	public void setMobno(Long mobno) {
		this.mobno = mobno;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getCurrentLocation() {
		return currentLocation;
	}

	public void setCurrentLocation(String currentLocation) {
		this.currentLocation = currentLocation;
	}

	public List<Booking> getBookings() {
		return bookings;
	}

	public void setBookings(List<Booking> bookings) {
		this.bookings = bookings;
	}
}

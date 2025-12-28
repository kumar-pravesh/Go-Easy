package com.ride.goeasy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Userr {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(nullable = false)
	private long mobNo;
	 @Column(nullable = false)
	private String password;
	private String role;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getMobNo() {
	    return mobNo; 
	}

	public void setMobno(long mobNo) {
	    this.mobNo = mobNo; 
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Userr(long mobNo, String password, String role) {
		super();
		this.mobNo = mobNo;
		this.password = password;
		this.role = role;
	}

	public Userr() {
		super();
	}



}

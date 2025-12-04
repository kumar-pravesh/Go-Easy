package com.ride.goeasy.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

@Entity
public class Driver {
   private static int count = 101;
   @Id
   private int id;
   private int licNo;
   private String upiId;
   private String dname;
   private String dstatus;
   private int age;
   private long mobNo;
   private String gender;
   private String mailId;
   
   @OneToOne
   private Vehicle vehicle;

   public static int getCount() {
	return count;
   }

   public static void setCount(int count) {
	Driver.count = count;
   }

   public int getId() {
	return id;
   }

  

   public int getLicNo() {
	return licNo;
   }

   public void setLicNo(int licNo) {
	this.licNo = licNo;
   }

   public String getUpiId() {
	return upiId;
   }

   public void setUpiId(String upiId) {
	this.upiId = upiId;
   }

   public String getDname() {
	return dname;
   }

   public void setDname(String dname) {
	this.dname = dname;
   }

   public String getDstatus() {
	return dstatus;
   }

   public void setDstatus(String dstatus) {
	this.dstatus = dstatus;
   }

   public int getAge() {
	return age;
   }

   public void setAge(int age) {
	this.age = age;
   }

   public long getMobNo() {
	return mobNo;
   }

   public void setMobNo(long mobNo) {
	this.mobNo = mobNo;
   }

   public String getGender() {
	return gender;
   }

   public void setGender(String gender) {
	this.gender = gender;
   }

   public String getMailId() {
	return mailId;
   }

   public void setMailId(String mailId) {
	this.mailId = mailId;
   }

   public Vehicle getVehicle() {
	return vehicle;
   }

   public void setVehicle(Vehicle vehicle) {
	this.vehicle = vehicle;
   }

   public Driver(int id, int licNo, String upiId, String dname, String dstatus, int age, long mobNo, String gender,
		String mailId, Vehicle vehicle) {
	super();
	this.id = count++;
	this.licNo = licNo;
	this.upiId = upiId;
	this.dname = dname;
	this.dstatus = dstatus;
	this.age = age;
	this.mobNo = mobNo;
	this.gender = gender;
	this.mailId = mailId;
	this.vehicle = vehicle;
   }

   public Driver() {
	super();
	// TODO Auto-generated constructor stub
   }

   @Override
   public String toString() {
	return "Driver [id=" + id + ", licNo=" + licNo + ", upiId=" + upiId + ", dname=" + dname + ", dstatus=" + dstatus
			+ ", age=" + age + ", mobNo=" + mobNo + ", gender=" + gender + ", mailId=" + mailId + ", vehicle=" + vehicle
			+ "]";
   }

  
   
   
   
}

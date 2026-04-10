package com.ride.goeasy.dto;

public class CustomerDTO {
	 private String name;
	    private int age;
	    private String gender;
	    private Long mobno;
	    private String email;
	    private String password;
	    private double lat;   
	    private double lon;
	   
      
		public CustomerDTO() {
			super();
			// TODO Auto-generated constructor stub
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
		
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}
		public double getLat() {
			return lat;
		}
		public void setLat(double lat) {
			this.lat = lat;
		}
		public double getLon() {
			return lon;
		}
		public void setLon(double lon) {
			this.lon = lon;
		}  
	    
	    
	    
	    
}

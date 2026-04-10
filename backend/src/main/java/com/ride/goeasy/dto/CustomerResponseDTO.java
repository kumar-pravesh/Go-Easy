package com.ride.goeasy.dto;

public class CustomerResponseDTO {
	    private int id;
	    private String name;
	    private int age;
	    private String gender;
	    private Long mobno;
	    private String email;
	    private String currentLocation;
	    
	    
		public int getId() {
			return id;
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
	    
	    

}

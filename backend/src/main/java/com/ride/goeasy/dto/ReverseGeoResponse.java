package com.ride.goeasy.dto;

public class ReverseGeoResponse {
	private Address address;

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public static class Address {
		private String city;
		private String town;
		private String village;
		private String state;

		// Final city getter
		public String getCity() {
			if (city != null && !city.isBlank())
				return city;
			if (town != null && !town.isBlank())
				return town;
			if (village != null && !village.isBlank())
				return village;
			return state;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public void setTown(String town) {
			this.town = town;
		}

		public void setVillage(String village) {
			this.village = village;
		}

		public void setState(String state) {
			this.state = state;
		}
	}
}


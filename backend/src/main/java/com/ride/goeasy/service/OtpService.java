package com.ride.goeasy.service;

import org.springframework.stereotype.Service;

@Service
public class OtpService {

	  public String generateOtp() {
	        return String.valueOf(
	            (int)(Math.random() * 900000) + 100000
	        );
	    }
}

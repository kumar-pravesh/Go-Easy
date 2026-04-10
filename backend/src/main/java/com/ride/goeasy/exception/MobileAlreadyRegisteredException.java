package com.ride.goeasy.exception;

public class MobileAlreadyRegisteredException extends RuntimeException {

    public MobileAlreadyRegisteredException() {
        super("Mobile number is already registered");
    }
}
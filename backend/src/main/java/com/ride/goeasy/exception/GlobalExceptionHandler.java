package com.ride.goeasy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ride.goeasy.response.ResponseStructure;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
        DriverNotFoundException.class, 
        CustomerNotFoundException.class, 
        BookingNotFoundException.class, 
        VehicleNotFoundException.class,
        PaymentNotFoundException.class
    })
    public ResponseEntity<ResponseStructure<String>> handleNotFoundException(RuntimeException ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.NOT_FOUND.value());
        rs.setMessage(ex.getMessage());
        rs.setData(null);
        return new ResponseEntity<>(rs, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({
        InvalidLocationException.class,
        MobileAlreadyRegisteredException.class
    })
    public ResponseEntity<ResponseStructure<String>> handleBadRequestException(RuntimeException ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.BAD_REQUEST.value());
        rs.setMessage(ex.getMessage());
        rs.setData(null);
        return new ResponseEntity<>(rs, HttpStatus.BAD_REQUEST);
    }

    // Spring Security auth failures → 401, never 500
    @ExceptionHandler({
        AuthenticationException.class,
        BadCredentialsException.class,
        UsernameNotFoundException.class,
        DisabledException.class,
        LockedException.class
    })
    public ResponseEntity<ResponseStructure<String>> handleAuthException(RuntimeException ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        rs.setMessage("Invalid credentials. Please check your mobile number / email and password.");
        rs.setData(null);
        return new ResponseEntity<>(rs, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseStructure<String>> handleRuntimeException(RuntimeException ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        String msg = ex.getMessage() != null ? ex.getMessage() : "Unexpected error";

        // Business logic errors that are really bad-request, not server faults
        boolean isBadRequest = msg.contains("OTP")
                || msg.contains("completed")
                || msg.contains("missing")
                || msg.contains("not found")
                || msg.contains("Not Found")
                || msg.contains("already")
                || msg.contains("cannot")
                || msg.contains("Invalid")
                || msg.contains("must be");

        HttpStatus status = isBadRequest ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        rs.setStatusCode(status.value());
        rs.setMessage(msg);
        rs.setData(null);
        return new ResponseEntity<>(rs, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseStructure<String>> handleGenericException(Exception ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        rs.setMessage("Critical Server Error: " + ex.getMessage());
        rs.setData(null); 
        ex.printStackTrace(); 
        return new ResponseEntity<>(rs, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

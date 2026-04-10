package com.ride.goeasy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseStructure<String>> handleRuntimeException(RuntimeException ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        // If message suggests a business validation error, use 400
        boolean isValidationError = ex.getMessage() != null && (
            ex.getMessage().contains("OTP") || 
            ex.getMessage().contains("completed") ||
            ex.getMessage().contains("missing")
        );
        
        HttpStatus status = isValidationError ? HttpStatus.BAD_REQUEST : HttpStatus.INTERNAL_SERVER_ERROR;
        
        rs.setStatusCode(status.value());
        rs.setMessage(ex.getMessage());
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

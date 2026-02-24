package com.ride.goeasy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.ride.goeasy.response.ResponseStructure;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseStructure<String>> handleRuntimeException(RuntimeException ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        rs.setMessage("Error: " + ex.getMessage());
        rs.setData(null);
        return new ResponseEntity<>(rs, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseStructure<String>> handleGenericException(Exception ex) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        rs.setMessage("Critical Server Error: " + ex.getClass().getSimpleName() + " - " + ex.getMessage()); // Expose the real error
        rs.setData(null); 
        ex.printStackTrace(); // Keep server log for detailed trace
        return new ResponseEntity<>(rs, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

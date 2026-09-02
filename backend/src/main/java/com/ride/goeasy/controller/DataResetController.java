package com.ride.goeasy.controller;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.DataSeeder;
import com.ride.goeasy.response.ResponseStructure;

@RestController
@RequestMapping("/dev")
@ConditionalOnProperty(prefix = "app.db", name = "reset-token")
public class DataResetController {

    private final DataSeeder dataSeeder;
    private final String resetToken;

    public DataResetController(DataSeeder dataSeeder,
                               @org.springframework.beans.factory.annotation.Value("${app.db.reset-token}") String resetToken) {
        this.dataSeeder = dataSeeder;
        this.resetToken = resetToken;
    }

    @PostMapping("/reset-db")
    public ResponseEntity<ResponseStructure<String>> resetDb(@RequestHeader("X-Reset-Token") String token) {
        ResponseStructure<String> rs = new ResponseStructure<>();
        if (resetToken == null || resetToken.isBlank() || !resetToken.equals(token)) {
            rs.setStatusCode(HttpStatus.FORBIDDEN.value());
            rs.setMessage("Forbidden: invalid reset token.");
            rs.setData(null);
            return new ResponseEntity<>(rs, HttpStatus.FORBIDDEN);
        }

        dataSeeder.resetDatabase();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Database cleared and dummy data reseeded successfully.");
        rs.setData(null);
        return ResponseEntity.ok(rs);
    }
}

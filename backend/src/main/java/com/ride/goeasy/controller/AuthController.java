package com.ride.goeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.ride.goeasy.dto.LoginRequestDTO;
import com.ride.goeasy.dto.LoginResponseDTO;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Userr;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.UserrRepo;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.security.JwtUtils;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private UserrRepo userrRepo;

	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	private DriverRepo driverRepo;

    @Autowired
    private JwtUtils jwtUtils;
    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<ResponseStructure<LoginResponseDTO>> login(
            @RequestBody LoginRequestDTO dto) {

        String identifier = dto.getMobileNo();
        String finalMobNo = identifier;
        String userName = "User";

        // Check if identifier is an email
        if (identifier != null && identifier.contains("@")) {
            // Try to find as Customer first
            Customer customer = customerRepo.findByEmail(identifier).orElse(null);
            if (customer != null) {
                finalMobNo = String.valueOf(customer.getMobno());
                userName = customer.getName();
            } else {
                // Then try Driver
                Driver driver = driverRepo.findByMailId(identifier)
                        .orElseThrow(() -> new RuntimeException("User not found with email: " + identifier));
                finalMobNo = String.valueOf(driver.getMobNo());
                userName = driver.getDname();
            }
        } else if (identifier != null) {
            // Identifier is mobile number - get name for response
            try {
                Long mNo = Long.parseLong(identifier);
                Customer customer = customerRepo.findByMobno(mNo).orElse(null);
                if (customer != null) {
                    userName = customer.getName();
                } else {
                    Driver driver = driverRepo.findByMobNo(mNo).orElse(null);
                    if (driver != null) userName = driver.getDname();
                }
            } catch (NumberFormatException e) {
                // Ignore, let authenticationManager handle it
            }
        }

        // Authenticate using the mobile number (which is what Spring Security expects)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        finalMobNo,
                        dto.getPassword()
                )
        );

        Userr user = userrRepo.findByMobNo(Long.parseLong(finalMobNo))
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtils.generateToken(
        	    String.valueOf(user.getMobNo()),
        	    "ROLE_" + user.getRole()
        	);

        LoginResponseDTO loginResponse = new LoginResponseDTO(
                "Bearer " + token,
                user.getMobNo(),
                userName,
                user.getRole()
        );

        ResponseStructure<LoginResponseDTO> rs = new ResponseStructure<>();
        rs.setStatusCode(200);
        rs.setMessage("Login successful");
        rs.setData(loginResponse);

        return ResponseEntity.ok(rs);
    }
}
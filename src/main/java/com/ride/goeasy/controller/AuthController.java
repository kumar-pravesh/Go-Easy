package com.ride.goeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.ride.goeasy.dto.LoginRequestDTO;
import com.ride.goeasy.entity.Userr;
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
    private JwtUtils jwtUtils;
    // ================= LOGIN =================
    @PostMapping("/login")
    public ResponseEntity<ResponseStructure<String>> login(
            @RequestBody LoginRequestDTO dto) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        String.valueOf(dto.getMobileNo()),
                        dto.getPassword()
                )
        );

        Userr user = userrRepo.findByMobNo(dto.getMobileNo())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtils.generateToken(
        	    String.valueOf(user.getMobNo()),
        	    "ROLE_" + user.getRole()  // must include ROLE_ prefix
        	);

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(200);
        rs.setMessage("Login successful");
        rs.setData("Bearer " + token);

        return ResponseEntity.ok(rs);
    }
}
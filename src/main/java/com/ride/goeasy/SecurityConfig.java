package com.ride.goeasy;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// 🔐 Password Encoder Bean
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	 @Bean
	    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

	        http
	            // Disable CSRF for REST APIs
	            .csrf(csrf -> csrf.disable())

	            // State less session (JWT / REST)
	            .sessionManagement(session -> 
	                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	            )

	            // Authorization rules
	            .authorizeHttpRequests(auth -> auth
	                // ✅ Permit register API
		                .requestMatchers("/driver/**").permitAll()

		                .requestMatchers("/customer/**").permitAll()

	                // (Optional) permit login
	                .requestMatchers("/auth/login").permitAll()

	                // Any other request must be authenticated
	                .anyRequest().authenticated()
	            );

	        return http.build();
	    }
	 
}


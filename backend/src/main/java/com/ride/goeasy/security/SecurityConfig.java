package com.ride.goeasy.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	

	@Autowired
    private JwtFilter jwtFilter;
	
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
    private PasswordEncoder passwordEncoder;



	    public SecurityConfig(JwtFilter jwtFilter) {
	        this.jwtFilter = jwtFilter;
	    }
	
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable()) 
	        .cors(cors -> cors.configurationSource(request -> {
	            var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
	            corsConfiguration.setAllowedOriginPatterns(java.util.List.of(
	                "http://localhost:5173", 
	                "http://localhost:5174",
	                "https://*.vercel.app"
	            )); 
	            corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	            corsConfiguration.setAllowedHeaders(java.util.List.of("*"));
	            return corsConfiguration;
	        })) 
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .authorizeHttpRequests(auth -> auth
	        	    // 🔓 PUBLIC
	                .requestMatchers(
	                    "/auth/**",
	                    "/customer/register/**",
	                    "/registercustomerwithpwd",
	                    "/newtoken",
	                    "/driver/save",
	                    "/driver/generateUpiQr/**"
	                ).permitAll()

	                // 👤 CUSTOMER & DRIVER SHARED
	                .requestMatchers(
	                    "/customer/**",
	                    "/availableVehicles" // Vehicle search endpoint
	                ).hasRole("CUSTOMER")
	                
	                .requestMatchers(
	                    "/booking/**"
	                ).authenticated() // Both Drivers and Customers need access to booking endpoints

	                // 🚖 DRIVER
	                .requestMatchers("/driver/**")
	                .hasRole("DRIVER")

	                // 🔐 DEFAULT
	                .anyRequest().authenticated()
	            )
	       
	        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
	
	
	
}

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
	                "https://*.vercel.app",
	                "https://go-easy-woad.vercel.app"
	            )); 
	            corsConfiguration.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
	            corsConfiguration.setAllowedHeaders(java.util.List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
	            corsConfiguration.setExposedHeaders(java.util.List.of("Authorization"));
	            corsConfiguration.setAllowCredentials(true);
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
                    "/corporate/**",
	                    "/driver/generateUpiQr/**"
	                ).permitAll()

	                // 👤 CUSTOMER & DRIVER SHARED
	                .requestMatchers(
	                    "/customer/**",
	                    "/availableVehicles" // Vehicle search endpoint
	                ).hasRole("CUSTOMER")
	                
	                .requestMatchers(
	                    "/booking/**",
                    "/driver/rate"
	                ).authenticated() // Customers and drivers both need access

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

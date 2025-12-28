package com.ride.goeasy.security;
import com.ride.goeasy.entity.Userr;
import com.ride.goeasy.repository.UserrRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Autowired
    private UserrRepo userrRepo;

    @Override
    public UserDetails loadUserByUsername(String mobileNo) throws UsernameNotFoundException {
        
        Userr user = userrRepo.findByMobNo(Long.parseLong(mobileNo))
                .orElseThrow(() -> new UsernameNotFoundException("User not found with mobile: " + mobileNo));

        // Spring Security ka User object return kr rhe h
        return User.builder()
                .username(String.valueOf(user.getMobNo()))
                .password(user.getPassword()) 
                .authorities(user.getRole()) 
                .build();
    }

	
}

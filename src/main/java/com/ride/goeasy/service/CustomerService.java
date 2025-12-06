package com.ride.goeasy.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.CustomerDTO;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.repository.CustomerRepo;

@Service
public class CustomerService {
	
	   @Autowired
	    private CustomerRepo customerRepo;

	   
//	   Register customer method
	    public Customer saveCustomer(CustomerDTO dto) {

	        Customer c = new Customer();
	        c.setName(dto.getName());
	        c.setAge(dto.getAge());
	        c.setGender(dto.getGender());
	        c.setMobno(dto.getMobno());
	        c.setEmail(dto.getEmail());

	        // store "lat,lon" format
	        String loc = dto.getLat() + "," + dto.getLon();
	        c.setCurrentLocation(loc);

	        return customerRepo.save(c);
	    }
	    
	    
	    
	    
	    // Find customer by mobile number
	    public Optional<Customer> findCustomerByMobNo(Long mobno) {
	        return customerRepo.findByMobno(mobno);
	    }

	    // Delete customer by mobile number
	    public String deleteCustomerByMobNo(Long mobno) {
	        Optional<Customer> customer = customerRepo.findByMobno(mobno);
	        if (customer.isPresent()) {
	            customerRepo.delete(customer.get());
	            return "Customer with mobile " + mobno + " deleted successfully";
	        } else {
	            return "Customer with mobile " + mobno + " not found";
	        }
	    }

}

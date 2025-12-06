package com.ride.goeasy.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.CustomerDTO;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.service.CustomerService;

@RestController
@RequestMapping("/customer")
public class CustomerController {

	    @Autowired
	    private CustomerService customerService;

	    
//	    Register customer method
	    @PostMapping("/registerCustomer")
	    public Customer addCustomer(@RequestBody CustomerDTO dto) {
	        return customerService.saveCustomer(dto);
	    }
	    
	    
	    
	    // Find customer by mobile number
	    @GetMapping("/find")
	    public Optional<Customer> getCustomerByMobNo(@RequestParam Long mobno) {
	        return customerService.findCustomerByMobNo(mobno);
	    }

	    // Delete customer by mobile number
	    @DeleteMapping("/delete")
	    public String deleteCustomerByMobNo(@RequestParam Long mobno) {
	        return customerService.deleteCustomerByMobNo(mobno);
	    }
	    
	    
}

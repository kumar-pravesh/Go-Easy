package com.ride.goeasy.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ride.goeasy.dto.TrustedContactDTO;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.TrustedContact;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.TrustedContactRepository;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class TrustedContactService {

    private final TrustedContactRepository trustedContactRepo;
    private final CustomerRepo customerRepo;

    public TrustedContactService(TrustedContactRepository trustedContactRepo, CustomerRepo customerRepo) {
        this.trustedContactRepo = trustedContactRepo;
        this.customerRepo = customerRepo;
    }

    public ResponseStructure<TrustedContactDTO> addContact(TrustedContactDTO dto) {
        Customer customer = customerRepo.findByMobno(dto.getCustomerMobNo())
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        // Max 5 contacts per customer logic
        List<TrustedContact> existingContacts = trustedContactRepo.findByCustomer(customer);
        if (existingContacts.size() >= 5) {
            throw new RuntimeException("Maximum of 5 trusted contacts allowed.");
        }

        TrustedContact contact = new TrustedContact(dto.getName(), dto.getPhoneNumber(), dto.getEmail(), customer);
        TrustedContact savedContact = trustedContactRepo.save(contact);

        TrustedContactDTO responseDto = new TrustedContactDTO(
                savedContact.getId(),
                savedContact.getName(),
                savedContact.getPhoneNumber(),
                savedContact.getEmail(),
                customer.getMobno()
        );

        ResponseStructure<TrustedContactDTO> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.CREATED.value());
        rs.setMessage("Trusted contact added successfully");
        rs.setData(responseDto);

        return rs;
    }

    public ResponseStructure<List<TrustedContactDTO>> getContacts(long customerMobNo) {
        Customer customer = customerRepo.findByMobno(customerMobNo)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        List<TrustedContact> contacts = trustedContactRepo.findByCustomer(customer);
        List<TrustedContactDTO> dtos = contacts.stream().map(c -> 
            new TrustedContactDTO(c.getId(), c.getName(), c.getPhoneNumber(), c.getEmail(), customer.getMobno())
        ).collect(Collectors.toList());

        ResponseStructure<List<TrustedContactDTO>> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Trusted contacts fetched");
        rs.setData(dtos);

        return rs;
    }

    public ResponseStructure<String> deleteContact(int contactId, long customerMobNo) {
        TrustedContact contact = trustedContactRepo.findById(contactId)
                .orElseThrow(() -> new RuntimeException("Contact not found"));

        if (contact.getCustomer().getMobno() != customerMobNo) {
            throw new RuntimeException("Unauthorized to delete this contact");
        }

        trustedContactRepo.delete(contact);

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Trusted contact deleted successfully");
        rs.setData(null);

        return rs;
    }
}

package com.ride.goeasy.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.TrustedContactDTO;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.TrustedContactService;

@RestController
@RequestMapping("/api/contacts")
public class TrustedContactController {

    private final TrustedContactService trustedContactService;

    public TrustedContactController(TrustedContactService trustedContactService) {
        this.trustedContactService = trustedContactService;
    }

    @PostMapping
    public ResponseStructure<TrustedContactDTO> addContact(@RequestBody TrustedContactDTO dto) {
        return trustedContactService.addContact(dto);
    }

    @GetMapping
    public ResponseStructure<List<TrustedContactDTO>> getContacts(@RequestParam long customerMobNo) {
        return trustedContactService.getContacts(customerMobNo);
    }

    @DeleteMapping
    public ResponseStructure<String> deleteContact(@RequestParam int contactId, @RequestParam long customerMobNo) {
        return trustedContactService.deleteContact(contactId, customerMobNo);
    }
}

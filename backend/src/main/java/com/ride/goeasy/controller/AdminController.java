package com.ride.goeasy.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.dto.AdminStatsDTO;
import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.PromoCode;
import com.ride.goeasy.entity.SOSEvent;
import com.ride.goeasy.repository.PromoCodeRepo;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.AdminService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PromoCodeRepo promoCodeRepo;

    @GetMapping("/stats")
    public ResponseStructure<AdminStatsDTO> getPlatformStats() {
        return adminService.getPlatformStats();
    }

    @GetMapping("/drivers")
    public ResponseStructure<List<Driver>> getAllDrivers() {
        return adminService.getAllDrivers();
    }

    @GetMapping("/bookings")
    public ResponseStructure<List<Booking>> getAllBookings() {
        return adminService.getAllBookings();
    }

    @GetMapping("/sos")
    public ResponseStructure<List<SOSEvent>> getAllSOSEvents() {
        return adminService.getAllSOSEvents();
    }

    @PostMapping("/blockDriver")
    public ResponseStructure<Driver> blockDriver(@RequestParam Integer driverId) {
        return adminService.blockDriver(driverId, true);
    }

    @PostMapping("/unblockDriver")
    public ResponseStructure<Driver> unblockDriver(@RequestParam Integer driverId) {
        return adminService.blockDriver(driverId, false);
    }

    // ─── Promo Codes ────────────────────────────────────────────────────
    @GetMapping("/promos")
    public ResponseStructure<List<PromoCode>> getAllPromos() {
        List<PromoCode> promos = promoCodeRepo.findAll();
        ResponseStructure<List<PromoCode>> rs = new ResponseStructure<>();
        rs.setStatusCode(200);
        rs.setMessage("Promos fetched");
        rs.setData(promos);
        return rs;
    }

    @PostMapping("/promos/create")
    public ResponseStructure<PromoCode> createPromo(@org.springframework.web.bind.annotation.RequestBody PromoCode promo) {
        if (promoCodeRepo.existsByCodeIgnoreCase(promo.getCode())) {
            throw new RuntimeException("Promo code already exists: " + promo.getCode());
        }
        PromoCode saved = promoCodeRepo.save(promo);
        ResponseStructure<PromoCode> rs = new ResponseStructure<>();
        rs.setStatusCode(201);
        rs.setMessage("Promo code created");
        rs.setData(saved);
        return rs;
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/promos/{id}")
    public ResponseStructure<String> deactivatePromo(@org.springframework.web.bind.annotation.PathVariable Integer id) {
        PromoCode promo = promoCodeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Promo not found"));
        promo.setActive(false);
        promoCodeRepo.save(promo);
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(200);
        rs.setMessage("Promo deactivated");
        rs.setData(promo.getCode());
        return rs;
    }
}

package com.ride.goeasy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ride.goeasy.dto.CorporateLoginRequest;

import com.ride.goeasy.entity.Company;
import com.ride.goeasy.entity.CorporateWallet;
import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.CompanyService;

@RestController
@RequestMapping("/corporate")
public class CompanyController {

    @Autowired private CompanyService companyService;

    @PostMapping("/login")
    public ResponseStructure<Company> login(@RequestBody CorporateLoginRequest req) {
        return companyService.loginCompany(req.getEmail(), req.getPassword());
    }

    @PostMapping("/register")
    public ResponseStructure<Company> register(@RequestBody Company company) {
        return companyService.registerCompany(company);
    }

    @PostMapping("/addEmployee")
    public ResponseStructure<CorporateWallet> addEmployee(
            @RequestParam Integer companyId,
            @RequestParam Long customerMobno) {
        return companyService.addEmployee(companyId, customerMobno);
    }

    @PostMapping("/topUp")
    public ResponseStructure<Company> topUp(
            @RequestParam Integer companyId,
            @RequestParam Double amount) {
        return companyService.topUpWallet(companyId, amount);
    }

    @GetMapping("/dashboard")
    public ResponseStructure<Map<String, Object>> dashboard(@RequestParam Integer companyId) {
        return companyService.getDashboard(companyId);
    }

    @GetMapping("/employeeWallet")
    public ResponseStructure<Map<String, Object>> employeeWallet(@RequestParam Long mobNo) {
        return companyService.getEmployeeWallet(mobNo);
    }

    @PostMapping("/removeEmployee")
    public ResponseStructure<String> removeEmployee(
            @RequestParam Integer companyId,
            @RequestParam Long customerMobno) {
        return companyService.removeEmployee(companyId, customerMobno);
    }
}

package com.ride.goeasy.service;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.ride.goeasy.entity.Company;
import com.ride.goeasy.entity.CorporateWallet;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.exception.CustomerNotFoundException;
import com.ride.goeasy.repository.CompanyRepo;
import com.ride.goeasy.repository.CorporateWalletRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class CompanyService {

    @Autowired private CompanyRepo companyRepo;
    @Autowired private CorporateWalletRepo walletRepo;
    @Autowired private CustomerRepo customerRepo;
    @Autowired private MailService mailService;

    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    // ── Login a company ──────────────────────────────────────────────
    public ResponseStructure<Company> loginCompany(String email, String password) {
        Company company = companyRepo.findByCompanyEmail(email)
                .orElseThrow(() -> new RuntimeException("No company found with this email."));
        if (!company.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password.");
        }
        if (!company.isActive()) {
            throw new RuntimeException("This company account has been deactivated.");
        }
        ResponseStructure<Company> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Login successful");
        rs.setData(company);
        return rs;
    }

    // ── Register a company ───────────────────────────────────────────
    public ResponseStructure<Company> registerCompany(Company company) {
        if (companyRepo.existsByCompanyEmail(company.getCompanyEmail())) {
            throw new RuntimeException("Company with this email already exists.");
        }
        company.setActive(true);
        Company saved = companyRepo.save(company);

        try {
            mailService.sendMail(
                saved.getCompanyEmail(),
                "Welcome to GoEasy Corporate — " + saved.getCompanyName(),
                "Hello " + saved.getContactPerson() + ",\n\n"
              + "Your corporate account has been created.\n"
              + "Monthly budget per employee: ₹" + saved.getMonthlyBudgetPerEmployee() + "\n"
              + "Top up your wallet to start covering employee rides.\n\n"
              + "– GoEasy Team"
            );
        } catch (Exception ignored) {}

        ResponseStructure<Company> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.CREATED.value());
        rs.setMessage("Company registered successfully");
        rs.setData(saved);
        return rs;
    }

    // ── Add an employee to a company ────────────────────────────────
    public ResponseStructure<CorporateWallet> addEmployee(Integer companyId, Long customerMobno) {
        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        Customer customer = customerRepo.findByMobno(customerMobno)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerMobno));

        if (walletRepo.existsByCustomerAndActiveTrue(customer)) {
            throw new RuntimeException("Customer is already linked to a corporate account.");
        }

        CorporateWallet wallet = new CorporateWallet();
        wallet.setCompany(company);
        wallet.setCustomer(customer);
        wallet.setMonthlyBudget(company.getMonthlyBudgetPerEmployee());
        wallet.setUsedThisMonth(0.0);
        wallet.setBudgetMonth(YearMonth.now().format(MONTH_FMT));
        wallet.setActive(true);
        CorporateWallet saved = walletRepo.save(wallet);

        try {
            mailService.sendMail(
                customer.getEmail(),
                "Corporate Ride Plan Activated — GoEasy",
                "Hello " + customer.getName() + ",\n\n"
              + "You've been added to " + company.getCompanyName() + "'s corporate ride plan.\n"
              + "Monthly ride budget: ₹" + company.getMonthlyBudgetPerEmployee() + "\n"
              + "Select 'Use Corporate Wallet' when booking.\n\n"
              + "– GoEasy Team"
            );
        } catch (Exception ignored) {}

        ResponseStructure<CorporateWallet> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.CREATED.value());
        rs.setMessage("Employee added to corporate plan");
        rs.setData(saved);
        return rs;
    }

    // ── Top-up company wallet ────────────────────────────────────────
    public ResponseStructure<Company> topUpWallet(Integer companyId, Double amount) {
        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        company.setWalletBalance(company.getWalletBalance() + amount);
        companyRepo.save(company);

        ResponseStructure<Company> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Wallet topped up. New balance: ₹" + company.getWalletBalance());
        rs.setData(company);
        return rs;
    }

    // ── Company dashboard — wallet + all employee usage ─────────────
    public ResponseStructure<Map<String, Object>> getDashboard(Integer companyId) {
        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found: " + companyId));
        List<CorporateWallet> wallets = walletRepo.findByCompanyAndActiveTrue(company);

        double totalSpentThisMonth = wallets.stream()
                .mapToDouble(CorporateWallet::getUsedThisMonth).sum();

        Map<String, Object> data = new HashMap<>();
        data.put("company", company);
        data.put("walletBalance", company.getWalletBalance());
        data.put("totalEmployees", wallets.size());
        data.put("totalSpentThisMonth", totalSpentThisMonth);
        data.put("employees", wallets.stream().map(w -> Map.of(
            "name",        w.getCustomer().getName(),
            "mobile",      w.getCustomer().getMobno(),
            "budget",      w.getMonthlyBudget(),
            "used",        w.getUsedThisMonth(),
            "remaining",   w.getRemainingBudget()
        )).toList());

        ResponseStructure<Map<String, Object>> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Dashboard loaded");
        rs.setData(data);
        return rs;
    }

    // ── Deduct fare from corporate wallet (called from BookingService) ─
    public void deductCorporateFare(Customer customer, double fare) {
        walletRepo.findByCustomerAndActiveTrue(customer).ifPresent(wallet -> {
            String thisMonth = YearMonth.now().format(MONTH_FMT);
            // Reset if new month
            if (!thisMonth.equals(wallet.getBudgetMonth())) {
                wallet.setUsedThisMonth(0.0);
                wallet.setBudgetMonth(thisMonth);
            }
            double deduct = Math.min(fare, wallet.getRemainingBudget());
            if (deduct <= 0) return;

            wallet.setUsedThisMonth(wallet.getUsedThisMonth() + deduct);
            Company company = wallet.getCompany();
            company.setWalletBalance(Math.max(0, company.getWalletBalance() - deduct));
            walletRepo.save(wallet);
            companyRepo.save(company);
        });
    }

    // ── Get wallet info for a customer ──────────────────────────────
    public ResponseStructure<Map<String, Object>> getEmployeeWallet(Long mobno) {
        Customer customer = customerRepo.findByMobno(mobno)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
        return walletRepo.findByCustomerAndActiveTrue(customer)
            .map(w -> {
                String thisMonth = YearMonth.now().format(MONTH_FMT);
                if (!thisMonth.equals(w.getBudgetMonth())) {
                    w.setUsedThisMonth(0.0);
                    w.setBudgetMonth(thisMonth);
                    walletRepo.save(w);
                }
                Map<String, Object> data = Map.of(
                    "companyName",    w.getCompany().getCompanyName(),
                    "monthlyBudget",  w.getMonthlyBudget(),
                    "used",           w.getUsedThisMonth(),
                    "remaining",      w.getRemainingBudget(),
                    "month",          thisMonth
                );
                ResponseStructure<Map<String, Object>> rs = new ResponseStructure<>();
                rs.setStatusCode(HttpStatus.OK.value());
                rs.setMessage("Corporate wallet loaded");
                rs.setData(data);
                return rs;
            })
            .orElseGet(() -> {
                ResponseStructure<Map<String, Object>> rs = new ResponseStructure<>();
                rs.setStatusCode(HttpStatus.OK.value());
                rs.setMessage("No corporate plan linked");
                rs.setData(null);
                return rs;
            });
    }

    // ── Monthly reset at midnight on 1st of each month ──────────────
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlyUsage() {
        String newMonth = YearMonth.now().format(MONTH_FMT);
        walletRepo.findAll().forEach(w -> {
            w.setUsedThisMonth(0.0);
            w.setBudgetMonth(newMonth);
            walletRepo.save(w);
        });
        System.out.println("Corporate wallet monthly usage reset for " + newMonth);
    }

    // ── Remove employee from corporate plan ─────────────────────────
    public ResponseStructure<String> removeEmployee(Integer companyId, Long customerMobno) {
        Company company = companyRepo.findById(companyId)
                .orElseThrow(() -> new RuntimeException("Company not found"));
        Customer customer = customerRepo.findByMobno(customerMobno)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        CorporateWallet wallet = walletRepo.findByCustomerAndActiveTrue(customer)
                .orElseThrow(() -> new RuntimeException("Employee not linked to any active corporate plan"));

        if (!wallet.getCompany().getId().equals(company.getId())) {
            throw new RuntimeException("Employee does not belong to this company");
        }

        wallet.setActive(false);
        walletRepo.save(wallet);

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(200);
        rs.setMessage("Employee removed from corporate plan");
        rs.setData("Removed: " + customer.getName());
        return rs;
    }
}

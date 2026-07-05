package com.ride.goeasy.entity;

import jakarta.persistence.*;

@Entity
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String companyName;

    @Column(unique = true, nullable = false)
    private String companyEmail;

    private String password;
    private String contactPerson;
    private String gstNumber;

    private Double walletBalance = 0.0;
    private Double monthlyBudgetPerEmployee = 500.0; // default ₹500/employee/month
    private boolean active = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }

    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }

    public Double getWalletBalance() { return walletBalance; }
    public void setWalletBalance(Double walletBalance) { this.walletBalance = walletBalance; }

    public Double getMonthlyBudgetPerEmployee() { return monthlyBudgetPerEmployee; }
    public void setMonthlyBudgetPerEmployee(Double monthlyBudgetPerEmployee) { this.monthlyBudgetPerEmployee = monthlyBudgetPerEmployee; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

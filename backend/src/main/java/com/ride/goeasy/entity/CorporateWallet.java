package com.ride.goeasy.entity;

import jakarta.persistence.*;

@Entity
public class CorporateWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Company company;

    @OneToOne
    @JoinColumn(nullable = false)
    private Customer customer;

    // Monthly budget allocated to this employee (overrides company default if set)
    private Double monthlyBudget;

    // How much has been used this month
    private Double usedThisMonth = 0.0;

    // "2025-06" — resets each month
    private String budgetMonth;

    private boolean active = true;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public Double getMonthlyBudget() {
        return monthlyBudget != null ? monthlyBudget : (company != null ? company.getMonthlyBudgetPerEmployee() : 500.0);
    }
    public void setMonthlyBudget(Double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public Double getUsedThisMonth() { return usedThisMonth != null ? usedThisMonth : 0.0; }
    public void setUsedThisMonth(Double usedThisMonth) { this.usedThisMonth = usedThisMonth; }

    public String getBudgetMonth() { return budgetMonth; }
    public void setBudgetMonth(String budgetMonth) { this.budgetMonth = budgetMonth; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Double getRemainingBudget() {
        return Math.max(0, getMonthlyBudget() - getUsedThisMonth());
    }
}

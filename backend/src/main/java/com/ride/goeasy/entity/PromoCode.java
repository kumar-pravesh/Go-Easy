package com.ride.goeasy.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true, nullable = false, length = 30)
    private String code;

    private Double discountPercent = 0.0;   // e.g. 10 = 10%
    private Double maxDiscountAmount = 0.0; // cap on discount ₹
    private Double minFare = 0.0;           // min fare to apply

    private Integer usageLimit = 100;       // max total uses
    private Integer usedCount = 0;          // how many times used

    private LocalDate validUntil;
    private boolean active = true;
    private String description;

    // Getters & Setters

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Double getDiscountPercent() { return discountPercent; }
    public void setDiscountPercent(Double discountPercent) { this.discountPercent = discountPercent; }

    public Double getMaxDiscountAmount() { return maxDiscountAmount; }
    public void setMaxDiscountAmount(Double maxDiscountAmount) { this.maxDiscountAmount = maxDiscountAmount; }

    public Double getMinFare() { return minFare; }
    public void setMinFare(Double minFare) { this.minFare = minFare; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getUsedCount() { return usedCount; }
    public void setUsedCount(Integer usedCount) { this.usedCount = usedCount; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}

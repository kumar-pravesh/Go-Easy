package com.ride.goeasy.dto;

public class TrustedContactDTO {
    private Integer id;
    private String name;
    private String phoneNumber;
    private String email;
    private long customerMobNo;

    public TrustedContactDTO() {}

    public TrustedContactDTO(Integer id, String name, String phoneNumber, String email, long customerMobNo) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.customerMobNo = customerMobNo;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public long getCustomerMobNo() {
        return customerMobNo;
    }
    public void setCustomerMobNo(long customerMobNo) {
        this.customerMobNo = customerMobNo;
    }
}

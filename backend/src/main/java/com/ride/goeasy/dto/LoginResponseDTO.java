package com.ride.goeasy.dto;

public class LoginResponseDTO {
    private String token;
    private Long mobileNo;
    private String name;
    private String role;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, Long mobileNo, String name, String role) {
        this.token = token;
        this.mobileNo = mobileNo;
        this.name = name;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(Long mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

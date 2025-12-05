package com.ride.goeasy.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;

@Entity
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String dname;
    private String licNo;
    private String upiId;
    private String dstatus;
    private Integer age;
    private Long mobNo;
    private String gender;
    private String mailId;
    
    

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Vehicle vehicle;

    public Driver() {
    }

    public Driver(String dname, String licNo, String upiId, String dstatus, Integer age, Long mobNo, String gender, String mailId, Vehicle vehicle) {
        this.dname = dname;
        this.licNo = licNo;
        this.upiId = upiId;
        this.dstatus = dstatus;
        this.age = age;
        this.mobNo = mobNo;
        this.gender = gender;
        this.mailId = mailId;
        setVehicle(vehicle);
    }

    public int getId() {
        return id;
    }

    

    public String getDname() {
        return dname;
    }

    public void setDname(String dname) {
        this.dname = dname;
    }

    public String getLicNo() {
        return licNo;
    }

    public void setLicNo(String licNo) {
        this.licNo = licNo;
    }

    public String getUpiId() {
        return upiId;
    }

    public void setUpiId(String upiId) {
        this.upiId = upiId;
    }

    public String getDstatus() {
        return dstatus;
    }

    public void setDstatus(String dstatus) {
        this.dstatus = dstatus;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Long getMobNo() {
        return mobNo;
    }

    public void setMobNo(Long mobNo) {
        this.mobNo = mobNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getMailId() {
        return mailId;
    }

    public void setMailId(String mailId) {
        this.mailId = mailId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        vehicle.setDriver(this);
    }

    @Override
    public String toString() {
        return "Driver{" +
                "id=" + id +
                ", dname='" + dname + '\'' +
                ", licNo='" + licNo + '\'' +
                ", upiId='" + upiId + '\'' +
                ", dstatus='" + dstatus + '\'' +
                ", age=" + age +
                ", mobNo=" + mobNo +
                ", gender='" + gender + '\'' +
                ", mailId='" + mailId + '\'' +
                ", vehicle=" + (vehicle != null ? vehicle.getVehicleName() : null) +
                '}';
    }
}

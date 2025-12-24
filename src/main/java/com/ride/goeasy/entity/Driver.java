package com.ride.goeasy.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.*;


@Entity
public class Driver {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String dname;
    private String licNo;
    private String upiId;
    private String dstatus;
    private Integer age;
    private Long mobNo;
    private String gender;
    private String mailId;
    private String password;
   

	@OneToOne
    private Userr userr;
    

    @OneToOne(mappedBy = "driver", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Vehicle vehicle;
    
    @OneToMany
    List<Booking> dblist;
    
   

    public List<Booking> getDblist() {
		return dblist;
	}

	public void setDblist(List<Booking> dblist) {
		this.dblist = dblist;
	}

	public Driver() {
    }

  

    public Driver(String dname, String licNo, String upiId, String dstatus, Integer age, Long mobNo, String gender,
			String mailId, String password, Userr userr, Vehicle vehicle, List<Booking> dblist) {
		super();
		this.dname = dname;
		this.licNo = licNo;
		this.upiId = upiId;
		this.dstatus = dstatus;
		this.age = age;
		this.mobNo = mobNo;
		this.gender = gender;
		this.mailId = mailId;
		this.password = password;
		this.userr = userr;
		this.vehicle = vehicle;
		this.dblist = dblist;
	}

	public Integer getId() {
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
    public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Userr getUserr() {
		return userr;
	}

	public void setUserr(Userr userr) {
		this.userr = userr;
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

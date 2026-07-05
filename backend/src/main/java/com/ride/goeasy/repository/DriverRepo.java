package com.ride.goeasy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ride.goeasy.entity.Driver;

@Repository
public interface DriverRepo extends JpaRepository<Driver, Integer>{
	 List<Driver> findByDstatus(String dstatus);  // To fetch only available drivers

    Optional<Driver> findFirstByMobNo(long mobNo);
    Optional<Driver> findFirstByMailId(String mailId);
    boolean existsByMobNo(long mobNo);
    boolean existsByMailId(String mailId);

    default Optional<Driver> findByMobNo(long mobNo) { return findFirstByMobNo(mobNo); }
    default Optional<Driver> findByMailId(String mailId) { return findFirstByMailId(mailId); }
}

package com.ride.goeasy.repository;

import com.ride.goeasy.entity.SOSEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SOSEventRepository extends JpaRepository<SOSEvent, Integer> {
}

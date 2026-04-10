package com.ride.goeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.service.DriverResetTask;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private DriverResetTask driverResetTask;

    @GetMapping("/triggerReset")
    public String triggerReset() {
        driverResetTask.resetDailyCancellations();
        return "Reset Task Triggered Successfully";
    }
}

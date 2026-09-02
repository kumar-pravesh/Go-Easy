package com.ride.goeasy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ride.goeasy.response.ResponseStructure;
import com.ride.goeasy.service.PaymentService;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/driver-confirm-cash")
    public ResponseStructure<String> driverConfirmCash(@RequestParam int bookingId) {
        return paymentService.driverConfirmCash(bookingId);
    }

    @PostMapping("/customer-confirm-cash")
    public ResponseStructure<String> customerConfirmCash(@RequestParam int bookingId) {
        return paymentService.customerConfirmCash(bookingId);
    }

    @PostMapping("/simulate-upi-success")
    public ResponseStructure<String> simulateUpiSuccess(@RequestParam int bookingId) {
        return paymentService.simulateUpiWebhook(bookingId, "SUCCESS");
    }

    @PostMapping("/webhook")
    public ResponseStructure<String> webhook(@RequestBody java.util.Map<String, Object> payload) {
        // Parse simulated webhook payload
        // Expected format: { "event": "payment.captured", "payload": { "payment": { "entity": { "notes": { "bookingId": 12 } }, "status": "captured" } } }
        int bookingId = -1;
        String status = "FAILED";
        try {
            if (payload.containsKey("payload")) {
                java.util.Map<String, Object> pl = (java.util.Map<String, Object>) payload.get("payload");
                if (pl.containsKey("payment")) {
                    java.util.Map<String, Object> payment = (java.util.Map<String, Object>) pl.get("payment");
                    if (payment.containsKey("entity")) {
                        java.util.Map<String, Object> entity = (java.util.Map<String, Object>) payment.get("entity");
                        if (entity.containsKey("notes")) {
                            java.util.Map<String, Object> notes = (java.util.Map<String, Object>) entity.get("notes");
                            if (notes.containsKey("bookingId")) {
                                bookingId = Integer.parseInt(notes.get("bookingId").toString());
                            }
                        }
                        if (entity.containsKey("status") && "captured".equalsIgnoreCase(entity.get("status").toString())) {
                            status = "SUCCESS";
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Webhook parsing failed: " + e.getMessage());
        }

        if (bookingId != -1) {
            return paymentService.simulateUpiWebhook(bookingId, status);
        }

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(400);
        rs.setMessage("Invalid Webhook Payload");
        rs.setData(null);
        return rs;
    }

    @GetMapping("/status")
    public ResponseStructure<String> getPaymentStatus(@RequestParam int bookingId) {
        return paymentService.getPaymentStatus(bookingId);
    }
}

package com.ride.goeasy.service;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ride.goeasy.entity.Booking;
import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.entity.Vehicle;
import com.ride.goeasy.enums.BookingStatus;
import com.ride.goeasy.repository.BookingRepo;
import com.ride.goeasy.repository.CustomerRepo;
import com.ride.goeasy.repository.DriverRepo;
import com.ride.goeasy.repository.PaymentRepo;
import com.ride.goeasy.repository.VehicleRepo;
import com.ride.goeasy.response.ResponseStructure;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private VehicleRepo vehicleRepo;

    @Autowired
    private DriverRepo driverRepo;

    @Autowired
    private MailService mailService;

    public ResponseStructure<String> driverConfirmCash(int bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = booking.getPayment();
        if (payment == null) {
            payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getFare());
            payment.setPaymentType("CASH");
            payment.setPaymentStatus("PENDING");
            payment.setCustomer(booking.getCustomer());
            payment.setVehicle(booking.getVehicle());
        }

        if ("PAID".equalsIgnoreCase(payment.getPaymentStatus()) || "SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new RuntimeException("Payment is already completed");
        }

        payment.setDriverConfirmedCash(true);
        payment.setPaymentType("CASH");
        payment.setLastConfirmedAt(LocalDateTime.now());

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());

        if (payment.isCustomerConfirmedCash()) {
            // Both confirmed! Complete the booking and payment
            completePaymentAndBooking(booking, payment, "CASH");
            rs.setMessage("Cash payment confirmed by both driver and customer. Ride Completed.");
            rs.setData("SUCCESS");
        } else {
            payment.setPaymentStatus("PENDING");
            paymentRepo.save(payment);
            rs.setMessage("Cash payment confirmed by driver. Waiting for customer confirmation.");
            rs.setData("PENDING_CUSTOMER_CONFIRMATION");
        }

        return rs;
    }

    public ResponseStructure<String> customerConfirmCash(int bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = booking.getPayment();
        if (payment == null) {
            payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getFare());
            payment.setPaymentType("CASH");
            payment.setPaymentStatus("PENDING");
            payment.setCustomer(booking.getCustomer());
            payment.setVehicle(booking.getVehicle());
        }

        if ("PAID".equalsIgnoreCase(payment.getPaymentStatus()) || "SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new RuntimeException("Payment is already completed");
        }

        payment.setCustomerConfirmedCash(true);
        payment.setPaymentType("CASH");
        payment.setLastConfirmedAt(LocalDateTime.now());

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());

        if (payment.isDriverConfirmedCash()) {
            // Both confirmed! Complete the booking and payment
            completePaymentAndBooking(booking, payment, "CASH");
            rs.setMessage("Cash payment confirmed by both driver and customer. Ride Completed.");
            rs.setData("SUCCESS");
        } else {
            payment.setPaymentStatus("PENDING");
            paymentRepo.save(payment);
            rs.setMessage("Cash payment confirmed by customer. Waiting for driver confirmation.");
            rs.setData("PENDING_DRIVER_CONFIRMATION");
        }

        return rs;
    }

    public ResponseStructure<String> simulateUpiWebhook(int bookingId, String status) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = booking.getPayment();
        if (payment == null) {
            payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getFare());
            payment.setPaymentType("UPI");
            payment.setPaymentStatus("PENDING");
            payment.setCustomer(booking.getCustomer());
            payment.setVehicle(booking.getVehicle());
        }

        if ("PAID".equalsIgnoreCase(payment.getPaymentStatus()) || "SUCCESS".equalsIgnoreCase(payment.getPaymentStatus())) {
            throw new RuntimeException("Payment is already completed");
        }

        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());

        if ("SUCCESS".equalsIgnoreCase(status)) {
            completePaymentAndBooking(booking, payment, "UPI");
            rs.setMessage("UPI Payment simulation successful. Ride Completed.");
            rs.setData("SUCCESS");
        } else {
            payment.setPaymentStatus("FAILED");
            paymentRepo.save(payment);
            rs.setMessage("UPI Payment simulation failed.");
            rs.setData("FAILED");
        }

        return rs;
    }

    public ResponseStructure<String> getPaymentStatus(int bookingId) {
        Booking booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        Payment payment = booking.getPayment();
        ResponseStructure<String> rs = new ResponseStructure<>();
        rs.setStatusCode(HttpStatus.OK.value());

        if (payment == null) {
            rs.setMessage("No payment details found for this booking.");
            rs.setData("NO_PAYMENT");
            return rs;
        }

        String status = payment.getPaymentStatus();
        if ("PENDING".equalsIgnoreCase(status)) {
            if (payment.isDriverConfirmedCash() && !payment.isCustomerConfirmedCash()) {
                status = "PENDING_CUSTOMER_CONFIRMATION";
            } else if (!payment.isDriverConfirmedCash() && payment.isCustomerConfirmedCash()) {
                status = "PENDING_DRIVER_CONFIRMATION";
            }
        }

        rs.setMessage("Payment status fetched");
        rs.setData(status);
        return rs;
    }

    private void completePaymentAndBooking(Booking booking, Payment payment, String type) {
        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setActiveBookingFlag(false);

        Customer customer = booking.getCustomer();
        if (customer != null) {
            customer.setActiveBookingFlag(false);
            customer.setCancellationCount(0);
            customer.setPenaltyAmount(0.0);
            customerRepo.save(customer);
        }

        Vehicle vehicle = booking.getVehicle();
        if (vehicle != null) {
            vehicle.setAvlStatus("AVAILABLE");
            vehicleRepo.save(vehicle);
        }

        Driver driver = booking.getDriver();
        if (driver == null && vehicle != null) {
            driver = vehicle.getDriver();
        }
        if (driver != null) {
            driver.setDstatus("AVAILABLE");
            driverRepo.save(driver);
        }

        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentType(type);
        paymentRepo.save(payment);

        booking.setPayment(payment);
        bookingRepo.save(booking);

        // Send payment confirmation mail
        if (customer != null && customer.getEmail() != null) {
            try {
                mailService.sendPaymentConfirmationMail(
                        customer.getEmail(),
                        booking.getFare(),
                        type + "-" + booking.getId()
                );
            } catch (Exception e) {
                System.err.println("Mail sending failed: " + e.getMessage());
            }
        }
    }
}

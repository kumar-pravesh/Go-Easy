package com.ride.goeasy.service;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ride.goeasy.entity.Customer;
import com.ride.goeasy.entity.Driver;
import com.ride.goeasy.entity.Payment;
import com.ride.goeasy.repository.PaymentRepo;

@Component
public class PaymentDisputeTask {

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private MailService mailService;

    // Runs every minute to check for cash confirmation mismatches/timeouts
    @Scheduled(fixedRate = 60000)
    public void checkPaymentDisputes() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
        List<Payment> pendingDisputes = paymentRepo.findPendingCashConfirmationsBefore(cutoff);

        for (Payment payment : pendingDisputes) {
            try {
                payment.setPaymentStatus("DISPUTED");
                payment.setPaymentDisputedAt(LocalDateTime.now());
                paymentRepo.save(payment);

                Customer customer = payment.getCustomer();
                Driver driver = payment.getBooking() != null ? payment.getBooking().getDriver() : null;

                String subject = "⚠️ Payment Dispute Alert - Go-Easy Ride #" + payment.getBooking().getId();
                String bodyTemplate = "Hello %s,\n\n"
                        + "There is a payment dispute regarding Go-Easy Ride #%d.\n"
                        + "One party confirmed cash payment, but the other did not respond within the 15-minute window.\n\n"
                        + "Details:\n"
                        + "- Fare Amount: ₹%.2f\n"
                        + "- Driver Confirmed: %b\n"
                        + "- Customer Confirmed: %b\n\n"
                        + "Our support team will investigate this dispute shortly.\n\n"
                        + "– Go-Easy Safety & Finance Team";

                // Notify Customer
                if (customer != null && customer.getEmail() != null) {
                    String customerBody = String.format(bodyTemplate, customer.getName(), 
                            payment.getBooking().getId(), payment.getAmount(), 
                            payment.isDriverConfirmedCash(), payment.isCustomerConfirmedCash());
                    mailService.sendMail(customer.getEmail(), subject, customerBody);
                }

                // Notify Driver
                if (driver != null && driver.getMailId() != null) {
                    String driverBody = String.format(bodyTemplate, driver.getDname(), 
                            payment.getBooking().getId(), payment.getAmount(), 
                            payment.isDriverConfirmedCash(), payment.isCustomerConfirmedCash());
                    mailService.sendMail(driver.getMailId(), subject, driverBody);
                }

                System.out.println("[PaymentDisputeTask] Dispute flagged and emails sent for booking #" + payment.getBooking().getId());

            } catch (Exception e) {
                System.err.println("[PaymentDisputeTask] Error processing dispute for payment ID " + payment.getId() + ": " + e.getMessage());
            }
        }
    }
}

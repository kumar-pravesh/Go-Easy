package com.ride.goeasy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;

	// Generic method to send mail
	public void sendMail(String to, String sub, String message) {

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(to);
		mail.setSubject(sub);
		mail.setFrom("pk4645478@gmail.com");
		mail.setText(message);

		javaMailSender.send(mail);

	}
	// Driver Registration
    public void sendDriverRegistrationMail(String to, String name) {
        sendMail(
            to,
            "Welcome to GoEasy - Driver Registration Successful",
            "Hello " + name + ",\n\n"
          + "Your driver profile has been created successfully.\n"
          + "Our team will verify your documents shortly.\n\n"
          + "– GoEasy Team"
        );
    }

    // Customer Registration
    public void sendCustomerRegistrationMail(String to, String name) {
        sendMail(
            to,
            "Welcome to GoEasy!",
            "Hi " + name + ",\n\n"
          + "Your account has been created successfully.\n"
          + "Book your first ride now!\n\n"
          + "– GoEasy Team"
        );
    }

    // Ride Cancellation
    public void sendRideCancellationMail(String to, String bookingId) {
        sendMail(
            to,
            "Ride Cancelled - #" + bookingId,
            "Your ride with booking ID " + bookingId + " has been cancelled.\n\n"
          + "If this wasn't you, please contact support.\n\n"
          + "– GoEasy Team"
        );
    }

    // Payment Confirmation
    public void sendPaymentConfirmationMail(String to, double amount, String txnId) {
        sendMail(
            to,
            "Payment Successful - GoEasy",
            "Payment of ₹" + amount + " was successful.\n\n"
          + "Transaction ID: " + txnId + "\n\n"
          + "Thank you for choosing GoEasy 🚖"
        );
    }
    
 // Booking Confirmation
    public void sendBookingConfirmationMail(
            String to,
            String customerName,
            long bookingId,
            String driverName,
            long driverMob
    ) {
        sendMail(
            to,
            "Booking Confirmed - GoEasy",
            "Hello " + customerName + ",\n\n"
          + "Your ride has been booked successfully.\n"
          + "Booking ID: " + bookingId + "\n"
          + "Driver Name: " + driverName + "\n"
          + "Driver Mobile: " + driverMob + "\n\n"
          + "Have a safe ride 🚖\n\n"
          + "– GoEasy Team"
        );
    }


}



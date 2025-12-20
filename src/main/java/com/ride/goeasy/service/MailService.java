package com.ride.goeasy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Service
public class MailService {

	@Autowired
	private JavaMailSender javaMailSender;

	public void sendMail(String to, String sub, String message) {

		SimpleMailMessage mail = new SimpleMailMessage();
		mail.setTo(to);
		mail.setSubject(sub);
		mail.setFrom("pk4645478@gmail.com");
		mail.setText(message);

		javaMailSender.send(mail);

	}
}

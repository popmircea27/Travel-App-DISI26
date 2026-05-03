package com.example.travelappbe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String adminEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${app.admin.email}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    public void sendContactMessage(String fromEmail, String subject, String message) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(adminEmail);
        mailMessage.setSubject("Contact Form: " + subject);
        mailMessage.setText("Message from: " + fromEmail + "\n\n" + message);
        mailSender.send(mailMessage);
    }
}
package com.example.travelappbe.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String adminEmail;

    @Autowired
    public EmailService(JavaMailSender mailSender, @Value("${app.admin.email}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    public void sendContactMessage(String fromEmail, String subject, String message) {
        try {
            if (fromEmail == null || fromEmail.trim().isEmpty()) {
                logger.warn("Cannot send email: from email is empty");
                throw new IllegalArgumentException("Sender email cannot be empty");
            }

            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(fromEmail);
            mailMessage.setTo(adminEmail);
            mailMessage.setSubject("Contact Form: " + subject);
            mailMessage.setText("Message from: " + fromEmail + "\n\n" + message);
            
            mailSender.send(mailMessage);
            logger.info("Email sent successfully from {} to {}", fromEmail, adminEmail);
        } catch (IllegalArgumentException e) {
            logger.error("Email validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Failed to send email from {} to {}: {}", fromEmail, adminEmail, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
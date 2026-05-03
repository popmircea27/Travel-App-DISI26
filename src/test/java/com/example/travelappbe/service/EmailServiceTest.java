package com.example.travelappbe.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("Email Service Unit Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @Test
    @DisplayName("Should construct and send contact message correctly")
    void testSendContactMessage() {
        emailService = new EmailService(mailSender, "admin@travelapp.com");
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendContactMessage("tourist@example.com", "Question", "This is a test.");

        verify(mailSender).send(messageCaptor.capture());
        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertEquals("admin@travelapp.com", sentMessage.getTo()[0]);
        assertEquals("Contact Form: Question", sentMessage.getSubject());
        assertEquals("Message from: tourist@example.com\n\nThis is a test.", sentMessage.getText());
    }
}
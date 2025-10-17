package com.example.emailfeature;

import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    // Set your sender address here
    private static final String SENDER_EMAIL = "email@example.com";

    public EmailService(JavaMailSender mailSender, EmailRepository emailRepository) {
        this.mailSender = mailSender;
        this.emailRepository = emailRepository;
    }

    @Transactional
    public void sendEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(SENDER_EMAIL);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            Email email = new Email();
            email.setSender(SENDER_EMAIL);
            email.setRecipient(to);
            email.setSubject(subject);
            email.setBody(body);
            email.setSentAt(LocalDateTime.now());
            email.setReceived(false);
            emailRepository.saveAndFlush(email);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public List<Email> list(Pageable pageable) {
        return emailRepository.findAllBy(pageable).toList();
    }
}
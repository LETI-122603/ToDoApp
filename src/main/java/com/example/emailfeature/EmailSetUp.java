package com.example.emailfeature;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;

@Configuration
public class EmailSetUp {

    @Bean
    public JavaMailSender javaMailSender() {
        return new JavaMailSender() {
            @Override
            public void send(SimpleMailMessage simpleMessage) {
                System.out.println("Pretending to send email: " + simpleMessage);
            }

            @Override
            public void send(SimpleMailMessage... simpleMessages) {
                for (SimpleMailMessage msg : simpleMessages) {
                    send(msg);
                }
            }

            @Override
            public MimeMessage createMimeMessage() {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public MimeMessage createMimeMessage(InputStream contentStream) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public void send(MimeMessage mimeMessage) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public void send(MimeMessage... mimeMessages) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public void send(org.springframework.mail.javamail.MimeMessagePreparator mimeMessagePreparator) {
                throw new UnsupportedOperationException("Not implemented");
            }

            @Override
            public void send(org.springframework.mail.javamail.MimeMessagePreparator... mimeMessagePreparators) {
                throw new UnsupportedOperationException("Not implemented");
            }
        };
    }
}

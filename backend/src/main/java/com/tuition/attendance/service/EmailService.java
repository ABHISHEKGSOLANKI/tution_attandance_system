package com.tuition.attendance.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;
    private final String mailUsername;

    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username:}") String mailUsername) {
        this.mailSender = mailSender;
        this.mailUsername = mailUsername;
    }

    public void sendCredentials(String to, String name, String username, String password) {
        if (mailSender instanceof JavaMailSenderImpl sender
                && (sender.getHost() == null || sender.getHost().isBlank())) {
            log.info("SMTP not configured. Credentials for {} -> username: {}, password: {}", to, username, password);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        if (mailUsername != null && !mailUsername.isBlank()) {
            message.setFrom(mailUsername);
        }
        message.setTo(to);
        message.setSubject("Tuition Attendance Login Credentials");
        message.setText(
                "Hello " + name + ",\n\n"
                        + "Your account has been approved.\n"
                        + "Username: " + username + "\n"
                        + "Temporary Password: " + password + "\n\n"
                        + "Please log in and change your password immediately."
        );

        try {
            log.info("Sending credentials email to {}", to);
            mailSender.send(message);
            log.info("Credentials email sent successfully to {}", to);
        } catch (MailException ex) {
            log.error("Failed to send credentials email to {}", to, ex);
            throw ex;
        }
    }
}

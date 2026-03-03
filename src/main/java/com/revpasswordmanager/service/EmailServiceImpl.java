package com.revpasswordmanager.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger logger = LogManager.getLogger(EmailServiceImpl.class);

    @Autowired
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    @Override
    public void sendOtp(String to, String otp) {
        String subject = "2FA Verification Code";
        String body = "Your code is: " + otp;
        sendEmail(to, subject, body);
        logger.info("2FA OTP sent to: {}", to);
    }

    @Override
    public void sendPasswordChangeVerification(String to, String otp) {
        String subject = "Master Password Change Verification";
        String body = "Your verification code is: " + otp;
        sendEmail(to, subject, body);
        logger.info("Password change verification OTP sent to: {}", to);
    }

    @Override
    public void sendRegistrationOtp(String to, String otp) {
        String subject = "RevVault Registration Verification";
        String body = "Welcome to RevVault! Your verification code is: " + otp;
        sendEmail(to, subject, body);
        logger.info("Registration OTP sent to: {}", to);
    }

    private void sendEmail(String to, String subject, String body) {
        // Log to console for simulation/debugging
        System.out.println("========================================");
        System.out.println("SIMULATED EMAIL TO: " + to);
        System.out.println("SUBJECT: " + subject);
        System.out.println("BODY: " + body);
        System.out.println("========================================");

        logger.debug("Attempting to send email to {} with subject: {}", to, subject);
        try {
            org.springframework.mail.SimpleMailMessage message = new org.springframework.mail.SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            logger.info("Successfully sent email to {}", to);
        } catch (Exception e) {
            logger.error("CRITICAL: Failed to send email to {}. Error: {}", to, e.getMessage());
            throw new com.revpasswordmanager.exception.EmailDeliveryException(
                    "Failed to send verification email. Please check your SMTP configuration.", e);
        }
    }
}

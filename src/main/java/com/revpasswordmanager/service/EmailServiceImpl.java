package com.revpasswordmanager.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements IEmailService {

    private static final Logger logger = LogManager.getLogger(EmailServiceImpl.class);

    @Override
    public void sendOtp(String to, String otp) {
        // In a real application, this would use JavaMailSender to send an actual email.
        // For this demo, we'll log the OTP.
        logger.info("SIMULATED EMAIL: To: {}, Subject: 2FA Verification Code, Body: Your code is: {}", to, otp);
        System.out.println("DEBUG: OTP sent to " + to + " is: " + otp);
    }

    @Override
    public void sendPasswordChangeVerification(String to, String otp) {
        logger.info(
                "SIMULATED EMAIL: To: {}, Subject: Master Password Change Verification, Body: Your verification code is: {}",
                to, otp);
        System.out.println("DEBUG: Password change OTP sent to " + to + " is: " + otp);
    }
}

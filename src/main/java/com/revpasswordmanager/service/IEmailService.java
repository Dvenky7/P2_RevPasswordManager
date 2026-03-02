package com.revpasswordmanager.service;

public interface IEmailService {
    void sendOtp(String to, String otp);

    void sendPasswordChangeVerification(String to, String otp);
}

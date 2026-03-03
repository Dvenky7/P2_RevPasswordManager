package com.revpasswordmanager.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    public void testSendOtp_Success() {
        emailService.sendOtp("test@example.com", "123456");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    public void testSendPasswordChangeVerification_Success() {
        emailService.sendPasswordChangeVerification("test@example.com", "654321");
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test(expected = com.revpasswordmanager.exception.EmailDeliveryException.class)
    public void testSendEmail_Failure() {
        doThrow(new RuntimeException("Mail server down")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendOtp("test@example.com", "123456");
    }
}

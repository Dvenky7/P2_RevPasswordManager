package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.entity.VerificationCode;
import com.revpasswordmanager.exception.OtpExpiredException;
import com.revpasswordmanager.exception.OtpLimitExceededException;
import com.revpasswordmanager.exception.EmailDeliveryException;
import com.revpasswordmanager.exception.InvalidVerificationCodeException;
import com.revpasswordmanager.repository.IVerificationCodeRepository;
import com.revpasswordmanager.repository.IUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceOtpTest {

    @Mock
    private IVerificationCodeRepository verificationCodeRepository;

    @Mock
    private IEmailService emailService;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    public void testGenerateOtp_Success() {
        // When
        String otp = userService.generateOtp(testUser, "2FA");

        // Then
        assertThat(otp).hasSize(6);
        verify(verificationCodeRepository, times(1)).save(any(VerificationCode.class));
        verify(emailService, times(1)).sendOtp(eq("test@example.com"), eq(otp));
    }

    @Test
    public void testGenerateOtp_EmailFailure() {
        // Given
        doThrow(new EmailDeliveryException("Failure", null)).when(emailService).sendOtp(anyString(), anyString());

        // Then
        assertThatThrownBy(() -> userService.generateOtp(testUser, "2FA"))
                .isInstanceOf(EmailDeliveryException.class);
    }

    @Test
    public void testVerifyOtp_Success() {
        // Given
        VerificationCode code = new VerificationCode();
        code.setCode("123456");
        code.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        code.setIsUsed(false);
        code.setFailedAttempts(0);

        when(verificationCodeRepository.findByUserAndCodeAndPurposeAndIsUsedFalse(any(), eq("123456"), eq("2FA")))
                .thenReturn(Optional.of(code));

        // When
        boolean result = userService.verifyOtp(testUser, "123456", "2FA");

        // Then
        assertThat(result).isTrue();
        assertThat(code.getIsUsed()).isTrue();
        verify(verificationCodeRepository, times(1)).save(code);
    }

    @Test
    public void testVerifyOtp_Expired() {
        // Given
        VerificationCode code = new VerificationCode();
        code.setCode("123456");
        code.setExpiryTime(LocalDateTime.now().minusMinutes(1));
        code.setIsUsed(false);

        when(verificationCodeRepository.findByUserAndCodeAndPurposeAndIsUsedFalse(any(), eq("123456"), eq("2FA")))
                .thenReturn(Optional.of(code));

        // Then
        assertThatThrownBy(() -> userService.verifyOtp(testUser, "123456", "2FA"))
                .isInstanceOf(OtpExpiredException.class);
    }

    @Test
    public void testVerifyOtp_InvalidCode() {
        // Given
        when(verificationCodeRepository.findByUserAndCodeAndPurposeAndIsUsedFalse(any(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        // Then
        assertThatThrownBy(() -> userService.verifyOtp(testUser, "wrong", "2FA"))
                .isInstanceOf(InvalidVerificationCodeException.class);
    }

    @Test
    public void testVerifyOtp_AttemptExceeded() {
        // Given
        VerificationCode code = new VerificationCode();
        code.setCode("123456");
        code.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        code.setIsUsed(false);
        code.setFailedAttempts(3);

        when(verificationCodeRepository.findByUserAndCodeAndPurposeAndIsUsedFalse(any(), eq("123456"), eq("2FA")))
                .thenReturn(Optional.of(code));

        // Then
        assertThatThrownBy(() -> userService.verifyOtp(testUser, "123456", "2FA"))
                .isInstanceOf(OtpLimitExceededException.class);
    }
}

package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.SecurityQuestion;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.entity.VerificationCode;
import com.revpasswordmanager.mapper.UserMapper;
import com.revpasswordmanager.repository.IVerificationCodeRepository;
import com.revpasswordmanager.repository.IUserRepository;
import com.revpasswordmanager.service.IEmailService;
import com.revpasswordmanager.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class UserServiceImpl implements IUserService {

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private IEmailService emailService;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            logger.warn("Registration failed: Username already exists - {}", registrationDto.getUsername());
            throw new DuplicateUserException("Username already exists: " + registrationDto.getUsername());
        }
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email already exists - {}", registrationDto.getEmail());
            throw new DuplicateUserException("Email already exists: " + registrationDto.getEmail());
        }

        User user = userMapper.toEntity(registrationDto);
        user.setMasterPasswordHash(passwordEncoder.encode(registrationDto.getMasterPassword()));

        List<SecurityQuestion> securityQuestions = new ArrayList<>();

        if (registrationDto.getSecurityQuestion1() != null) {
            securityQuestions.add(new SecurityQuestion(null, user, registrationDto.getSecurityQuestion1(),
                    passwordEncoder.encode(registrationDto.getSecurityAnswer1().toLowerCase()), null));
        }
        if (registrationDto.getSecurityQuestion2() != null) {
            securityQuestions.add(new SecurityQuestion(null, user, registrationDto.getSecurityQuestion2(),
                    passwordEncoder.encode(registrationDto.getSecurityAnswer2().toLowerCase()), null));
        }
        if (registrationDto.getSecurityQuestion3() != null) {
            securityQuestions.add(new SecurityQuestion(null, user, registrationDto.getSecurityQuestion3(),
                    passwordEncoder.encode(registrationDto.getSecurityAnswer3().toLowerCase()), null));
        }

        user.setSecurityQuestions(securityQuestions);
        User savedUser = userRepository.save(user);
        logger.info("New user registered successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsernameWithSecurityQuestions(String username) {
        return userRepository.findByUsernameWithSecurityQuestions(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional
    public void updateMasterPassword(User user, String oldPassword, String newPassword) {
        if (passwordEncoder.matches(oldPassword, user.getMasterPasswordHash())) {
            user.setMasterPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            logger.info("Master password updated for user: {}", user.getUsername());
        } else {
            logger.warn("Password update failed for user: {}. Old password mismatch.", user.getUsername());
            throw new InvalidCredentialsException("Old password does not match");
        }
    }

    @Transactional
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean verifySecurityQuestions(String username, List<String> answers) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }
        User user = userOpt.get();
        List<SecurityQuestion> questions = user.getSecurityQuestions();

        if (questions.size() != answers.size()) {
            return false;
        }

        for (int i = 0; i < questions.size(); i++) {
            if (!passwordEncoder.matches(answers.get(i).toLowerCase(), questions.get(i).getAnswerHash())) {
                return false;
            }
        }
        return true;
    }

    @Transactional
    public void resetPassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
        user.setMasterPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setLastLoginAt(java.time.LocalDateTime.now());
            userRepository.save(user);
        });
    }

    @Transactional
    public void incrementFailedAttempts(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            int newAttempts = user.getFailedAttempts() + 1;
            user.setFailedAttempts(newAttempts);
            if (newAttempts >= 5) {
                user.setAccountLocked(true);
                logger.error("Account LOCKED for user: {} due to {} failed attempts", username, newAttempts);
            } else {
                logger.warn("Failed login attempt #{} for user: {}", newAttempts, username);
            }
            userRepository.save(user);
        });
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getMasterPasswordHash());
    }

    @Override
    @Transactional
    public void toggleTwoFactor(String username, boolean enabled) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setTwoFactorEnabled(enabled);
        userRepository.save(user);
        logger.info("2FA {} for user: {}", enabled ? "ENABLED" : "DISABLED", username);
    }

    @Override
    @Transactional
    public String generateOtp(User user, String purpose) {
        String code = String.format("%06d", new Random().nextInt(1000000));
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setUser(user);
        verificationCode.setCode(code);
        verificationCode.setPurpose(purpose);
        verificationCode.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        verificationCode.setIsUsed(false);
        verificationCodeRepository.save(verificationCode);

        if ("2FA".equals(purpose)) {
            emailService.sendOtp(user.getEmail(), code);
        } else if ("PROFILE_CHANGE".equals(purpose)) {
            emailService.sendPasswordChangeVerification(user.getEmail(), code);
        }

        return code;
    }

    @Override
    @Transactional
    public boolean verifyOtp(User user, String code, String purpose) {
        Optional<VerificationCode> otpOpt = verificationCodeRepository
                .findByUserAndCodeAndPurposeAndIsUsedFalse(user, code, purpose);

        if (otpOpt.isPresent()) {
            VerificationCode otp = otpOpt.get();
            if (otp.getExpiryTime().isAfter(LocalDateTime.now())) {
                otp.setIsUsed(true);
                verificationCodeRepository.save(otp);
                return true;
            }
        }
        return false;
    }
}

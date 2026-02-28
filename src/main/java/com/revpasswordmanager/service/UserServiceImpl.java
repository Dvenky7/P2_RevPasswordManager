package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.SecurityQuestion;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.repository.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revpasswordmanager.mapper.UserMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
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
        return userRepository.save(user);
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
        } else {
            throw new RuntimeException("Old password does not match");
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
                .orElseThrow(() -> new RuntimeException("User not found"));
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
            }
            userRepository.save(user);
        });
    }

    public boolean verifyPassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getMasterPasswordHash());
    }
}

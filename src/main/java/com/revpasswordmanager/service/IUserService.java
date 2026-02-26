package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.User;
import java.util.Optional;

public interface IUserService {
    User registerUser(UserRegistrationDto registrationDto);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    void updateMasterPassword(User user, String oldPassword, String newPassword);

    boolean verifySecurityQuestions(String username, java.util.List<String> answers);

    void resetPassword(String username, String newPassword);

    boolean verifyPassword(User user, String rawPassword);

    User updateUser(User user);
}

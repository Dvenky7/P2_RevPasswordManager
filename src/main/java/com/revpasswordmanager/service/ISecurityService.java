package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.User;

public interface ISecurityService {
    String generateVerificationCode(User user, String purpose);

    boolean verifyCode(User user, String code);
}


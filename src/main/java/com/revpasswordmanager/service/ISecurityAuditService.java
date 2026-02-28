package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import java.util.List;

public interface ISecurityAuditService {
    List<Credential> findReusedPasswords(User user);

    List<Credential> findOldPasswords(User user, int days);

    List<Credential> findWeakPasswords(User user);
}

package com.revpasswordmanager.service;

import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.repository.ICredentialRepository;
import com.revpasswordmanager.util.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SecurityAuditServiceImpl implements ISecurityAuditService {

    @Autowired
    private ICredentialRepository credentialRepository;

    @Value("${encryption.secret.key}")
    private String secretKey;

    @Override
    public List<Credential> findReusedPasswords(User user) {
        List<Credential> credentials = credentialRepository.findByUser(user);
        Map<String, List<Credential>> passwordMap = new HashMap<>();

        for (Credential cred : credentials) {
            try {
                String decrypted = CryptoUtil.decrypt(cred.getEncryptedPassword(), secretKey);
                passwordMap.computeIfAbsent(decrypted, k -> new ArrayList<>()).add(cred);
            } catch (Exception e) {
                // Log error
            }
        }

        return passwordMap.values().stream()
                .filter(list -> list.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Credential> findOldPasswords(User user, int days) {
        LocalDateTime limit = LocalDateTime.now().minusDays(days);
        return credentialRepository.findByUser(user).stream()
                .filter(cred -> cred.getUpdatedAt().isBefore(limit))
                .collect(Collectors.toList());
    }

    @Override
    public List<Credential> findWeakPasswords(User user) {
        return credentialRepository.findByUser(user).stream()
                .filter(cred -> "Weak".equalsIgnoreCase(cred.getPasswordStrength()))
                .collect(Collectors.toList());
    }
}

package com.revpasswordmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.exception.*;
import com.revpasswordmanager.util.CryptoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class BackupServiceImpl implements IBackupService {

    private static final Logger logger = LogManager.getLogger(BackupServiceImpl.class);

    @Autowired
    private IVaultService vaultService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${encryption.secret.key}")
    private String secretKey;

    @Override
    public String exportVault(User user) {
        try {
            List<CredentialDto> credentials = vaultService.getCredentialsByUser(user);

            Map<String, Object> backupData = new HashMap<>();
            backupData.put("username", user.getUsername());
            backupData.put("exportDate", LocalDateTime.now().toString());
            backupData.put("credentials", credentials);

            String json = objectMapper.writeValueAsString(backupData);
            String encrypted = CryptoUtil.encrypt(json, secretKey);
            logger.info("Vault exported successfully for user: {}", user.getUsername());
            return encrypted;
        } catch (Exception e) {
            logger.error("Vault export failed for user: {}", user.getUsername(), e);
            throw new EncryptionException("Failed to export and encrypt vault", e);
        }
    }

    @Override
    @Transactional
    public void importVault(User user, String encryptedJson) {
        try {
            String json = CryptoUtil.decrypt(encryptedJson, secretKey);
            Map<String, Object> backupData = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });

            List<CredentialDto> credentials = objectMapper.convertValue(backupData.get("credentials"),
                    new TypeReference<List<CredentialDto>>() {
                    });

            for (CredentialDto dto : credentials) {
                dto.setId(null); // Ensure we create new entries
                vaultService.addCredential(user, dto);
            }
            logger.info("Vault imported successfully for user: {}. {} credentials added.", user.getUsername(),
                    credentials.size());
        } catch (Exception e) {
            logger.error("Vault import failed for user: {}", user.getUsername(), e);
            throw new InvalidBackupFileException("Failed to decrypt or parse backup file", e);
        }
    }
}

package com.revpasswordmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.util.CryptoUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
            // Reveal passwords for export
            for (CredentialDto dto : credentials) {
                try {
                    dto.setPassword(vaultService.revealPassword(dto.getId(), user));
                } catch (Exception e) {
                    logger.warn("Could not reveal password for credential {} during export", dto.getId());
                }
            }
            String json = objectMapper.writeValueAsString(credentials);
            String encrypted = CryptoUtil.encrypt(json, secretKey);
            logger.info("Vault exported successfully for user: {}", user.getUsername());
            return encrypted;
        } catch (Exception e) {
            logger.error("Export failed for user: {}", user.getUsername(), e);
            throw new RuntimeException("Failed to export vault: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void importVault(User user, String encryptedContent) {
        try {
            String json = CryptoUtil.decrypt(encryptedContent, secretKey);
            List<CredentialDto> credentials = objectMapper.readValue(json, new TypeReference<List<CredentialDto>>() {
            });

            for (CredentialDto dto : credentials) {
                dto.setId(null); // Force new entry
                vaultService.addCredential(user, dto);
            }
            logger.info("Imported {} credentials for user: {}", credentials.size(), user.getUsername());
        } catch (Exception e) {
            logger.error("Import failed for user: {}", user.getUsername(), e);
            throw new RuntimeException(
                    "Failed to import vault. Ensure the backup file is valid and encrypted correctly.");
        }
    }
}

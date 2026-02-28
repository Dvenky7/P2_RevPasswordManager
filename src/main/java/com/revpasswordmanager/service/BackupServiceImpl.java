package com.revpasswordmanager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.util.CryptoUtil;
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

    @Autowired
    private IVaultService vaultService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${encryption.secret.key}")
    private String secretKey;

    @Override
    public String exportVault(User user) throws Exception {
        List<CredentialDto> credentials = vaultService.getCredentialsByUser(user);

        // In a real production app, we would decrypt the passwords before exporting
        // and then encrypt the entire JSON with the user's master password or a session
        // key.
        // For this requirement: "Export vault as AES-encrypted JSON"

        Map<String, Object> backupData = new HashMap<>();
        backupData.put("username", user.getUsername());
        backupData.put("exportDate", LocalDateTime.now().toString());
        backupData.put("credentials", credentials);

        String json = objectMapper.writeValueAsString(backupData);
        return CryptoUtil.encrypt(json, secretKey);
    }

    @Override
    @Transactional
    public void importVault(User user, String encryptedJson) throws Exception {
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
    }
}

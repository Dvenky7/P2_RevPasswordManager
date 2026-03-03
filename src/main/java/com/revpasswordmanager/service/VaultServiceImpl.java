package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.exception.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.revpasswordmanager.mapper.CredentialMapper;
import com.revpasswordmanager.repository.ICredentialRepository;
import com.revpasswordmanager.util.CryptoUtil;
import com.revpasswordmanager.util.PasswordStrengthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultServiceImpl implements IVaultService {

    private static final Logger logger = LogManager.getLogger(VaultServiceImpl.class);

    @Autowired
    private ICredentialRepository credentialRepository;

    @Autowired
    private CredentialMapper credentialMapper;

    @Value("${encryption.secret.key}")
    private String secretKey;

    public List<CredentialDto> getCredentialsByUser(User user) {
        return credentialRepository.findByUser(user).stream()
                .map(credentialMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CredentialDto addCredential(User user, CredentialDto credentialDto) {
        try {
            Credential credential = credentialMapper.toEntity(credentialDto);
            credential.setUser(user);
            credential.setEncryptedPassword(CryptoUtil.encrypt(credentialDto.getPassword(), secretKey));
            credential.setPasswordStrength(PasswordStrengthUtil.calculateStrength(credentialDto.getPassword()));
            credential.setLastAccessedAt(LocalDateTime.now());
            CredentialDto saved = credentialMapper.toDto(credentialRepository.save(credential));
            logger.info("New credential added for user: {}, account: {}", user.getUsername(),
                    credential.getAccountName());
            return saved;
        } catch (Exception e) {
            logger.error("Encryption failed while adding credential for user: {}. Details: {}", user.getUsername(),
                    e.getMessage(), e);
            throw new EncryptionException("Failed to encrypt password: " + e.getMessage(), e);
        }
    }

    @Transactional
    public CredentialDto updateCredential(CredentialDto credentialDto) {
        Credential credential = credentialRepository.findById(credentialDto.getId())
                .orElseThrow(() -> new CredentialNotFoundException(
                        "Credential not found with id: " + credentialDto.getId()));

        credential.setAccountName(credentialDto.getAccountName());
        credential.setUsername(credentialDto.getUsername());
        credential.setUrl(credentialDto.getUrl());
        credential.setNotes(credentialDto.getNotes());
        credential.setCategory(credentialDto.getCategory());
        credential.setIsFavorite(credentialDto.getIsFavorite());

        if (credentialDto.getPassword() != null && !credentialDto.getPassword().isEmpty()) {
            try {
                credential.setEncryptedPassword(CryptoUtil.encrypt(credentialDto.getPassword(), secretKey));
                credential.setPasswordStrength(PasswordStrengthUtil.calculateStrength(credentialDto.getPassword()));
            } catch (Exception e) {
                throw new EncryptionException("Failed to encrypt updated password", e);
            }
        }

        return credentialMapper.toDto(credentialRepository.save(credential));
    }

    @Transactional
    public void deleteCredential(Long id, User user) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new CredentialNotFoundException("Credential not found with id: " + id));
        if (!credential.getUser().getId().equals(user.getId())) {
            logger.warn("Unauthorized delete attempt for credential {} by user {}", id, user.getUsername());
            throw new InvalidMasterPasswordException("Unauthorized access to credential");
        }
        credentialRepository.delete(credential);
        logger.info("Credential deleted: {} for user {}", id, user.getUsername());
    }

    @Transactional
    public String revealPassword(Long id, User user) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new CredentialNotFoundException("Credential not found with id: " + id));

        if (!credential.getUser().getId().equals(user.getId())) {
            logger.warn("Unauthorized reveal attempt for credential {} by user {}", id, user.getUsername());
            throw new InvalidMasterPasswordException("Unauthorized access to credential");
        }

        credential.setLastAccessedAt(LocalDateTime.now());
        credentialRepository.save(credential);

        try {
            String decrypted = CryptoUtil.decrypt(credential.getEncryptedPassword(), secretKey);
            logger.info("Password revealed for credential {} by user {}", id, user.getUsername());
            return decrypted;
        } catch (Exception e) {
            logger.error("Decryption failed for credential {} for user {}", id, user.getUsername(), e);
            throw new EncryptionException("Failed to decrypt password", e);
        }
    }

    public List<CredentialDto> searchCredentials(User user, String query) {
        return credentialRepository.searchVault(user, query).stream()
                .map(credentialMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<CredentialDto> getVaultEntries(User user, String query, String category, String sortBy) {
        List<Credential> filtered;
        if (query != null && !query.trim().isEmpty()) {
            filtered = credentialRepository.searchVault(user, query);
        } else if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            filtered = credentialRepository.findByUserAndCategory(user, category);
        } else {
            filtered = credentialRepository.findByUser(user);
        }

        // Apply sorting
        if ("name".equalsIgnoreCase(sortBy)) {
            filtered.sort((c1, c2) -> c1.getAccountName().compareToIgnoreCase(c2.getAccountName()));
        } else if ("date".equalsIgnoreCase(sortBy)) {
            filtered.sort((c1, c2) -> c2.getUpdatedAt().compareTo(c1.getUpdatedAt())); // Descending
        }

        return filtered.stream()
                .map(credentialMapper::toDto)
                .collect(Collectors.toList());
    }

    public CredentialDto getCredentialById(Long id, User user) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new CredentialNotFoundException("Credential not found with id: " + id));
        if (!credential.getUser().getId().equals(user.getId())) {
            throw new InvalidMasterPasswordException("Unauthorized access to credential");
        }
        return credentialMapper.toDto(credential);
    }

    public byte[] exportVault(User user) throws Exception {
        List<CredentialDto> credentials = getCredentialsByUser(user);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(credentials);
        String encrypted = CryptoUtil.encrypt(json, secretKey);
        return encrypted.getBytes();
    }

    @Transactional
    public void importVault(User user, byte[] data) throws Exception {
        String encrypted = new String(data);
        String json = CryptoUtil.decrypt(encrypted, secretKey);
        ObjectMapper mapper = new ObjectMapper();
        List<CredentialDto> credentials = mapper.readValue(json,
                new TypeReference<List<CredentialDto>>() {
                });

        for (CredentialDto dto : credentials) {
            // Check if credential already exists by account name and username
            // Simple approach: Always add as new, or we could update.
            // Requirement says "Import", so we add them.
            dto.setId(null); // Ensure it's treated as new
            addCredential(user, dto);
        }
    }
}

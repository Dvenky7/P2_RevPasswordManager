package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.mapper.CredentialMapper;
import com.revpasswordmanager.repository.ICredentialRepository;
import com.revpasswordmanager.util.CryptoUtil;
import com.revpasswordmanager.util.PasswordStrengthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultServiceImpl implements IVaultService {

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
    public CredentialDto addCredential(User user, CredentialDto credentialDto) throws Exception {
        Credential credential = credentialMapper.toEntity(credentialDto);
        credential.setUser(user);
        credential.setEncryptedPassword(CryptoUtil.encrypt(credentialDto.getPassword(), secretKey));
        credential.setPasswordStrength(PasswordStrengthUtil.calculateStrength(credentialDto.getPassword()));
        credential.setLastAccessedAt(LocalDateTime.now());
        return credentialMapper.toDto(credentialRepository.save(credential));
    }

    @Transactional
    public CredentialDto updateCredential(CredentialDto credentialDto) throws Exception {
        Credential credential = credentialRepository.findById(credentialDto.getId())
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        credential.setAccountName(credentialDto.getAccountName());
        credential.setUsername(credentialDto.getUsername());
        credential.setUrl(credentialDto.getUrl());
        credential.setNotes(credentialDto.getNotes());
        credential.setCategory(credentialDto.getCategory());
        credential.setIsFavorite(credentialDto.getIsFavorite());

        if (credentialDto.getPassword() != null && !credentialDto.getPassword().isEmpty()) {
            credential.setEncryptedPassword(CryptoUtil.encrypt(credentialDto.getPassword(), secretKey));
            credential.setPasswordStrength(PasswordStrengthUtil.calculateStrength(credentialDto.getPassword()));
        }

        return credentialMapper.toDto(credentialRepository.save(credential));
    }

    @Transactional
    public void deleteCredential(Long id, User user) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found"));
        if (!credential.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        credentialRepository.delete(credential);
    }

    @Transactional
    public String revealPassword(Long id, User user) throws Exception {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        if (!credential.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        credential.setLastAccessedAt(LocalDateTime.now());
        credentialRepository.save(credential);

        return CryptoUtil.decrypt(credential.getEncryptedPassword(), secretKey);
    }

    public List<CredentialDto> searchCredentials(User user, String query) {
        return credentialRepository.findByUserAndAccountNameContainingIgnoreCase(user, query).stream()
                .map(credentialMapper::toDto)
                .collect(Collectors.toList());
    }
}

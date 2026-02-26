package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.mapper.CredentialMapper;
import com.revpasswordmanager.repository.ICredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultServiceImpl implements IVaultService {

    @Autowired
    private ICredentialRepository credentialRepository;

    @Autowired
    private IEncryptionService encryptionService;

    @Autowired
    private CredentialMapper credentialMapper;

    public List<CredentialDto> getCredentialsByUser(User user) {
        return credentialRepository.findByUser(user).stream()
                .map(credentialMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CredentialDto addCredential(User user, CredentialDto credentialDto) throws Exception {
        Credential credential = credentialMapper.toEntity(credentialDto);
        credential.setUser(user);
        credential.setEncryptedPassword(encryptionService.encrypt(credentialDto.getPassword()));
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

        if (credentialDto.getPassword() != null && !credentialDto.getPassword().isEmpty()) {
            credential.setEncryptedPassword(encryptionService.encrypt(credentialDto.getPassword()));
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

    public String revealPassword(Long id, User user) throws Exception {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credential not found"));

        if (!credential.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return encryptionService.decrypt(credential.getEncryptedPassword());
    }

    public List<CredentialDto> searchCredentials(User user, String query) {
        return credentialRepository.findByUserAndAccountNameContainingIgnoreCase(user, query).stream()
                .map(credentialMapper::toDto)
                .collect(Collectors.toList());
    }
}

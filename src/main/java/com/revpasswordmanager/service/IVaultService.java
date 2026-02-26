package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import java.util.List;

public interface IVaultService {
    List<CredentialDto> getCredentialsByUser(User user);

    CredentialDto addCredential(User user, CredentialDto credentialDto) throws Exception;

    CredentialDto updateCredential(CredentialDto credentialDto) throws Exception;

    void deleteCredential(Long id, User user);

    String revealPassword(Long id, User user) throws Exception;

    List<CredentialDto> searchCredentials(User user, String query);
}

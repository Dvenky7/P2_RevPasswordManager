package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.User;
import java.util.List;

public interface IVaultService {
    List<CredentialDto> getCredentialsByUser(User user);

    CredentialDto addCredential(User user, CredentialDto credentialDto);

    CredentialDto updateCredential(CredentialDto credentialDto);

    void deleteCredential(Long id, User user);

    String revealPassword(Long id, User user);

    CredentialDto getCredentialById(Long id, User user);

    List<CredentialDto> searchCredentials(User user, String query);

    List<CredentialDto> getVaultEntries(User user, String query, String category, String sortBy);

    byte[] exportVault(User user) throws Exception;

    void importVault(User user, byte[] data) throws Exception;
}

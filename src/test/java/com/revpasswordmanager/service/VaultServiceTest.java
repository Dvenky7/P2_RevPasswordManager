package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.CredentialDto;
import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.exception.CredentialNotFoundException;
import com.revpasswordmanager.mapper.CredentialMapper;
import com.revpasswordmanager.repository.ICredentialRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class VaultServiceTest {

    @Mock
    private ICredentialRepository credentialRepository;

    @Mock
    private CredentialMapper credentialMapper;

    @InjectMocks
    private VaultServiceImpl vaultService;

    private User user;
    private Credential credential;
    private CredentialDto credentialDto;

    @Before
    public void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        credentialDto = new CredentialDto();
        credentialDto.setId(1L);
        credentialDto.setAccountName("Google");
        credentialDto.setPassword("secret123");

        credential = new Credential();
        credential.setId(1L);
        credential.setUser(user);
        credential.setAccountName("Google");

        ReflectionTestUtils.setField(vaultService, "secretKey", "testSecretKey12345");
    }

    @Test
    public void testAddCredential_Success() {
        when(credentialMapper.toEntity(any())).thenReturn(credential);
        when(credentialRepository.save(any())).thenReturn(credential);
        when(credentialMapper.toDto(any())).thenReturn(credentialDto);

        CredentialDto result = vaultService.addCredential(user, credentialDto);

        assertNotNull(result);
        assertEquals("Google", result.getAccountName());
        verify(credentialRepository, times(1)).save(any());
    }

    @Test(expected = CredentialNotFoundException.class)
    public void testUpdateCredential_NotFound() {
        when(credentialRepository.findById(1L)).thenReturn(Optional.empty());
        vaultService.updateCredential(credentialDto);
    }

    @Test
    public void testDeleteCredential_Success() {
        when(credentialRepository.findById(1L)).thenReturn(Optional.of(credential));

        vaultService.deleteCredential(1L, user);

        verify(credentialRepository, times(1)).delete(credential);
    }

    @Test
    public void testExportVault_Success() throws Exception {
        java.util.List<com.revpasswordmanager.entity.Credential> credentials = java.util.List.of(credential);
        when(credentialRepository.findByUser(user)).thenReturn(credentials);
        when(credentialMapper.toDto(credential)).thenReturn(credentialDto);

        byte[] exported = vaultService.exportVault(user);
        assertNotNull(exported);
    }

    @Test
    public void testImportVault_Success() throws Exception {
        // Prepare some mock data
        java.util.List<CredentialDto> dtos = java.util.List.of(credentialDto);
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        String json = mapper.writeValueAsString(dtos);
        String encrypted = com.revpasswordmanager.util.CryptoUtil.encrypt(json, "testSecretKey12345");
        byte[] data = encrypted.getBytes();

        when(credentialMapper.toEntity(any())).thenReturn(credential);
        when(credentialRepository.save(any())).thenReturn(credential);
        when(credentialMapper.toDto(any())).thenReturn(credentialDto);

        vaultService.importVault(user, data);

        verify(credentialRepository, atLeastOnce()).save(any());
    }
}

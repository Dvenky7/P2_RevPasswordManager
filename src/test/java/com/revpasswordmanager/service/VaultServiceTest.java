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
}

package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.exception.DuplicateUserException;
import com.revpasswordmanager.exception.InvalidCredentialsException;
import com.revpasswordmanager.exception.UserNotFoundException;
import com.revpasswordmanager.mapper.UserMapper;
import com.revpasswordmanager.repository.IUserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private IUserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private UserRegistrationDto registrationDto;
    private User user;

    @Before
    public void setUp() {
        registrationDto = new UserRegistrationDto();
        registrationDto.setUsername("testuser");
        registrationDto.setEmail("test@example.com");
        registrationDto.setMasterPassword("password123");

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setMasterPasswordHash("hashedPassword");
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
    }

    @Test
    public void testRegisterUser_Success() {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.registerUser(registrationDto);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository, times(1)).save(any());
    }

    @Test(expected = DuplicateUserException.class)
    public void testRegisterUser_DuplicateUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        userService.registerUser(registrationDto);
    }

    @Test
    public void testIncrementFailedAttempts_Locking() {
        User lockedUser = new User();
        lockedUser.setUsername("testuser");
        lockedUser.setFailedAttempts(4);
        lockedUser.setAccountLocked(false);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(lockedUser));

        userService.incrementFailedAttempts("testuser");

        assertEquals(Integer.valueOf(5), lockedUser.getFailedAttempts());
        assertTrue(lockedUser.getAccountLocked());
        verify(userRepository, times(1)).save(lockedUser);
    }

    @Test(expected = InvalidCredentialsException.class)
    public void testUpdateMasterPassword_InvalidOldPassword() {
        user.setMasterPasswordHash("correctHash");
        when(passwordEncoder.matches("wrongPassword", "correctHash")).thenReturn(false);

        userService.updateMasterPassword(user, "wrongPassword", "newPassword");
    }

    @Test(expected = UserNotFoundException.class)
    public void testResetPassword_UserNotFound() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        userService.resetPassword("nonexistent", "newPass");
    }
}

package com.revpasswordmanager.service;

import com.revpasswordmanager.dto.UserRegistrationDto;
import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.exception.DuplicateUserException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceImplTest {

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
        registrationDto.setName("Test User");
        registrationDto.setMobile("1234567890");
        registrationDto.setSecurityQuestion1("Q1");
        registrationDto.setSecurityAnswer1("A1");
        registrationDto.setSecurityQuestion2("Q2");
        registrationDto.setSecurityAnswer2("A2");
        registrationDto.setSecurityQuestion3("Q3");
        registrationDto.setSecurityAnswer3("A3");

        user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
    }

    @Test
    public void testRegisterUser_Success() {
        // Given
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userMapper.toEntity(any())).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any())).thenReturn(user);

        // When
        User savedUser = userService.registerUser(registrationDto);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser");
        assertThat(savedUser.isEnabled()).isFalse();
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    public void testRegisterUser_DuplicateUsername() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Then
        assertThatThrownBy(() -> userService.registerUser(registrationDto))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    public void testFindByUsername_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // When
        Optional<User> found = userService.findByUsername("testuser");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }

    @Test
    public void testResetPassword_Success() {
        // Given
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");

        // When
        userService.resetPassword("testuser", "newPass");

        // Then
        assertThat(user.getMasterPasswordHash()).isEqualTo("hashedNewPass");
        verify(userRepository, times(1)).save(user);
    }
}

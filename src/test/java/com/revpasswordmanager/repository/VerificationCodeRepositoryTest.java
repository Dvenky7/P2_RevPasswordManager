package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.entity.VerificationCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class VerificationCodeRepositoryTest {

    @Autowired
    private IVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private IUserRepository userRepository;

    @Test
    public void testSaveAndRetrieveVerificationCode() {
        // Given
        User user = new User();
        user.setUsername("testuser_repo");
        user.setEmail("repo@test.com");
        user.setName("Repo Test");
        user.setMasterPasswordHash("hashed");
        user.setTwoFactorEnabled(false);
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        User savedUser = userRepository.save(user);

        VerificationCode code = new VerificationCode();
        code.setUser(savedUser);
        code.setCode("123456");
        code.setPurpose("2FA");
        code.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        code.setIsUsed(false);
        code.setFailedAttempts(0);

        // When
        VerificationCode savedCode = verificationCodeRepository.save(code);

        // Then
        assertThat(savedCode.getId()).isNotNull();

        Optional<VerificationCode> found = verificationCodeRepository
                .findByUserAndCodeAndPurposeAndIsUsedFalse(savedUser, "123456", "2FA");

        assertThat(found).isPresent();
        assertThat(found.get().getCode()).isEqualTo("123456");
    }

    @Test
    public void testUserLookupByUsername() {
        // Given
        User user = new User();
        user.setUsername("lookup_test");
        user.setEmail("lookup@test.com");
        user.setName("Lookup Test");
        user.setMasterPasswordHash("hashed");
        user.setTwoFactorEnabled(false);
        user.setAccountLocked(false);
        user.setFailedAttempts(0);
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByUsername("lookup_test");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("lookup@test.com");
    }
}

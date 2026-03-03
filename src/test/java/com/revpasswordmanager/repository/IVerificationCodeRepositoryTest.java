package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.entity.VerificationCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class IVerificationCodeRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IVerificationCodeRepository verificationCodeRepository;

    @Test
    public void testFindByUserAndCodeAndPurposeAndIsUsedFalse_Success() {
        // Given
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setMasterPasswordHash("hash");
        user = entityManager.persist(user);

        VerificationCode code = new VerificationCode();
        code.setUser(user);
        code.setCode("123456");
        code.setPurpose("2FA");
        code.setIsUsed(false);
        code.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        code.setFailedAttempts(0);
        entityManager.persist(code);
        entityManager.flush();

        // When
        Optional<VerificationCode> result = verificationCodeRepository
                .findByUserAndCodeAndPurposeAndIsUsedFalse(user, "123456", "2FA");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("123456");
    }

    @Test
    public void testFindByUserAndCodeAndPurposeAndIsUsedFalse_NotFound() {
        // Given
        User user = new User();
        user.setUsername("testuser2");
        user.setEmail("test2@example.com");
        user.setMasterPasswordHash("hash");
        user = entityManager.persist(user);

        // When
        Optional<VerificationCode> result = verificationCodeRepository
                .findByUserAndCodeAndPurposeAndIsUsedFalse(user, "999999", "2FA");

        // Then
        assertThat(result).isNotPresent();
    }
}

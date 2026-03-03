package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class IUserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IUserRepository userRepository;

    @Test
    public void testFindByUsername_Success() {
        // Given
        User user = new User();
        user.setUsername("john_doe");
        user.setEmail("john@example.com");
        user.setMasterPasswordHash("hashed_password");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findByUsername("john_doe");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    public void testFindByEmail_Success() {
        // Given
        User user = new User();
        user.setUsername("jane_doe");
        user.setEmail("jane@example.com");
        user.setMasterPasswordHash("hashed_password");
        entityManager.persist(user);
        entityManager.flush();

        // When
        Optional<User> result = userRepository.findByEmail("jane@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("jane_doe");
    }
}

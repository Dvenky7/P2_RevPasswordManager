package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IUserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u LEFT JOIN FETCH u.securityQuestions WHERE u.username = :username")
    Optional<User> findByUsernameWithSecurityQuestions(String username);

    Optional<User> findByEmail(String email);
}

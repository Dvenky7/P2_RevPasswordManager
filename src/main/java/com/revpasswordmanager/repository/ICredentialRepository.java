package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICredentialRepository extends JpaRepository<Credential, Long> {
    List<Credential> findByUser(User user);

    List<Credential> findByUserAndAccountNameContainingIgnoreCase(User user, String accountName);
}


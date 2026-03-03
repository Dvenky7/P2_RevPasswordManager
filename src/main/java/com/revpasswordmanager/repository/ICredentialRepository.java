package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.Credential;
import com.revpasswordmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ICredentialRepository extends JpaRepository<Credential, Long> {
    List<Credential> findByUser(User user);

    @Query("SELECT c FROM Credential c WHERE c.user = :user AND (" +
            "LOWER(c.accountName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.url) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(c.username) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Credential> searchVault(@Param("user") User user, @Param("query") String query);

    List<Credential> findByUserAndCategory(User user, String category);
}

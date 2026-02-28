package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.User;
import com.revpasswordmanager.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IVerificationCodeRepository extends JpaRepository<VerificationCode, Long> {
    Optional<VerificationCode> findByUserAndCodeAndIsUsedFalse(User user, String code);
}



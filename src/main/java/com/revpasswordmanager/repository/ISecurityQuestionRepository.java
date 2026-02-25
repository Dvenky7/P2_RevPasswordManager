package com.revpasswordmanager.repository;

import com.revpasswordmanager.entity.SecurityQuestion;
import com.revpasswordmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ISecurityQuestionRepository extends JpaRepository<SecurityQuestion, Long> {
    List<SecurityQuestion> findByUser(User user);
}


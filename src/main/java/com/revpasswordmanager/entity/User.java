package com.revpasswordmanager.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "master_password_hash", nullable = false, length = 255)
    private String masterPasswordHash;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Credential> credentials;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SecurityQuestion> securityQuestions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<VerificationCode> verificationCodes;
}

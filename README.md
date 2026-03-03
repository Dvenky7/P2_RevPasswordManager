# RevVault - Secure Password Manager

RevVault is a professional, full-stack password management solution built with Spring Boot and Thymeleaf. It features AES-256 vault encryption, two-factor authentication, and a comprehensive security audit system.

## Features
- **Secure Vault**: Encrypted storage for all your credentials.
- **2FA Protection**: Extra layer of security via email-based OTP.
- **Security Audit**: Identifies weak, reused, and old passwords.
- **Password Generator**: High-entropy password creation with vault integration.
- **Export/Import**: Secure, encrypted backups of your vault.

## Tech Stack
- **Backend**: Java 17, Spring Boot 3.x, Spring Security, Spring Data JPA.
- **Frontend**: Thymeleaf, Bootstrap 5.3, Vanilla CSS/JS.
- **Database**: H2 (Development) / Oracle/MySQL (Production ready).
- **Security**: AES-256 (Vault), BCrypt (Passwords), Log4j2 (Audit Logging).

## Security Implementation
- **Vault Encryption**: Credentials are encrypted using AES-256. The secret key is managed securely via application properties.
- **Master Password**: Never stored in plain text. Hashed using BCrypt.
- **Database Security**: All sensitive fields (passwords, 2FA codes, security answers) are either hashed or encrypted.

## Setup Instructions
1. Clone the repository.
2. Configure `application.properties` with your mail server and secret key.
3. Run `mvn spring-boot:run`.
4. Access at `http://localhost:8081`.

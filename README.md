# RevPasswordManager - Secure Password Vault

A full-stack monolithic web application to securely store and manage passwords.

## Tech Stack
- **Backend**: Java 17, Spring Boot 3, Spring Data JPA, Spring Security
- **Database**: Oracle DB
- **Frontend**: Thymeleaf, HTML5, CSS3 (Bootstrap 5), JavaScript
- **Security**: AES-128 Encryption, BCrypt Hashing

## Prerequisites
- Java 17 or higher
- Maven installed (or use included `mvnw`)
- Running Oracle DB instance (Credentials: `revpass/revpass` on `localhost:1521/FREEPDB1`)

## Getting Started

1. **Configure Database**:
   Verify the credentials in `src/main/resources/application.properties`.

2. **Run the Application**:
   Open a terminal in the project root and run:
   ```bash
   mvn spring-boot:run
   ```
   (Alternatively, use `.\mvnw.cmd spring-boot:run` on Windows)

3. **Access the Web UI**:
   - **Register**: [http://localhost:8080/register](http://localhost:8080/register)
   - **Login**: [http://localhost:8080/login](http://localhost:8080/login)
   - **Dashboard**: Access after logging in to manage your vault.

## Core Features
- **User Authentication**: Secure registration and login.
- **Encrypted Vault**: Store your passwords with AES-128 encryption.
- **Password Generator**: Create strong, customizable passwords with a strength indicator.
- **Responsive Design**: Modern UI that works across various screen sizes.

## Project Structure
- `com.revpasswordmanager.entity`: JPA Entities based on the ER diagram.
- `com.revpasswordmanager.repository`: Data access layer.
- `com.revpasswordmanager.service`: Business logic (Vault, Encryption, Security).
- `com.revpasswordmanager.controller`: MVC Controllers for UI navigation.
- `src/main/resources/templates`: Thymeleaf templates for the UI.

package com.revpasswordmanager.exception;

import com.revpasswordmanager.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public Object handleUserNotFound(UserNotFoundException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.NOT_FOUND, request, model);
    }

    @ExceptionHandler(AccountLockedException.class)
    public Object handleAccountLocked(AccountLockedException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.FORBIDDEN, request, model);
    }

    @ExceptionHandler(DuplicateUserException.class)
    public Object handleDuplicateUser(DuplicateUserException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.CONFLICT, request, model);
    }

    @ExceptionHandler(CredentialNotFoundException.class)
    public Object handleCredentialNotFound(CredentialNotFoundException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.NOT_FOUND, request, model);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public Object handleInvalidVerificationCode(InvalidVerificationCodeException ex, HttpServletRequest request,
            Model model) {
        return handleException(ex, HttpStatus.BAD_REQUEST, request, model);
    }

    @ExceptionHandler(InvalidMasterPasswordException.class)
    public Object handleInvalidMasterPassword(InvalidMasterPasswordException ex, HttpServletRequest request,
            Model model) {
        return handleException(ex, HttpStatus.UNAUTHORIZED, request, model);
    }

    @ExceptionHandler(EncryptionException.class)
    public Object handleEncryptionError(EncryptionException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, model);
    }

    @ExceptionHandler(InvalidBackupFileException.class)
    public Object handleInvalidBackupFile(InvalidBackupFileException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.BAD_REQUEST, request, model);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public Object handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest request, Model model) {
        return handleException(ex, HttpStatus.UNAUTHORIZED, request, model);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request,
            Model model) {
        log.warn("Validation failed: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        if (isApiRequest(request)) {
            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.BAD_REQUEST.value())
                    .error("Validation Error")
                    .message("The request contains invalid data")
                    .path(request.getRequestURI())
                    .validationErrors(errors)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } else {
            model.addAttribute("error", "Validation failed. Please check the form.");
            model.addAttribute("validationErrors", errors);
            return "error";
        }
    }

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex, HttpServletRequest request, Model model) {
        log.error("Unexpected error occurred: ", ex);
        return handleException(ex, HttpStatus.INTERNAL_SERVER_ERROR, request, model);
    }

    private Object handleException(Exception ex, HttpStatus status, HttpServletRequest request, Model model) {
        if (status.is5xxServerError()) {
            log.error("Error: ", ex);
        } else {
            log.warn("Request warning: {} - {}", status, ex.getMessage());
        }

        if (isApiRequest(request)) {
            ErrorResponse response = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(status.value())
                    .error(status.getReasonPhrase())
                    .message(ex.getMessage())
                    .path(request.getRequestURI())
                    .build();
            return new ResponseEntity<>(response, status);
        } else {
            model.addAttribute("status", status.value());
            model.addAttribute("error", status.getReasonPhrase());
            model.addAttribute("message", ex.getMessage());
            return "error";
        }
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String path = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        return path.startsWith("/api") || (acceptHeader != null && acceptHeader.contains("application/json"));
    }
}

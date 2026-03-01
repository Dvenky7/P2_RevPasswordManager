package com.revpasswordmanager.exception;

public class InvalidMasterPasswordException extends RuntimeException {
    public InvalidMasterPasswordException(String message) {
        super(message);
    }

    public InvalidMasterPasswordException(String message, Throwable cause) {
        super(message, cause);
    }
}

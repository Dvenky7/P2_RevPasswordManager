package com.revpasswordmanager.exception;

public class InvalidBackupFileException extends RuntimeException {
    public InvalidBackupFileException(String message) {
        super(message);
    }

    public InvalidBackupFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

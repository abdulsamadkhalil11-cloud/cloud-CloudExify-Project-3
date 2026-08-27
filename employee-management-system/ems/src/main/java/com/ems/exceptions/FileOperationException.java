package com.ems.exceptions;

/** Thrown when saving, loading, backing up, or restoring data fails. */
public class FileOperationException extends EMSException {
    public FileOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileOperationException(String message) {
        super(message);
    }
}

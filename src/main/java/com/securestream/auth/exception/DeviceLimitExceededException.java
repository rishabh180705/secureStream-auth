package com.securestream.auth.exception;

public class DeviceLimitExceededException extends RuntimeException {
    public DeviceLimitExceededException(String message) {
        super(message);
    }
}

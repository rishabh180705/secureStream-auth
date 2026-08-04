package com.securestream.auth.exception;

public class UserDoes_notExit extends RuntimeException {
    public UserDoes_notExit(String message) {
        super(message);
    }
}

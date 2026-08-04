package com.securestream.auth.util;


import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
    public class HashService {
    private final PasswordEncoder passwordEncoder;
        public String sha256(String value) {
            passwordEncoder.encode(value);
            return value;
        }
    }


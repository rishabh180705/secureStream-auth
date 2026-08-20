package com.securestream.auth.service;

import com.securestream.auth.entity.PasswordResetToken;
import com.securestream.auth.entity.User;
import com.securestream.auth.repository.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createToken(User user) {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        tokenRepository.deleteByUser(user);

        PasswordResetToken resetToken =
                PasswordResetToken.builder()
                        .user(user)
                        .token(token)
                        .expiresAt(
                                LocalDateTime.now().plusMinutes(10)
                        )
                        .used(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        tokenRepository.save(resetToken);

        return token;
    }
}
package com.securestream.auth.service;

import com.securestream.auth.entity.EmailVerificationToken;
import com.securestream.auth.entity.User;
import com.securestream.auth.repository.EmailVerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
//    private final HashService hashService;

    private final SecureRandom secureRandom = new SecureRandom();

    public String createToken(User user) {

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String rawToken = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

//        String tokenHash = hashService.sha256(rawToken);

        tokenRepository.deleteByUser(user);

        EmailVerificationToken token =
                EmailVerificationToken.builder()
                        .user(user)
                        .token(rawToken)
                        .expiresAt(
                                LocalDateTime.now().plusHours(24)
                        )
                        .used(false)
                        .createdAt(LocalDateTime.now())
                        .build();

        tokenRepository.save(token);

        return rawToken;
    }
}
package com.securestream.auth.service;

import com.securestream.auth.entity.User;
import com.securestream.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountLockService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final long LOCK_DURATION_MINUTES = 15;

    private final UserRepository userRepository;

    public void handleFailedLogin(User user) {

        int attempts = user.getFailedLoginAttempts() + 1;

        user.setFailedLoginAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {

            user.setAccountNonLocked(false);

            user.setLockedUntil(
                    LocalDateTime.now()
                            .plusMinutes(LOCK_DURATION_MINUTES)
            );
        }

        userRepository.save(user);
    }

    public void handleSuccessfulLogin(User user) {

        user.setFailedLoginAttempts(0);

        user.setAccountNonLocked(true);

        user.setLockedUntil(null);

        userRepository.save(user);
    }

    public boolean isLocked(User user) {

        if (user.isAccountNonLocked()) {
            return false;
        }

        if (user.getLockedUntil() != null &&
                LocalDateTime.now()
                        .isAfter(user.getLockedUntil())) {

            user.setAccountNonLocked(true);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);

            return false;
        }

        return true;
    }
}
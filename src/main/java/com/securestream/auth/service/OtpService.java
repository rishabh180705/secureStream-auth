package com.securestream.auth.service;

import com.securestream.auth.entity.PasswordResetOtp;
import com.securestream.auth.entity.User;
import com.securestream.auth.exception.InvalidCredentialsException;
import com.securestream.auth.repository.PasswordResetOtpRepository;
import com.securestream.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetOtpRepository otpRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    private final UserRepository userRepository;
    private final  PasswordResetTokenService  passwordResetTokenService;

    public String generateOtp(User user) {

        String otp = String.format(
                "%06d", secureRandom.nextInt(1_000_000)
        );

        otpRepository.deleteByUser(user);

        PasswordResetOtp resetOtp = PasswordResetOtp.builder()
                .user(user)
                .otpHash(passwordEncoder.encode(otp))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .attemptCount(0)
                .verified(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpRepository.save(resetOtp);

        return otp;
    }

    public String verifyOtp(String email, String enteredOtp) {
        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new InvalidCredentialsException("Invalid OTP"));

        PasswordResetOtp resetOtp =
                otpRepository.findTopByUserOrderByCreatedAtDesc(user)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid OTP"
                                ));

        if (resetOtp.isVerified()) {
            throw new InvalidCredentialsException("OTP already used");
        }

        if (resetOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new InvalidCredentialsException("OTP expired");
        }

        if (resetOtp.getAttemptCount() >= 5) {
            throw new InvalidCredentialsException(
                    "Too many OTP attempts"
            );
        }

        if (!passwordEncoder.matches(
                enteredOtp,
                resetOtp.getOtpHash())) {

            resetOtp.setAttemptCount(
                    resetOtp.getAttemptCount() + 1
            );

            otpRepository.save(resetOtp);

            throw new InvalidCredentialsException(
                    "Invalid OTP"
            );
        }

        resetOtp.setVerified(true);
        otpRepository.save(resetOtp);
        String resetToken = passwordResetTokenService.createToken(user);

        return resetToken;
    }
}
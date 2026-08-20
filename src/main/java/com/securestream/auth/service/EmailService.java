package com.securestream.auth.service;

public interface EmailService {

    void sendPasswordResetOtp(String email, String otp);

    void sendVerificationEmail(
            String email,
            String verificationToken
    );
}
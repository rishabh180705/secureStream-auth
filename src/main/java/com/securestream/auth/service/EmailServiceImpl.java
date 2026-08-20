package com.securestream.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendPasswordResetOtp(
            String email,
            String otp) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(email);
        message.setSubject("SecureStream - Password Reset OTP");

        message.setText(
                "Your password reset OTP is: " + otp
                        + "\n\n"
                        + "This OTP expires in 5 minutes."
                        + "\n\n"
                        + "If you did not request this, "
                        + "please ignore this email."
        );

        mailSender.send(message);
    }

    @Override
    public void sendVerificationEmail(
            String email,
            String verificationToken) {

        // We'll implement this when we do
        // Email Verification.
    }
}
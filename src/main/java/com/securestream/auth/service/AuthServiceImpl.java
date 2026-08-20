package com.securestream.auth.service;


import java.time.LocalDateTime;
import java.util.Optional;

import com.securestream.auth.dto.*;

import com.securestream.auth.entity.*;
import com.securestream.auth.exception.DeviceLimitExceededException;
import com.securestream.auth.exception.EmailAlreadyExistsException;
import com.securestream.auth.exception.InvalidCredentialsException;
import com.securestream.auth.exception.UserDoes_notExit;
import com.securestream.auth.repository.PasswordResetTokenRepository;
import com.securestream.auth.repository.RefreshTokenRepository;
import com.securestream.auth.security.CustomUserDetails;
import com.securestream.auth.security.JwtService;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.securestream.auth.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;


@Data
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final  OtpService otpService;
    private final PasswordResetTokenRepository tokenRepository;
    private final AccountLockService accountLockService;
    private final EmailService emailService;

    @Override
    public String register(RegisterRequest request) {

        Optional<User> existingUser = userRepository.findByEmailIgnoreCase(request.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists");
        }
        User user = User.builder()
                .email(request.getEmail().toLowerCase())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .subscription(Subscription.BASIC)
                .role(Role.USER)
                .accountNonLocked(true)
                .createdAt(LocalDateTime.now())
                .enabled(true)
                .updatedAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
        return "User Registered Successfully";

    }

    @Override
    public AuthResponse login(LoginRequest request, String ip, String userAgent) {

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UserDoes_notExit("User doesn't exist"));

        if (accountLockService.isLocked(user)) {
            throw new InvalidCredentialsException(
                    "Account is temporarily locked"
            );
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            accountLockService.handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid email or password");
        }

        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }
        if (!user.isAccountNonLocked()) {
            throw new InvalidCredentialsException("Account is locked");
        }
        RefreshToken existingDevice =
                refreshTokenRepository.findByUserAndDeviceIdAndRevokedFalse(
                        user,
                        request.getDeviceId()
                ).orElse(null);

        if (existingDevice != null) {
            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            String newRefreshToken = jwtService.generateRefreshToken(customUserDetails);
            String AccessToken = jwtService.generateAccessToken(customUserDetails);
            existingDevice.setRefreshToken(newRefreshToken);

            existingDevice.setLastUsedAt(LocalDateTime.now());

            existingDevice.setExpiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiryMs() / 1000));

            refreshTokenRepository.save(existingDevice);
            return AuthResponse.builder()
                    .accessToken(AccessToken)
                    .refreshToken(newRefreshToken)
                    .tokenType("Bearer")
                    .accessTokenExpiresInSeconds(jwtService.getAccessTokenExpirySeconds())
//                  .sessionId(sessionId)
                    .build();
        }
        long deviceMaxCount = user.getSubscription() == Subscription.BASIC ? 1 : 3;

        long activeDevice = refreshTokenRepository.countByUserAndRevokedFalse(user);
        if (activeDevice >= deviceMaxCount) {

            throw new DeviceLimitExceededException(
                    "Maximum active devices reached"
            );
        }

        accountLockService.handleSuccessfulLogin(user);
        return issueTokensForNewSession(user, ip, userAgent, request.getDeviceId());

    }

    @Override
    public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        if (jwtService.isTokenValid(refreshToken) && jwtService.isRefreshToken(refreshToken)) {

            RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshToken).
                    orElseThrow(() -> new InvalidCredentialsException("Invalid refresh token"));
            if (token.isRevoked()) {
                throw new InvalidCredentialsException("Refresh token revoked");
            }
            if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                throw new InvalidCredentialsException("Refresh token expired");
            }
            String email = jwtService.extractUserId(refreshToken);

            User user = userRepository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new UserDoes_notExit("User doesn't exist"));
            CustomUserDetails customUserDetails = new CustomUserDetails(user);
            String accessToken = jwtService.generateAccessToken(customUserDetails);
            String type = "Bearer";
            long ttl = jwtService.getAccessTokenExpirySeconds();

            token.setLastUsedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);

            return RefreshTokenResponse.builder()
                    .accessToken(accessToken)
                    .tokenType(type)
                    .expiresIn(ttl)
                    .build();

        } else {
            throw new InvalidCredentialsException("Invalid refresh token");
        }

    }


    private AuthResponse issueTokensForNewSession(User user, String ip, String userAgent, String deviceId) {
        CustomUserDetails principal = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        RefreshToken token = RefreshToken.builder()
                .refreshToken(refreshToken)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiryMs() / 1000))
                .user(user)
                .deviceId(deviceId)
                .ipAddress(ip)
                .userAgent(userAgent)
                .build();
        refreshTokenRepository.save(token);


        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresInSeconds(jwtService.getAccessTokenExpirySeconds())
//              .sessionId(sessionId)
                .build();

    }

    @Override
    public void logout(LogoutRequest request) {
        if ((!jwtService.isTokenValid(request.getRefreshToken())) ||
                (!jwtService.isRefreshToken(request.getRefreshToken()))) {
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        RefreshToken token = refreshTokenRepository.findByRefreshToken(
                request.getRefreshToken()).orElseThrow(() ->
                new InvalidCredentialsException("Refresh token not found"));

        if (token.isRevoked()) {
            return;
        }
        token.setRevokedAt(LocalDateTime.now());
        token.setRevoked(true);

        refreshTokenRepository.save(token);
//        refreshTokenRepository.setRevokedTrueByRefreshToken(request.getRefreshToken());
    }

    @Override
    public void logoutFromAllSessions(LogoutRequest request) {

        if (!jwtService.isTokenValid(request.getRefreshToken())||
                !jwtService.isRefreshToken(request.getRefreshToken())){
            throw new InvalidCredentialsException("Invalid refresh token");
        }
        RefreshToken token = refreshTokenRepository.findByRefreshToken(
                request.getRefreshToken()).orElseThrow(() ->
                new InvalidCredentialsException("Refresh token not found"));

//         String email=jwtService.extractUserId(request.getRefreshToken());
//
//        User user= userRepository.findByEmailIgnoreCase(email).orElseThrow(
//                () -> new UserDoes_notExit("User doesn't exist")
//        );
        if (token.isRevoked()) {
            return;
        }
        refreshTokenRepository.revokeAllByUser(token.getUser());

    }


    @Override
    public void forgotPassword(ForgotPasswordRequest request) {

        Optional<User> userOptional =
                userRepository.findByEmailIgnoreCase(
                        request.getEmail()
                );

        if (userOptional.isEmpty()) {
            return;
        }

        User user = userOptional.get();


        String otp = otpService.generateOtp(user);

        emailService.sendPasswordResetOtp(
                user.getEmail(),
                otp
        );
    }

    @Transactional
    @Override
    public void resetPassword(ResetPasswordRequest request) {

        String token=request.getResetToken();

        PasswordResetToken resetToken =
                tokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new InvalidCredentialsException(
                                        "Invalid reset token"
                                ));

        if (resetToken.isUsed()) {
            throw new InvalidCredentialsException(
                    "Reset token already used"
            );
        }

        if (resetToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            throw new InvalidCredentialsException(
                    "Reset token expired"
            );
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        refreshTokenRepository.revokeAllByUser(user);
    }

}

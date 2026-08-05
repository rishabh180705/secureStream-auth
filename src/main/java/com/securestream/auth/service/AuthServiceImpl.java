package com.securestream.auth.service;


import java.time.LocalDateTime;
import java.util.Optional;

import com.securestream.auth.dto.AuthResponse;
import com.securestream.auth.dto.LoginRequest;

import com.securestream.auth.entity.RefreshToken;
import com.securestream.auth.entity.Subscription;
import com.securestream.auth.exception.EmailAlreadyExistsException;
import com.securestream.auth.exception.InvalidCredentialsException;
import com.securestream.auth.exception.UserDoes_notExit;
import com.securestream.auth.repository.RefreshTokenRepository;
import com.securestream.auth.security.CustomUserDetails;
import com.securestream.auth.security.JwtService;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.securestream.auth.dto.RegisterRequest;
import com.securestream.auth.entity.Role;
import com.securestream.auth.entity.User;
import com.securestream.auth.repository.UserRepository;


@Data
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

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
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmailIgnoreCase(request.getEmail())
                .orElseThrow(() -> new UserDoes_notExit("User doesn't exist"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isEnabled()) {
            throw new InvalidCredentialsException("Account is disabled");
        }
        if (!user.isAccountNonLocked()) {
            throw new InvalidCredentialsException("Account is locked");
        }

        return issueTokensForNewSession(user);

    }

    private AuthResponse issueTokensForNewSession(User user) {
        CustomUserDetails principal = new CustomUserDetails(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = jwtService.generateRefreshToken(principal);

        RefreshToken token = RefreshToken.builder()
                .refreshToken(refreshToken)
                .revoked(false)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtService.getRefreshTokenExpiryMs() / 1000))
                .user(user)
                .build();
        refreshTokenRepository.save(token);


        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .accessTokenExpiresInSeconds(jwtService.getAccessTokenExpirySeconds())
//                .sessionId(sessionId)
                .build();

    }


}

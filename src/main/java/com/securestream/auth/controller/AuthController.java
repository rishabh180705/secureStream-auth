package com.securestream.auth.controller;
import com.securestream.auth.dto.*;
import com.securestream.auth.security.ClientRequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.securestream.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;


    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequest request){

        String response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }


    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest){

            String ip = ClientRequestUtils.resolveClientIp(httpRequest);
            String userAgent = ClientRequestUtils.resolveUserAgent(httpRequest);
            System.out.println("device"+ip + " " + userAgent);
        return ResponseEntity.ok(
                authService.login(request,ip,userAgent)
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestBody LogoutRequest request){

        authService.logout(request);

        return ResponseEntity.ok("Logged out successfully");
    }
    @PostMapping("/logoutAll")
    public ResponseEntity<String> logoutFromAll(
            @RequestBody LogoutRequest request){

        authService.logoutFromAllSessions(request);

        return ResponseEntity.ok("Logged out successfully from all active sessions");
    }


}


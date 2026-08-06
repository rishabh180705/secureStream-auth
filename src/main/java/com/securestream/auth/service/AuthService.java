package com.securestream.auth.service;

import com.securestream.auth.dto.*;
import jakarta.validation.Valid;


public interface AuthService {

    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request,String ip,String userAgent);
    RefreshTokenResponse refreshToken(RefreshTokenRequest request);
}
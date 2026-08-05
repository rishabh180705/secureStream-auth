package com.securestream.auth.service;

import com.securestream.auth.dto.AuthResponse;
import com.securestream.auth.dto.LoginRequest;
import com.securestream.auth.dto.RegisterRequest;


public interface AuthService {

    String register(RegisterRequest request);

    AuthResponse login(LoginRequest request);





}
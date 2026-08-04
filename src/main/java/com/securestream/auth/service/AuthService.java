package com.securestream.auth.service;

import com.securestream.auth.dto.LoginRequest;
import com.securestream.auth.dto.LoginResponse;
import com.securestream.auth.dto.RegisterRequest;


public interface AuthService {

    String register(RegisterRequest request);

    LoginResponse login(LoginRequest request);





}
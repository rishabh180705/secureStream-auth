package com.securestream.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
public class RefreshTokenRequest {

    @NotBlank(message = "It can't be null")
    private String refreshToken;
}
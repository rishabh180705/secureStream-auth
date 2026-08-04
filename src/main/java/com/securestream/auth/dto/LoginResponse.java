package com.securestream.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {


        private String accessToken;

        private String refreshToken;

     public LoginResponse(String accessToken) {
              this.accessToken = accessToken;

      }

}
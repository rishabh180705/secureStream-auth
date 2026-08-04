package com.securestream.auth.security;

//import com.securestream.auth.entity.RefreshToken;
//import com.securestream.auth.repository.RefreshTokenRepository;
//import com.securestream.auth.util.HashService;
//import lombok.AllArgsConstructor;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.Date;
//
//@Service
//@AllArgsConstructor
//public class RefreshTokenService {
//    private final RefreshTokenRepository refreshTokenRepository;
//    private final JwtService jwtService;
//    private final HashService hashService;
//
//    public String createRefreshToken(){
//
//        RefreshToken token = new RefreshToken();
//        String refreshToken = jwtService.generateRefreshToken(userDetails);
//
//        token.setRefreshToken(hashService.sha256(refreshToken));
//        token.setCreatedAt(new Date());
//        token.setExpiresAt(jwtService.extractExpiration(refreshToken));
//        refreshTokenRepository.save(token);
//        return refreshToken;
//
//    }
//
//
//
//    public boolean validateRefreshToken(){
//
//    }
//
//
//
//    public void revokeToken(String deviceId){
//
//        refreshTokenRepository.RevokeRefreshTokenTrue(deviceId);
//    }
//
//    public void revokeAllTokens(){
//
//    }
//
//}

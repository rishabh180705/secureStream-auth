package com.securestream.auth.security;

import com.securestream.auth.entity.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.*;


@Service
@Slf4j
@AllArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public enum TokenType {ACCESS, REFRESH}

    private SecretKey signingKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtProperties.getSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        return buildToken(userDetails, TokenType.ACCESS, jwtProperties.getJwtAccessExpiration());
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        return buildToken(userDetails, TokenType.REFRESH, jwtProperties.getJwtRefreshTime());
    }

    // generation of token
    private String buildToken(CustomUserDetails principal, TokenType type, long ttlMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + ttlMs);

        List<String> roles = principal.getAuthorities().stream()
                .map(Object::toString)
                .toList();

        return Jwts.builder()
                .subject(principal.getUsername())
//                .id(sessionId)
                .issuer(jwtProperties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claims(Map.of(
                        "userId",principal.getUserId(),
                        "email", principal.getUsername(),
                        "roles", roles,
                        "subscription", principal.getSubscription(),
//                        "tokenVersion", principal.getTokenVersion(),
                        "type", type.name().toLowerCase(),
                        "enabled", principal.isEnabled()
                ))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Generates a fresh, cryptographically random session id (used as the refresh token's jti).
     */
    public String newSessionId() {
        return UUID.randomUUID().toString();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Returns empty/false rather than throwing, for use in filters where we want a clean 401.
     */
    public boolean isTokenValid(String token) {

        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT expired: {}", e.getMessage());
        } catch (MalformedJwtException | SignatureException e) {
            log.warn("JWT invalid/tampered: {}", e.getMessage());
        } catch (Exception e) {
            log.warn("JWT validation error: {}", e.getMessage());
        }
        return false;
    }

    public String extractUserId(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractSessionId(String token) {
        return parseClaims(token).getId();
    }

    public String extractType(String token) {
        return parseClaims(token).get("type", String.class);
    }

    public Integer extractTokenVersion(String token) {
        return parseClaims(token).get("tokenVersion", Integer.class);
    }

    public boolean isRefreshToken(String token) {
        return TokenType.REFRESH.name().toLowerCase().equals(extractType(token));
    }

    public boolean isAccessToken(String token) {
        return TokenType.ACCESS.name().toLowerCase().equals(extractType(token));
    }

    public long getAccessTokenExpirySeconds() {
        return jwtProperties.getJwtAccessExpiration() / 1000;
    }

    public long getRefreshTokenExpiryMs() {
        return jwtProperties.getJwtRefreshTime();
    }


}

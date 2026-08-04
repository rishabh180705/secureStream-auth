package com.securestream.auth.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshTime;



    // generation of token
    public String generateToken(UserDetails userDetails) {

        Map<String,Object> claims = new HashMap<>();

        CustomUserDetails user = (CustomUserDetails) userDetails;


        claims.put("userId", user.getUsername());

        claims.put("role", user.getAuthorities());

        claims.put("enabled", user.isEnabled());
        claims.put("plan", user.getSubscription());

        return Jwts.builder()
                .claims(claims)
                .subject(user.getUsername())
                .issuedAt(new Date())
                .expiration(
                        new Date(System.currentTimeMillis()
                                + jwtExpiration)
                )
                .signWith(getSignInKey())
                .compact();
    }
    public String generateRefreshToken(){
        return UUID.randomUUID().toString();
    }
    private Key getSignInKey(){

        byte[] keyBytes =
                Decoders.BASE64.decode(secretKey);

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token){

        return extractClaim(
                token,
                Claims::getSubject
        );
    }

    public <T> T extractClaim(
            String token,
            Function<Claims,T> resolver
    ){

        final Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token){

        return Jwts.parser()
                 .verifyWith((SecretKey) getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    public boolean isTokenValid(
            String token,
            UserDetails userDetails
    ){

        final String username =
                extractUsername(token);

        return username.equals(
                userDetails.getUsername()
        )
                &&
                !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token){

        return extractExpiration(token)
                .before(new Date());
    }


    public Date extractExpiration(String token){

        return extractClaim(
                token,
                Claims::getExpiration
        );
    }

}

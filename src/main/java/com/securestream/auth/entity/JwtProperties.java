package com.securestream.auth.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@Getter
public class JwtProperties {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.access.expiration}")
    private long jwtAccessExpiration;
    @Value("${jwt.refresh-expiration}")
    private long jwtRefreshTime;
    @Value("${jwt.issuer}")
    private String issuer;
}

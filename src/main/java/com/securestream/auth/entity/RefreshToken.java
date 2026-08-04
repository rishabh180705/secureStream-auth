package com.securestream.auth.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name= "refresh_token")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String refreshToken;
    private String deviceId;
    private boolean revoked;
    private Date createdAt;
    private Date lastUsedAt;
    private Date expiresAt;
    private String ipAddress;
    private String userAgent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}

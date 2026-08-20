package com.securestream.auth.repository;

import com.securestream.auth.entity.RefreshToken;
import com.securestream.auth.entity.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    long countByUserAndRevokedFalse(User user);

    Optional<RefreshToken> findByUserAndDeviceIdAndRevokedFalse(
            User user,
            String deviceId
    );


   // void setRevokedTrueByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    @Query("""
            UPDATE RefreshToken r
            SET r.revoked = true,
                r.lastUsedAt = CURRENT_TIMESTAMP
            WHERE r.user = :user
            AND r.revoked = false
            """)
    void revokeAllByUser(User user);
}



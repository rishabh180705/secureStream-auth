package com.securestream.auth.repository;

import com.securestream.auth.entity.RefreshToken;
import com.securestream.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

//    void RevokeRefreshTokenTrue(String deviceId);
//
//    void RevokeAllRefreshTokenTrue(String email);

}



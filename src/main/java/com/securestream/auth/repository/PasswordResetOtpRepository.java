package com.securestream.auth.repository;

import com.securestream.auth.entity.PasswordResetOtp;
import com.securestream.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PasswordResetOtpRepository
        extends JpaRepository<PasswordResetOtp, UUID> {

    Optional<PasswordResetOtp>
    findTopByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
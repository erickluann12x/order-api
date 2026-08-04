package com.erick.order_api.repository;

import com.erick.order_api.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
         SELECT token
         FROM RefreshToken token
         JOIN FETCH token.user
         WHERE token.tokenHash = :tokenHash
         """)
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Modifying
    @Query("""
        UPDATE RefreshToken token
        SET token.revokedAt = :now
        WHERE token.familyId = :familyId
          AND token.revokedAt IS NULL
        """)
    int revokeFamily(
            @Param("familyId") UUID familyId,
            @Param("now") Instant now
    );

    @Modifying
    @Query("""
        DELETE FROM RefreshToken token
        WHERE token.expiresAt < :now
        """)
    int deleteExpired(
            @Param("now") Instant now
    );
}

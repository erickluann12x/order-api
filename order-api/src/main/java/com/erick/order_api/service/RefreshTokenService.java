package com.erick.order_api.service;

import com.erick.order_api.entity.RefreshToken;
import com.erick.order_api.entity.User;
import com.erick.order_api.exception.InvalidRefreshTokenException;
import com.erick.order_api.repository.RefreshTokenRepository;
import com.erick.order_api.config.AuthProperties;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final AuthProperties authProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public IssuedRefreshToken createSession(User user){
        return createToken(user, UUID.randomUUID());
    }

    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public RotatedRefreshToken rotate(String rawToken){

        validatePresence(rawToken);

        Instant now = Instant.now();
        String tokenHash = hash(rawToken);

        RefreshToken currentToken = repository
                .findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() ->
                        new InvalidRefreshTokenException(
                                "Refresh token inválido"
                        )
                );

        if (currentToken.isRevoked()) {
            repository.revokeFamily(
                    currentToken.getFamilyId(),
                    now
            );

            throw new InvalidRefreshTokenException(
                    "Refresh token já utilizado"
            );
        }

        if (currentToken.isExpired(now)) {
            currentToken.revoke(now);

            throw new InvalidRefreshTokenException(
                    "Refresh token expirado"
            );
        }

        currentToken.revoke(now);

        IssuedRefreshToken replacement =
                createToken(
                        currentToken.getUser(),
                        currentToken.getFamilyId()
                );

        return new RotatedRefreshToken(
                currentToken.getUser(),
                replacement.rawToken(),
                replacement.expiresAt()
        );
    }

    @Transactional
    public void revoke(String rawToken) {
        if (
                rawToken == null ||
                        rawToken.isBlank()
        ) {
            return;
        }

        repository
                .findByTokenHashForUpdate(
                        hash(rawToken)
                )
                .filter(token ->
                        !token.isRevoked()
                )
                .ifPresent(token ->
                        token.revoke(Instant.now())
                );
    }

    private IssuedRefreshToken createToken(
            User user,
            UUID familyId
    ) {
        Instant now = Instant.now();

        Duration lifetime = Duration.ofDays(
                authProperties
                        .getRefreshToken()
                        .getDays()
        );

        Instant expiresAt =
                now.plus(lifetime);

        String rawToken =
                generateRandomToken();

        RefreshToken entity =
                RefreshToken.builder()
                        .tokenHash(hash(rawToken))
                        .familyId(familyId)
                        .user(user)
                        .createdAt(now)
                        .expiresAt(expiresAt)
                        .build();

        repository.save(entity);

        return new IssuedRefreshToken(
                rawToken,
                expiresAt
        );
    }

    private void validatePresence(
            String rawToken
    ) {
        if (
                rawToken == null ||
                        rawToken.isBlank()
        ) {
            throw new InvalidRefreshTokenException(
                    "Refresh token ausente"
            );
        }
    }

    private String generateRandomToken() {
        byte[] bytes = new byte[64];

        secureRandom.nextBytes(bytes);

        return Base64
                .getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest
                    .getInstance("SHA-256")
                    .digest(
                            rawToken.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            return HexFormat
                    .of()
                    .formatHex(digest);

        } catch (
                NoSuchAlgorithmException exception
        ) {
            throw new IllegalStateException(
                    "SHA-256 não está disponível",
                    exception
            );
        }
    }

    public record IssuedRefreshToken(
            String rawToken,
            Instant expiresAt
    ) {
    }

    public record RotatedRefreshToken(
            User user,
            String rawToken,
            Instant expiresAt
    ) {
    }
}

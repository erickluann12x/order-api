package com.erick.order_api.config;

import com.erick.order_api.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {

    private final RefreshTokenRepository repository;

    @Scheduled(cron = "0 0 3 * * *",
            zone = "America/Fortaleza")

    @Transactional
    public void removeExpiredToken() {
        repository.deleteExpired(Instant.now());
    }
}

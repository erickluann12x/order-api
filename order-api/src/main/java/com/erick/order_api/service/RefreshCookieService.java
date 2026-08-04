package com.erick.order_api.service;

import com.erick.order_api.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RefreshCookieService {

    public static final String COOKIE_NAME =
            "refreshToken";

    private final AuthProperties authProperties;

    public ResponseCookie create(
            String rawToken
    ) {
        return baseCookie(rawToken)
                .maxAge(
                        Duration.ofDays(
                                authProperties
                                        .getRefreshToken()
                                        .getDays()
                        )
                )
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder
    baseCookie(String value) {
        return ResponseCookie
                .from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(
                        authProperties
                                .getCookie()
                                .isSecure()
                )
                .sameSite("Lax")
                .path("/auth");
    }
}

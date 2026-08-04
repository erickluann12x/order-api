package com.erick.order_api.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "security")
public class AuthProperties {

    private Jwt jwt = new Jwt();
    private RefreshTokenConfig refreshToken = new RefreshTokenConfig();
    private Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Jwt {

        private long accessTokenMinutes = 15;
    }

    @Getter
    @Setter
    public static class RefreshTokenConfig {

        private long days = 7;
    }

    @Getter
    @Setter
    public static class Cookie {

        private boolean secure = false;
    }
}


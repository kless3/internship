package com.internship.auth_service.config.property;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private TokenProperties accessToken;
    private TokenProperties refreshToken;

    @Getter
    @Setter
    public static class TokenProperties {
        private Long expiration;
    }
}

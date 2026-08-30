package nz.ac.wintec.irm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties("irm.jwt")
public record JwtProperties(String secret, Duration ttl, Duration resetReferenceTtl) {

    public JwtProperties {
        if (secret == null || secret.getBytes(java.nio.charset.StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT_SECRET must contain at least 32 bytes");
        }
        if (ttl == null || ttl.isNegative() || ttl.isZero()) {
            throw new IllegalArgumentException("irm.jwt.ttl must be positive");
        }
        if (resetReferenceTtl == null
                || resetReferenceTtl.isNegative()
                || resetReferenceTtl.isZero()
                || resetReferenceTtl.compareTo(Duration.ofHours(1)) > 0) {
            throw new IllegalArgumentException(
                    "irm.jwt.reset-reference-ttl must be positive and no longer than one hour");
        }
    }
}

package nz.ac.wintec.irm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import nz.ac.wintec.irm.config.JwtProperties;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetReferenceService {

    private static final String PURPOSE_CLAIM = "purpose";
    private static final String PASSWORD_RESET_PURPOSE = "password_reset";

    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey key;

    public PasswordResetReferenceService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String issue(String email) {
        Instant issuedAt = clock.instant();
        return Jwts.builder()
                .subject(email)
                .claim(PURPOSE_CLAIM, PASSWORD_RESET_PURPOSE)
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(issuedAt.plus(properties.resetReferenceTtl())))
                .signWith(key)
                .compact();
    }

    public ResetReference parse(String value) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(value)
                    .getPayload();
            String email = claims.getSubject();
            String purpose = claims.get(PURPOSE_CLAIM, String.class);
            String referenceId = claims.getId();
            if (email == null || email.isBlank()
                    || referenceId == null || referenceId.isBlank()
                    || !PASSWORD_RESET_PURPOSE.equals(purpose)) {
                throw new IllegalArgumentException("Password reset reference claims are invalid");
            }
            return new ResetReference(email.trim().toLowerCase(Locale.ROOT));
        } catch (JwtException exception) {
            throw new IllegalArgumentException("Password reset reference is invalid", exception);
        }
    }

    public Optional<ResetReference> parseIfValid(String value) {
        try {
            return Optional.of(parse(value));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    public record ResetReference(String email) {
    }
}

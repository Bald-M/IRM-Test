package nz.ac.wintec.irm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import nz.ac.wintec.irm.config.JwtProperties;
import nz.ac.wintec.irm.domain.ApplicationUser;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final JwtProperties properties;
    private final Clock clock;
    private final SecretKey key;

    public JwtService(JwtProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.key = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public IssuedToken issue(ApplicationUser user) {
        Instant issuedAt = clock.instant();
        Instant expiresAt = issuedAt.plus(properties.ttl());
        String token = Jwts.builder()
                .claim("id", user.getId())
                .claim("email", user.getEmail())
                .claim("type", user.role().apiValue())
                .expiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
        return new IssuedToken(token, expiresAt);
    }

    public AuthenticatedUser parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Object rawId = claims.get("id");
        if (!(rawId instanceof Number number)) {
            throw new IllegalArgumentException("JWT user id is invalid");
        }
        Integer id = number.intValue();
        String email = claims.get("email", String.class);
        String type = claims.get("type", String.class);
        return new AuthenticatedUser(id, email, nz.ac.wintec.irm.domain.Role.fromDatabase(type));
    }

    public record IssuedToken(String value, Instant expiresAt) {
    }
}

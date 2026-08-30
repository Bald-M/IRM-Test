package nz.ac.wintec.irm.security;

import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.wintec.irm.domain.ApplicationUser;
import nz.ac.wintec.irm.domain.AuthenticationToken;
import nz.ac.wintec.irm.domain.UserStatus;
import nz.ac.wintec.irm.repository.ApplicationUserRepository;
import nz.ac.wintec.irm.repository.AuthenticationTokenRepository;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final AuthenticationTokenRepository tokenRepository;
    private final ApplicationUserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final TokenDigest tokenDigest;

    public JwtAuthenticationFilter(JwtService jwtService,
                                   AuthenticationTokenRepository tokenRepository,
                                   ApplicationUserRepository userRepository,
                                   ObjectMapper objectMapper,
                                   Clock clock,
                                   TokenDigest tokenDigest) {
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.tokenDigest = tokenDigest;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith(BEARER_PREFIX) || authorization.length() == BEARER_PREFIX.length()) {
            reject(response, "Authentication failed: No token provided");
            return;
        }

        String rawToken = authorization.substring(BEARER_PREFIX.length()).trim();
        try {
            AuthenticatedUser principal = jwtService.parse(rawToken);
            AuthenticationToken storedToken = tokenRepository.findByAppUserId(principal.id())
                    .filter(row -> tokenDigest.matches(rawToken, row.getToken()))
                    .orElseThrow(() -> new IllegalArgumentException("Token mismatch"));
            LocalDateTime now = LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC);
            if (!storedToken.getExpirationDate().isAfter(now)) {
                throw new IllegalArgumentException("Token expired");
            }

            ApplicationUser user = userRepository.findById(principal.id())
                    .orElseThrow(() -> new IllegalArgumentException("User missing"));
            if (user.userStatus() != UserStatus.ACTIVE
                    || !user.getEmail().equalsIgnoreCase(principal.email())
                    || user.role() != principal.role()) {
                throw new IllegalArgumentException("Token identity is stale");
            }

            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    rawToken,
                    List.of(new SimpleGrantedAuthority(principal.role().authority()))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException | IllegalArgumentException exception) {
            SecurityContextHolder.clearContext();
            reject(response, "Authentication failed: Invalid or expired token");
        }
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("error", message));
    }
}

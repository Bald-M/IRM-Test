package nz.ac.wintec.irm.security;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class TokenDigest {

    public String digest(String token) {
        try {
            byte[] value = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    public boolean matches(String rawToken, String expectedDigest) {
        if (rawToken == null || expectedDigest == null) {
            return false;
        }
        return MessageDigest.isEqual(
                digest(rawToken).getBytes(StandardCharsets.US_ASCII),
                expectedDigest.getBytes(StandardCharsets.US_ASCII)
        );
    }
}

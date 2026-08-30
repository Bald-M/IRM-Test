package nz.ac.wintec.irm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("irm.mail")
public record MailProperties(
        boolean enabled,
        String host,
        int port,
        String username,
        String password,
        String from,
        boolean startTls,
        String locale
) {
}

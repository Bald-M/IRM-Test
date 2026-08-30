package nz.ac.wintec.irm.mail;

import nz.ac.wintec.irm.config.MailProperties;
import nz.ac.wintec.irm.domain.VerificationPurpose;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.MessageSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import nz.ac.wintec.irm.web.ApiException;

import java.util.Locale;
import java.util.Properties;

@Component
@ConditionalOnProperty(name = "irm.mail.enabled", havingValue = "true")
public class SmtpMailAdapter implements MailPort {

    private final MailProperties properties;
    private final MessageSource messageSource;
    private final JavaMailSenderImpl sender;
    private final Locale locale;

    public SmtpMailAdapter(MailProperties properties, MessageSource messageSource) {
        requireText(properties.host(), "SMTP_HOST");
        requireText(properties.from(), "SMTP_FROM");
        this.properties = properties;
        this.messageSource = messageSource;
        this.locale = Locale.forLanguageTag(properties.locale());
        this.sender = buildSender(properties);
    }

    @Override
    public void sendOtp(String recipient, String otp, VerificationPurpose purpose) {
        String key = purpose.messageKey();
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(properties.from());
        message.setTo(recipient);
        message.setSubject(messageSource.getMessage("mail.otp.subject." + key, null, locale));
        message.setText(messageSource.getMessage("mail.otp.body." + key, new Object[]{otp}, locale));
        try {
            sender.send(message);
        } catch (MailException exception) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "Email delivery is unavailable");
        }
    }

    static JavaMailSenderImpl buildSender(MailProperties properties) {
        boolean hasUsername = StringUtils.hasText(properties.username());
        boolean hasPassword = StringUtils.hasText(properties.password());
        if (hasUsername != hasPassword) {
            throw new IllegalArgumentException(
                    "SMTP_USERNAME and SMTP_PASSWORD must either both be set or both be empty");
        }
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(properties.host());
        sender.setPort(properties.port());
        Properties javaMailProperties = sender.getJavaMailProperties();
        javaMailProperties.setProperty("mail.smtp.auth", Boolean.toString(hasUsername));
        if (hasUsername) {
            sender.setUsername(properties.username());
            sender.setPassword(properties.password());
        }
        javaMailProperties.setProperty("mail.smtp.starttls.enable", Boolean.toString(properties.startTls()));
        return sender;
    }

    private static void requireText(String value, String environmentVariable) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(environmentVariable + " is required when MAIL_ENABLED=true");
        }
    }
}

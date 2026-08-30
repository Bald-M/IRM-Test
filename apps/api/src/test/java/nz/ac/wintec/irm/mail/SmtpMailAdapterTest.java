package nz.ac.wintec.irm.mail;

import nz.ac.wintec.irm.config.MailProperties;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticMessageSource;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SmtpMailAdapterTest {

    @Test
    void supportsAnonymousSmtpForLocalMailpit() {
        MailProperties properties = properties("", "");
        StaticMessageSource messages = new StaticMessageSource();
        messages.addMessage("mail.otp.subject.email-verification", Locale.forLanguageTag("en-NZ"), "Verify");
        messages.addMessage("mail.otp.body.email-verification", Locale.forLanguageTag("en-NZ"), "Code {0}");

        SmtpMailAdapter adapter = new SmtpMailAdapter(properties, messages);
        var sender = SmtpMailAdapter.buildSender(properties);

        assertThat(adapter.isAvailable()).isTrue();
        assertThat(sender.getUsername()).isNull();
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.auth")).isEqualTo("false");
        assertThat(sender.getJavaMailProperties().getProperty("mail.smtp.starttls.enable")).isEqualTo("false");
    }

    @Test
    void requiresUsernameAndPasswordToBeConfiguredTogether() {
        assertThatThrownBy(() -> SmtpMailAdapter.buildSender(properties("username", "")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must either both be set");
    }

    private static MailProperties properties(String username, String password) {
        return new MailProperties(
                true,
                "mailpit",
                1025,
                username,
                password,
                "no-reply@example.com",
                false,
                "en-NZ"
        );
    }
}

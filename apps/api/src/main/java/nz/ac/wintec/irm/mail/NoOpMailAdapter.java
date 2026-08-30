package nz.ac.wintec.irm.mail;

import nz.ac.wintec.irm.domain.VerificationPurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "irm.mail.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpMailAdapter implements MailPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(NoOpMailAdapter.class);

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void sendOtp(String recipient, String otp, VerificationPurpose purpose) {
        LOGGER.debug("OTP email delivery is disabled; no message was sent");
    }
}

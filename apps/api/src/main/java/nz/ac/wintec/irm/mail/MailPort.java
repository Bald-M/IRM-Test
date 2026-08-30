package nz.ac.wintec.irm.mail;

import nz.ac.wintec.irm.domain.VerificationPurpose;

public interface MailPort {
    default boolean isAvailable() {
        return true;
    }

    void sendOtp(String recipient, String otp, VerificationPurpose purpose);
}

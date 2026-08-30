package nz.ac.wintec.irm.domain;

public enum VerificationPurpose {
    EMAIL_VERIFICATION("Email Verification", "email-verification"),
    FORGOT_PASSWORD("Forgot Password", "forgot-password");

    private final String databaseValue;
    private final String messageKey;

    VerificationPurpose(String databaseValue, String messageKey) {
        this.databaseValue = databaseValue;
        this.messageKey = messageKey;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public String messageKey() {
        return messageKey;
    }
}

package nz.ac.wintec.irm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_verification")
public class UserVerification {

    public static final String ACTIVE = "Active";
    public static final String INACTIVE = "Inactive";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "verification_id")
    private Integer id;

    @Column(name = "app_user_id", nullable = false, unique = true)
    private Integer appUserId;

    @Column(name = "server_ref", nullable = false, unique = true, length = 48)
    private String serverRef;

    @Column(name = "code", length = 6)
    private String code;

    @Column(name = "code_hash", length = 60)
    private String codeHash;

    @Column(name = "type", nullable = false, length = 32)
    private String type;

    @Column(name = "status", nullable = false, length = 16)
    private String status;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "failed_attempts", nullable = false)
    private int failedAttempts;

    @Column(name = "last_sent_date")
    private LocalDateTime lastSentDate;

    protected UserVerification() {
    }

    public UserVerification(Integer appUserId, String serverRef) {
        this.appUserId = appUserId;
        this.serverRef = serverRef;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAppUserId() {
        return appUserId;
    }

    public String getServerRef() {
        return serverRef;
    }

    public String getCode() {
        return code;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public LocalDateTime getLastSentDate() {
        return lastSentDate;
    }

    public boolean isActive() {
        return ACTIVE.equals(status);
    }

    public boolean hasPurpose(VerificationPurpose purpose) {
        return purpose.databaseValue().equals(type);
    }

    public void issue(String newCodeHash, VerificationPurpose purpose, LocalDateTime expiresAt, LocalDateTime now) {
        code = null;
        codeHash = newCodeHash;
        type = purpose.databaseValue();
        status = ACTIVE;
        expirationDate = expiresAt;
        updatedDate = now;
        lastSentDate = now;
        failedAttempts = 0;
    }

    public void markVerified(LocalDateTime now) {
        status = INACTIVE;
        updatedDate = now;
        failedAttempts = 0;
    }

    public int recordFailedAttempt(LocalDateTime now) {
        failedAttempts += 1;
        updatedDate = now;
        return failedAttempts;
    }

    public void consume(LocalDateTime now) {
        status = INACTIVE;
        code = null;
        codeHash = null;
        updatedDate = now;
    }
}

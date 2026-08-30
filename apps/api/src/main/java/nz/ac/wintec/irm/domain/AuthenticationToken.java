package nz.ac.wintec.irm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "authentication_token")
public class AuthenticationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_token_id")
    private Integer id;

    @Column(name = "app_user_id", nullable = false, unique = true)
    private Integer appUserId;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expiration_date", nullable = false)
    private LocalDateTime expirationDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    protected AuthenticationToken() {
    }

    public AuthenticationToken(Integer appUserId) {
        this.appUserId = appUserId;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAppUserId() {
        return appUserId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void rotate(String token, LocalDateTime expirationDate, LocalDateTime updatedDate) {
        this.token = token;
        this.expirationDate = expirationDate;
        this.updatedDate = updatedDate;
    }
}

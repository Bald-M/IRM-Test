package nz.ac.wintec.irm.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "application_user")
public class ApplicationUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "app_user_id")
    private Integer id;

    @Column(name = "username", length = 40)
    private String username;

    @Column(name = "email", nullable = false, length = 80, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 225)
    private String password;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "registered_date", nullable = false)
    private LocalDateTime registeredDate;

    protected ApplicationUser() {
    }

    public ApplicationUser(String username, String email, String password, Role role, UserStatus status,
                           LocalDateTime registeredDate) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.type = role.databaseValue();
        this.status = status.databaseValue();
        this.registeredDate = registeredDate;
    }

    public Integer getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public Role role() {
        return Role.fromDatabase(type);
    }

    public String getStatus() {
        return status;
    }

    public UserStatus userStatus() {
        return UserStatus.fromDatabase(status);
    }

    public void setStatus(UserStatus status) {
        this.status = status.databaseValue();
    }

    public LocalDateTime getRegisteredDate() {
        return registeredDate;
    }
}

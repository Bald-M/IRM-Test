package nz.ac.wintec.irm.repository;

import nz.ac.wintec.irm.domain.UserVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface UserVerificationRepository extends JpaRepository<UserVerification, Integer> {
    Optional<UserVerification> findByServerRef(String serverRef);

    Optional<UserVerification> findByAppUserId(Integer appUserId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select verification from UserVerification verification where verification.serverRef = :serverRef")
    Optional<UserVerification> findByServerRefForUpdate(@Param("serverRef") String serverRef);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select verification from UserVerification verification where verification.appUserId = :appUserId")
    Optional<UserVerification> findByAppUserIdForUpdate(@Param("appUserId") Integer appUserId);
}

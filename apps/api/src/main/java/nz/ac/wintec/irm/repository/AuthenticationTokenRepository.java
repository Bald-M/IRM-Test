package nz.ac.wintec.irm.repository;

import nz.ac.wintec.irm.domain.AuthenticationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthenticationTokenRepository extends JpaRepository<AuthenticationToken, Integer> {
    Optional<AuthenticationToken> findByAppUserId(Integer appUserId);

    void deleteByAppUserId(Integer appUserId);
}

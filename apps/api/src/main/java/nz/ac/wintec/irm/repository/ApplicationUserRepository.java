package nz.ac.wintec.irm.repository;

import nz.ac.wintec.irm.domain.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Integer> {
    Optional<ApplicationUser> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}

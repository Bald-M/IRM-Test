package nz.ac.wintec.irm.repository;

import nz.ac.wintec.irm.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByAppUserId(Integer appUserId);
}

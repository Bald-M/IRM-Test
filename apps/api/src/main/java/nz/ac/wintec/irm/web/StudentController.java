package nz.ac.wintec.irm.web;

import jakarta.validation.Valid;
import nz.ac.wintec.irm.domain.Student;
import nz.ac.wintec.irm.security.AuthenticatedUser;
import nz.ac.wintec.irm.service.StudentService;
import nz.ac.wintec.irm.web.dto.ApplicationRequest;
import nz.ac.wintec.irm.web.dto.UserIdRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/completeApplication")
    @PreAuthorize("hasRole('STUDENT')")
    Map<String, String> completeApplication(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody ApplicationRequest request) {
        studentService.saveApplication(principal, request);
        return Map.of("description", "Application saved");
    }

    @PostMapping("/userProfileData")
    Map<String, Student> getProfile(
            @AuthenticationPrincipal AuthenticatedUser principal,
            @Valid @RequestBody UserIdRequest request) {
        return Map.of("student", studentService.getProfile(principal, request.userId()));
    }

    @GetMapping("/allStudents")
    @PreAuthorize("hasAnyRole('ADMIN', 'INDUSTRY')")
    Map<String, List<Student>> getAllStudents() {
        return Map.of("students", studentService.getAllStudents());
    }
}

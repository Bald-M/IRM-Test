package nz.ac.wintec.irm.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import nz.ac.wintec.irm.domain.Role;
import nz.ac.wintec.irm.domain.Student;
import nz.ac.wintec.irm.repository.StudentRepository;
import nz.ac.wintec.irm.security.AuthenticatedUser;
import nz.ac.wintec.irm.web.ApiException;
import nz.ac.wintec.irm.web.dto.ApplicationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

@Service
public class StudentService {

    private static final Set<String> GENDERS = Set.of("Male", "Female");
    private static final Set<String> STUDENT_TYPES = Set.of("Domestic Student", "International Student");

    private final StudentRepository studentRepository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public StudentService(StudentRepository studentRepository, ObjectMapper objectMapper, Clock clock) {
        this.studentRepository = studentRepository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public void saveApplication(AuthenticatedUser principal, ApplicationRequest request) {
        if (principal.role() != Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Authentication failed. Please try again");
        }
        if (!principal.email().equalsIgnoreCase(request.studentEmail())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Authentication failed. Please try again");
        }
        if (!GENDERS.contains(request.gender())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid gender");
        }
        if (!STUDENT_TYPES.contains(request.studentType())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid student type");
        }
        validateProfileUrl(request.cvLink());
        validateProfileUrl(request.linkedinLink());
        validateProfileUrl(request.portfolioLink());
        validateProfileUrl(request.githubLink());

        String internshipOptions = toLegacyJson(request.internshipOptions());
        String preferredCompanies = toLegacyJson(request.preferredCompanies());
        if (internshipOptions.length() > 255 || preferredCompanies.length() > 255) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Internship choices are too long");
        }

        Student student = studentRepository.findByAppUserId(principal.id())
                .orElseGet(() -> new Student(principal.id()));
        student.setName(request.name());
        student.setWintecId(request.wintecId());
        student.setGender(request.gender());
        student.setStudentType(request.studentType());
        student.setPersonalEmail(request.personalEmail());
        student.setStudentEmail(request.studentEmail());
        student.setPhoneNumber(request.phoneNumber());
        student.setPersonalStatement(request.personalStatement());
        student.setCvLink(request.cvLink());
        student.setLinkedinLink(request.linkedinLink());
        student.setPortfolioLink(request.portfolioLink());
        student.setGithubLink(request.githubLink());
        student.setAverageGrade(request.averageGrade());
        student.setProgrammeOfStudy(request.programmeOfStudy());
        student.setAreaOfStudy(request.areaOfStudy());
        student.setInternshipOptions(internshipOptions);
        student.setPreferredCompanies(preferredCompanies);
        student.setFirstPreference(request.firstPreference());
        student.setSecondPreference(request.secondPreference());
        student.setSkills(request.skills());
        student.setFavouriteCourses(request.favouriteCourses());
        student.setReference(request.references());
        student.setSubmissionDate(LocalDateTime.ofInstant(clock.instant(), ZoneOffset.UTC));
        studentRepository.save(student);
    }

    @Transactional(readOnly = true)
    public Student getProfile(AuthenticatedUser principal, Integer requestedUserId) {
        boolean elevated = principal.role() == Role.ADMIN || principal.role() == Role.INDUSTRY;
        if (!elevated && (principal.role() != Role.STUDENT || !principal.id().equals(requestedUserId))) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied");
        }
        return studentRepository.findByAppUserId(requestedUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User is not exist"));
    }

    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    private String toLegacyJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JacksonException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid internship choices");
        }
    }

    private static void validateProfileUrl(String value) {
        try {
            URI uri = new URI(value);
            String scheme = uri.getScheme();
            if (!uri.isAbsolute()
                    || uri.getHost() == null
                    || !("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Profile links must be absolute HTTP or HTTPS URLs");
            }
        } catch (URISyntaxException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Profile links must be absolute HTTP or HTTPS URLs");
        }
    }
}

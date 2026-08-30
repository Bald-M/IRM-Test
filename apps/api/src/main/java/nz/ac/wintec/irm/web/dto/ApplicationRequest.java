package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ApplicationRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 20) String wintecId,
        @NotBlank String gender,
        @NotBlank String studentType,
        @NotBlank @Email(message = "Personal email address is not an email format") @Size(max = 255) String personalEmail,
        @NotBlank @Email(message = "Student email address is not an email format") @Size(max = 255) String studentEmail,
        @NotBlank @Size(max = 255) String phoneNumber,
        @NotBlank @Size(max = 512) String personalStatement,
        @NotBlank @Size(max = 255) String cvLink,
        @NotBlank @Size(max = 255) String linkedinLink,
        @NotBlank @Size(max = 255) String portfolioLink,
        @NotBlank @Size(max = 255) String githubLink,
        @NotBlank @Size(max = 10) String averageGrade,
        @NotBlank @Size(max = 255) String programmeOfStudy,
        @NotBlank @Size(max = 255) String areaOfStudy,
        @NotEmpty List<@NotBlank String> internshipOptions,
        @NotEmpty List<@NotBlank String> preferredCompanies,
        @NotBlank @Size(max = 255) String firstPreference,
        @NotBlank @Size(max = 255) String secondPreference,
        @NotBlank @Size(max = 255) String skills,
        @NotBlank @Size(max = 255) String favouriteCourses,
        @NotBlank @Size(max = 50) String references
) {
}

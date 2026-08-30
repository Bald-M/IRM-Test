package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegistrationRequest(
        @NotBlank(message = "Email, Password or User role is empty")
        @Email(message = "Invalid email address")
        @Size(max = 80, message = "Your email address length is not legal")
        String email,

        @NotBlank(message = "Email, Password or User role is empty")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must include uppercase, lowercase letters, and numbers and must be at least 8 digits"
        )
        String password,

        @NotBlank(message = "Email, Password or User role is empty")
        String type
) {
}

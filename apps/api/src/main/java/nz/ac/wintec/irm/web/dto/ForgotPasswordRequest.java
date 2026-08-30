package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Please provide email")
        @Email(message = "Please provide valid email address") String email
) {
}

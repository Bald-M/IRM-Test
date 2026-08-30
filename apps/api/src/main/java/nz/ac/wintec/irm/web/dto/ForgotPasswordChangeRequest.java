package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordChangeRequest(
        @NotBlank(message = "Invalid server_ref. Please try again") String serverRef,
        @NotBlank(message = "Email address did not match")
        @Email(message = "Please provide valid email address") String email,
        @NotBlank(message = "Invalid OTP. Please try again") String otp,
        @NotBlank(message = "Please provide password")
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must include uppercase, lowercase letters, and numbers and must be at least 8 digits"
        ) String password
) {
}

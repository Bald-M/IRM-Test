package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ForgotPasswordVerifyRequest(
        @NotBlank(message = "server_ref, email or OTP is empty") String serverRef,
        @NotBlank(message = "server_ref, email or OTP is empty")
        @Email(message = "Please provide valid email address") String email,
        @NotBlank(message = "server_ref, email or OTP is empty")
        @Pattern(regexp = "\\d{6}", message = "Invalid OTP. Please try again") String otp
) {
}

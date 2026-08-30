package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerificationRequest(
        @NotBlank(message = "server_ref or OTP is empty") String serverRef,
        @NotBlank(message = "server_ref or OTP is empty")
        @Pattern(regexp = "\\d{6}", message = "Invalid OTP. Please try again") String otp
) {
}

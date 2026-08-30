package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Please provide email or password") String email,
        @NotBlank(message = "Please provide email or password") String password
) {
}

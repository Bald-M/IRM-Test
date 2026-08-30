package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.NotNull;

public record UserIdRequest(
        @NotNull(message = "Please provide user id") Integer userId
) {
}

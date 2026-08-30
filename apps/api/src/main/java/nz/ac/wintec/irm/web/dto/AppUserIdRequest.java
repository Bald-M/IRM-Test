package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.NotNull;

public record AppUserIdRequest(
        @NotNull(message = "App User Id is empty") Integer appUserId
) {
}

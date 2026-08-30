package nz.ac.wintec.irm.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ServerRefRequest(
        @NotBlank(message = "Invalid Server Ref") String serverRef
) {
}

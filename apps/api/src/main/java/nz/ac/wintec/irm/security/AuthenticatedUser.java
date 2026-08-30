package nz.ac.wintec.irm.security;

import nz.ac.wintec.irm.domain.Role;

public record AuthenticatedUser(Integer id, String email, Role role) {
}

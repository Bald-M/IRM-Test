package nz.ac.wintec.irm.domain;

import java.util.Arrays;

public enum Role {
    STUDENT("Student", "Student"),
    INDUSTRY("Client", "Industry"),
    IRM_USER("IRM User", "IRM User"),
    ADMIN("Admin", "Admin");

    private final String databaseValue;
    private final String apiValue;

    Role(String databaseValue, String apiValue) {
        this.databaseValue = databaseValue;
        this.apiValue = apiValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public String apiValue() {
        return apiValue;
    }

    public String authority() {
        return "ROLE_" + name();
    }

    public static Role fromDatabase(String value) {
        return Arrays.stream(values())
                .filter(role -> role.databaseValue.equalsIgnoreCase(value)
                        || role.apiValue.equalsIgnoreCase(value)
                        || role.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported user role"));
    }

    public static Role fromRegistration(String value) {
        Role role = fromDatabase(value == null ? "" : value.trim());
        if (role != STUDENT && role != INDUSTRY) {
            throw new IllegalArgumentException("Unsupported user role");
        }
        return role;
    }
}

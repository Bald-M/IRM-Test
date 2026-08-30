package nz.ac.wintec.irm.domain;

public enum UserStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    BLOCKED("Blocked"),
    REMOVED("Removed");

    private final String databaseValue;

    UserStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String databaseValue() {
        return databaseValue;
    }

    public static UserStatus fromDatabase(String value) {
        for (UserStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unsupported account status");
    }
}

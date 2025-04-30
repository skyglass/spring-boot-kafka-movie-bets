package net.skycomposer.moviebets.common.dto.bet;

public enum BetStatus {
    PlACED("placed"),
    SETTLING("closing"),
    SETTLED("settled");

    private String value;

    private BetStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BetStatus fromValue(String value) {
        for (BetStatus status: values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for BetStatus enum: %d", value));
    }
}

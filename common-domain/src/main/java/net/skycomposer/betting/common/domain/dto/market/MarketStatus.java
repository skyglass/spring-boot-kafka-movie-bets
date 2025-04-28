package net.skycomposer.betting.common.domain.dto.market;

public enum MarketStatus {
    OPENED("opened"),
    CLOSING("closing"),
    CLOSED("closed"),
    SETTLED("settled");

    private String value;

    private MarketStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MarketStatus fromValue(String value) {
        for (MarketStatus status: values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for MarketStatus enum: %d", value));
    }
}

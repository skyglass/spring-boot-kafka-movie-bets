package net.skycomposer.betting.common.domain.dto.market;

public enum MarketResult {
    ITEM1_WINS(0),
    ITEM2_WINS(1);

    private int value;

    private MarketResult(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static MarketResult fromValue(int value) {
        for (MarketResult result: values()) {
            if (result.getValue() == value) {
                return result;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown value for MarketResult enum: %d", value));
    }
}

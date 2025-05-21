package net.skycomposer.moviebets.common.dto.bet;

import java.util.List;

public enum BetStatus {
    PlACED,
    VALIDATED,
    SETTLE_STARTED,
    SETTLED,
    CANCELLED;

    public static List<BetStatus> getClosedMarketInvalidStatuses() {
        return List.of(PlACED, CANCELLED);
    }
}

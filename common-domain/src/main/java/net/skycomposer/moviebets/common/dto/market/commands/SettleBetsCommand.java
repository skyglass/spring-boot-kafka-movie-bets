package net.skycomposer.moviebets.common.dto.market.commands;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettleBetsCommand {
    private UUID marketId;
    private String requestId;
    BigDecimal winnerEarned;
    BigDecimal totalLost;
    Long totalCount;

    public SettleBetsCommand(UUID marketId, String requestId) {
        this.marketId = marketId;
        this.requestId = requestId;
    }

}

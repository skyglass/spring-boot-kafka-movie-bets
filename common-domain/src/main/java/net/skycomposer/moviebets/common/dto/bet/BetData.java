package net.skycomposer.moviebets.common.dto.bet;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetData {
    private UUID betId;
    private UUID marketId;
    private String customerId;
    private UUID requestId;
    private UUID cancelRequestId;
    private String marketName;
    private Integer stake;
    private MarketResult result;
    private BetStatus status;
    private Boolean betWon;

}

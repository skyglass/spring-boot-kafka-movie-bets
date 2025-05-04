package net.skycomposer.moviebets.common.dto.bet.events;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetCreatedEvent {
    private UUID betId;
    private UUID marketId;
    private UUID requestId;
    private UUID cancelRequestId;
    private String customerId;
    private String marketName;
    private Integer stake;
    private MarketResult result;
}

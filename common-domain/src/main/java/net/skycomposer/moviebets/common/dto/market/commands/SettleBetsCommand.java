package net.skycomposer.moviebets.common.dto.market.commands;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettleBetsCommand {
    private UUID marketId;
    private UUID requestId;
    BigDecimal winnerEarned;
    Integer totalCount;
    MarketResult winnerResult;


}

package net.skycomposer.moviebets.common.dto.bet.commands;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleBetCommand {
    private UUID betId;
    private String customerId;
    private UUID marketId;
    private UUID requestId;
    private Integer stake;
    private BigDecimal winnerEarned;
    private boolean winner;
}

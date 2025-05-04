package net.skycomposer.moviebets.common.dto.bet.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

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
}

package net.skycomposer.moviebets.common.dto.market.commands;

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
public class SettleMarketCommand {
    private UUID marketId;
    private MarketResult winResult;
}

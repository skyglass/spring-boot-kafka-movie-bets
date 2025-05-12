package net.skycomposer.moviebets.common.dto.market.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketCloseConfirmedEvent {
    private UUID marketId;
    private MarketResult marketResult;
}
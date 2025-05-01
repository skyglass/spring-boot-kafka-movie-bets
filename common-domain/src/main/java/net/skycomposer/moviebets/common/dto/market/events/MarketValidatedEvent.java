package net.skycomposer.moviebets.common.dto.market.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketValidatedEvent {
    private UUID betId;
    private UUID marketId;

}

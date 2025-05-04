package net.skycomposer.moviebets.common.dto.bet.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetSettledEvent {
    private UUID betId;
    private UUID marketId;
}

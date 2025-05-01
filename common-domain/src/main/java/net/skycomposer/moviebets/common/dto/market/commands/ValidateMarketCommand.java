package net.skycomposer.moviebets.common.dto.market.commands;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateMarketCommand {
    private UUID betId;
    private UUID marketId;
}

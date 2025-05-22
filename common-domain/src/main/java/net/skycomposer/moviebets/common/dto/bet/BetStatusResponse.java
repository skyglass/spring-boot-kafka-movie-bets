package net.skycomposer.moviebets.common.dto.bet;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BetStatusResponse {
    private UUID betId;
    private boolean betExists;
}

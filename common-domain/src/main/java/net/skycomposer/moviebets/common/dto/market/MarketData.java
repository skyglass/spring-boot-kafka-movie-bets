package net.skycomposer.moviebets.common.dto.market;

import java.time.Instant;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    private UUID marketId;
    private String item1;
    private String item2;
    private MarketStatus status;
    private MarketResult result;
    private Instant closesAt;
    private boolean open;

}
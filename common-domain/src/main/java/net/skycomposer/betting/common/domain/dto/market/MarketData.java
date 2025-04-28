package net.skycomposer.betting.common.domain.dto.market;

import lombok.*;

import java.util.UUID;

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
    private long closesAt;
    private boolean open;

}
package net.skycomposer.moviebets.market;

import java.time.Instant;
import java.util.UUID;

import net.skycomposer.moviebets.common.dto.market.*;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketTestHelper {

    private static final long DEFAULT_MARKET_CLOSE_TIME_MS = 24 * 60 * 60 * 1000; //24 hours

    private final MarketClient marketClient;

    public MarketResponse createMarket(UUID marketId) {
        MarketData marketData = MarketData.builder()
                .marketId(marketId)
                .item1("RM")
                .item2("MU")
                .status(MarketStatus.OPENED)
                .closesAt(Instant.ofEpochMilli(Instant.now().toEpochMilli() + DEFAULT_MARKET_CLOSE_TIME_MS))
                .open(true)
                .build();
        return marketClient.open(marketData);
    }

    public MarketResponse updateMarket(UUID marketId) {
        MarketData marketData = MarketData.builder()
                .marketId(marketId)
                .status(MarketStatus.CLOSING)
                .build();
        return marketClient.update(marketData);
    }

    public MarketResponse closeMarket(UUID marketId, MarketResult result) {
        CloseMarketRequest closeMarketRequest = CloseMarketRequest
                .builder()
                .marketId(marketId)
                .result(result.getValue())
                .build();
        return marketClient.close(closeMarketRequest);
    }

    public MarketData getMarketData(UUID marketId) {
        return marketClient.getState(marketId.toString());
    }
}


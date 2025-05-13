package net.skycomposer.moviebets.market;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.common.dto.market.CloseMarketRequest;
import net.skycomposer.moviebets.common.dto.market.MarketData;
import net.skycomposer.moviebets.common.dto.market.MarketResponse;
import net.skycomposer.moviebets.common.dto.market.MarketStatus;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketTestHelper {

    private static final long DEFAULT_MARKET_CLOSE_TIME_MS = 24 * 60 * 60 * 1000; //24 hours

    private final MarketClient marketClient;

    public MarketResponse createMarket() {
        MarketData marketData = MarketData.builder()
                .item1("RM")
                .item2("MU")
                .status(MarketStatus.OPENED)
                .closesAt(Instant.ofEpochMilli(Instant.now().toEpochMilli() + DEFAULT_MARKET_CLOSE_TIME_MS))
                .open(true)
                .build();
        return marketClient.open(marketData);
    }

    public MarketResponse closeMarket(UUID marketId) {
        CloseMarketRequest closeMarketRequest = CloseMarketRequest
                .builder()
                .marketId(marketId)
                .build();
        return marketClient.close(closeMarketRequest);
    }

    public MarketData getMarketData(UUID marketId) {
        return marketClient.getState(marketId.toString());
    }
}


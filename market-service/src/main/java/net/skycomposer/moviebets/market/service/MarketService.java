package net.skycomposer.moviebets.market.service;

import net.skycomposer.moviebets.common.dto.market.*;

import java.util.List;
import java.util.UUID;

public interface MarketService {

    List<MarketData> findAll();

    MarketData findMarketById(UUID marketId);

    MarketResponse open(MarketData marketData);

    MarketResponse close(CloseMarketRequest request);

    MarketResponse close(UUID marketId);

    MarketResponse settle(UUID marketId);

    MarketResponse cancel(CancelMarketRequest request);

    MarketResponse marketCloseConfirmed(UUID marketId, MarketResult marketResult);

    MarketResponse marketCloseFailed(UUID marketId);

}

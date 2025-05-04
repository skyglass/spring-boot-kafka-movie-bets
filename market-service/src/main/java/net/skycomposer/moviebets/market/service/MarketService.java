package net.skycomposer.moviebets.market.service;

import net.skycomposer.moviebets.common.dto.market.CancelMarketRequest;
import net.skycomposer.moviebets.common.dto.market.CloseMarketRequest;
import net.skycomposer.moviebets.common.dto.market.MarketData;
import net.skycomposer.moviebets.common.dto.market.MarketResponse;

import java.util.List;
import java.util.UUID;

public interface MarketService {

    List<MarketData> findAll();

    MarketData findMarketById(UUID marketId);

    MarketResponse open(MarketData marketData);

    MarketResponse update(MarketData marketData);

    MarketResponse close(CloseMarketRequest request);

    MarketResponse settle(UUID marketId);

    MarketResponse cancel(CancelMarketRequest request);

}

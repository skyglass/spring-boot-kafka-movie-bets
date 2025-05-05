package net.skycomposer.moviebets.bet.service;

import java.util.List;
import java.util.UUID;

import net.skycomposer.moviebets.common.dto.bet.*;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

public interface BetService {

    List<BetData> findAll();

    BetData findBetById(UUID betId);

    List<BetData> findByMarketAndStatus(UUID marketId, BetStatus betStatus, Integer limit);

    void updateStatus(List<UUID> betUuids, BetStatus betStatus);

    BetData getState(UUID betId);

    SumStakesData getBetsByMarket(UUID marketId);

    BetDataList getBetsForMarket(UUID marketId);

    BetDataList getBetsForPlayer(String customerId);

    BetResponse open(BetData betData);

    BetResponse close(CancelBetRequest request);

    int countByStatus(BetStatus status);

    void setBetValidated(UUID betId);

    boolean isMarketClosed(UUID marketId);

    int countSettledBets(UUID marketId);

    void marketSettleStart(UUID marketId, int expectedCount);

    void updateMarketSettleCount(UUID betId, UUID marketId);

    void marketSettleDone(UUID marketId, MarketResult winResult);

}

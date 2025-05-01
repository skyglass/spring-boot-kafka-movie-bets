package net.skycomposer.moviebets.bet.service;

import java.util.List;
import java.util.UUID;

import net.skycomposer.moviebets.common.dto.bet.*;

public interface BetService {

    List<BetData> findAll();

    BetData findBetById(UUID betId);

    BetData getState(UUID betId);

    SumStakesData getBetsByMarket(UUID marketId);

    BetDataList getBetsForMarket(UUID marketId);

    BetDataList getBetsForPlayer(String customerId);

    BetResponse open(BetData betData);

    BetResponse close(CancelBetRequest request);

    BetResponse updateStatus(UUID betId, BetStatus betStatus);

}

package net.skycomposer.betting.bet;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import feign.FeignException;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import net.skycomposer.betting.common.domain.dto.betting.BetStatus;
import net.skycomposer.betting.common.domain.dto.market.MarketResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.betting.common.domain.dto.betting.BetData;
import net.skycomposer.betting.common.domain.dto.betting.BetResponse;
import net.skycomposer.betting.common.domain.dto.betting.SumStakesData;
import net.skycomposer.betting.common.domain.dto.market.MarketData;

@Component
@RequiredArgsConstructor
@Slf4j
public class BetTestHelper {

    private final BetClient betClient;

    @Async
    public CompletableFuture<BetResponse> asyncPlaceBet(UUID betId, UUID marketId, String customerId, int stake, MarketResult result) throws InterruptedException {
        return CompletableFuture.completedFuture(createBet(betId, marketId, customerId, stake, result));
    }

    @SneakyThrows
    public BetResponse createBet(UUID betId, UUID marketId, String customerId, int stake, MarketResult result) {
        BetData betData = BetData.builder()
                .betId(betId)
                .marketId(marketId)
                .customerId(customerId)
                .result(result)
                .stake(stake)
                .status(BetStatus.PlACED)
                .build();
        BetResponse response = null;
        while (response == null) {
            try {
                response = betClient.open(betData);
            } catch (FeignException.TooManyRequests e) {
                TimeUnit.MILLISECONDS.sleep(Duration.ofSeconds(1).toMillis());
            }
        }
        return response;

    }

    public BetData getState(UUID betId) {
        return betClient.getState(betId.toString());
    }

    public SumStakesData getBetsByMarket(UUID marketId) {
        return betClient.getBetsByMarket(marketId.toString());
    }

}


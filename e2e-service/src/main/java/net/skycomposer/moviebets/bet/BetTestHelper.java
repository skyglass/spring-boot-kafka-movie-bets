package net.skycomposer.moviebets.bet;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.BetResponse;
import net.skycomposer.moviebets.common.dto.bet.BetStatus;
import net.skycomposer.moviebets.common.dto.bet.SumStakesData;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Component
@RequiredArgsConstructor
@Slf4j
public class BetTestHelper {

    private final BetClient betClient;

    @Async
    public CompletableFuture<BetResponse> asyncPlaceBet(UUID marketId, String customerId, int stake, MarketResult result) throws InterruptedException {
        return CompletableFuture.completedFuture(createBet(marketId, customerId, stake, result));
    }

    @SneakyThrows
    public BetResponse createBet(UUID marketId, String customerId, int stake, MarketResult result) {
        BetData betData = BetData.builder()
                .marketId(marketId)
                .marketName("RM vs MU")
                .customerId(customerId)
                .requestId(UUID.randomUUID())
                .cancelRequestId(UUID.randomUUID())
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


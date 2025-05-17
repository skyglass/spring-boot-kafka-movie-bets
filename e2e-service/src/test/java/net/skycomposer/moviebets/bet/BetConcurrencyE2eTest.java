package net.skycomposer.moviebets.bet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.skycomposer.moviebets.common.E2eTest;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.BetResponse;
import net.skycomposer.moviebets.common.dto.bet.BetStatus;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;
import net.skycomposer.moviebets.common.dto.market.MarketData;
import net.skycomposer.moviebets.common.dto.market.MarketResponse;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.MarketStatus;
import net.skycomposer.moviebets.customer.CustomerTestHelper;
import net.skycomposer.moviebets.market.MarketTestHelper;

@SpringBootTest
@Slf4j
public class BetConcurrencyE2eTest extends E2eTest {

    @Autowired
    private CustomerTestHelper customerTestHelper;

    @Autowired
    private MarketTestHelper marketTestHelper;

    @Autowired
    private BetTestHelper betTestHelper;


    @Test
    @SneakyThrows
    void createParallelBetsThenFundsAreZeroTest() {
        String customerId = UUID.randomUUID().toString();
        UUID fundRequestId = UUID.randomUUID();
        int customerBalance = 500;
        int addFundsAmount = 5;
        int beforeMarketCloseBalance = 0;
        int afterMarketCloseBalance = 10;
        UUID marketId;
        int betStake = 10;
        MarketResult betResult = MarketResult.ITEM2_WINS;

        CustomerResponse customerResponse = customerTestHelper.createCustomer(customerId, fundRequestId, customerBalance);
        Thread.sleep(1200);
        //Duplicate request with the same request id to make sure that duplicates are handled correctly
        customerResponse = customerTestHelper.createCustomer(customerId, fundRequestId, customerBalance);
        assertThat(customerResponse.getMessage(), equalTo("Duplicate addFunds request for customer %s, requestId = %s".formatted(customerId, fundRequestId)));
        MarketResponse marketResponse = marketTestHelper.createMarket();
        marketId = marketResponse.getMarketId();
        assertThat(marketResponse.getMessage(), equalTo("Market %s opened successfully".formatted(marketId)));

        // Start the clock
        long start = Instant.now().toEpochMilli();

        int numberOfBets = 100;
        List<UUID> customers = new ArrayList<>();
        for (int i = 0; i < numberOfBets; i++) {
            customers.add(UUID.randomUUID());
        }

        List<CompletableFuture<CustomerResponse>> addedFunds = new ArrayList<>();
        for (int i = 0; i < numberOfBets; i++) {
            String currentCustomerId = customers.get(i).toString();
            CompletableFuture<CustomerResponse> addFundsResult = customerTestHelper
                    .asyncAddFunds(currentCustomerId, UUID.randomUUID(), addFundsAmount);
            addedFunds.add(addFundsResult);
        }

        List<CompletableFuture<BetResponse>> createdBets = new ArrayList<>();
        for (int i = 0; i < numberOfBets; i++) {
            String currentCustomerId = customers.get(i).toString();
            CompletableFuture<BetResponse> betResponse = betTestHelper.asyncPlaceBet(
                    marketId, currentCustomerId, betStake,
                    betResult);
            createdBets.add(betResponse);
        }

        List<CompletableFuture<CustomerResponse>> addedFunds2 = new ArrayList<>();
        for (int i = 0; i < numberOfBets; i++) {
            String currentCustomerId = customers.get(i).toString();
            CompletableFuture<CustomerResponse> addFundsResult = customerTestHelper
                    .asyncAddFunds(currentCustomerId, UUID.randomUUID(), addFundsAmount);
            addedFunds2.add(addFundsResult);
        }

        // Wait until they are all done
        CompletableFuture.allOf(createdBets.toArray(new CompletableFuture[0])).join();
        CompletableFuture.allOf(addedFunds.toArray(new CompletableFuture[0])).join();
        CompletableFuture.allOf(addedFunds2.toArray(new CompletableFuture[0])).join();

        //Before market is closed, make sure that all bets are validated
        for (CompletableFuture<BetResponse> betFuture: createdBets) {
            Thread.sleep(50);
            BetResponse betResponse = betFuture.get();
            final BetData[] resultHolder = {betTestHelper.getState(betResponse.getBetId())};
            log.info("--> " + betResponse.getBetId());
            assertTimeoutPreemptively(
                    Duration.ofSeconds(120)
                    , () -> {
                        while (resultHolder[0].getStatus() != BetStatus.VALIDATED) {
                            Thread.sleep(100);
                            resultHolder[0] = betTestHelper.getState(betResponse.getBetId());
                        }
                        assertThat(resultHolder[0].getStatus(), equalTo(BetStatus.VALIDATED));
                    }, () -> String.format("Bet with betId = %s is not validated; currentStatus = %s", betResponse.getBetId(), resultHolder[0].getStatus())
            );
        }

        for (int i = 0; i < numberOfBets; i++) {
            Thread.sleep(50);
            String currentCustomerId = customers.get(i).toString();
            log.info("<--> " + currentCustomerId);
            assertTimeoutPreemptively(
                    Duration.ofSeconds(20)
                    , () -> {
                        var result = customerTestHelper.findCustomerById(currentCustomerId);
                        while (result.getBalance().doubleValue() != beforeMarketCloseBalance) {
                            Thread.sleep(100);
                            result = customerTestHelper.findCustomerById(currentCustomerId);
                        }
                        assertThat(result.getBalance().compareTo(new BigDecimal(beforeMarketCloseBalance)), equalTo(0));
                    }, () -> String.format("Available customer balance after market update is incorrect for customerId = %s: balance = %d", customerId, customerTestHelper.findCustomerById(customerId).getBalance())
            );
        }


        marketTestHelper.closeMarket(marketId);

        //After market is closed, make sure that all winner customers received money back
        for (int i = 0; i < numberOfBets; i++) {
            Thread.sleep(50);
            String currentCustomerId = customers.get(i).toString();
            log.info("<<-->> " + currentCustomerId);
            assertTimeoutPreemptively(
                    Duration.ofSeconds(20)
                    , () -> {
                        var result = customerTestHelper.findCustomerById(currentCustomerId);
                        while (result.getBalance().doubleValue() != afterMarketCloseBalance) {
                            log.info("--> " + result.getBalance());
                            Thread.sleep(100);
                            result = customerTestHelper.findCustomerById(currentCustomerId);
                        }
                        assertThat(result.getBalance().compareTo(new BigDecimal(afterMarketCloseBalance)), equalTo(0));
                    }, () -> String.format("Available customer balance after market close is incorrect for customerId = %s: amount = %.2f", currentCustomerId, customerTestHelper.findCustomerById(currentCustomerId).getBalance())
            );
        }

        //After market is closed, make sure that all bets are settled
        for (CompletableFuture<BetResponse> betFuture: createdBets) {
            Thread.sleep(50);
            BetResponse betResponse = betFuture.get();
            final BetData[] resultHolder = {betTestHelper.getState(betResponse.getBetId())};
            log.info("-->--> " + betResponse.getBetId());
            assertTimeoutPreemptively(
                    Duration.ofSeconds(120)
                    , () -> {
                        while (resultHolder[0].getStatus() != BetStatus.SETTLED) {
                            Thread.sleep(100);
                            resultHolder[0] = betTestHelper.getState(betResponse.getBetId());
                        }
                        assertThat(resultHolder[0].getStatus(), equalTo(BetStatus.SETTLED));
                    }, () -> String.format("Bet with betId = %s is not settled; currentStatus = %s", betResponse.getBetId(), resultHolder[0].getStatus())
            );
        }

        final MarketData[] marketDataHolder = {marketTestHelper.getMarketData(marketId)};
        log.info("--<<marketId>--> " + marketId);

        assertTimeoutPreemptively(
                Duration.ofSeconds(10)
                , () -> {
                    while (marketDataHolder[0].getStatus() != MarketStatus.SETTLED) {
                        Thread.sleep(100);
                        marketDataHolder[0] = marketTestHelper.getMarketData(marketId);
                    }
                    assertThat(marketDataHolder[0].getStatus(), equalTo(MarketStatus.SETTLED));
                    assertThat(marketDataHolder[0].isOpen(), equalTo(false));
                }, () -> String.format("Market with id = %s is not settled; currentStatus = %s", marketId, marketDataHolder[0].getStatus())
        );

        log.info("Elapsed time: " + (Instant.now().toEpochMilli() - start));
    }


}


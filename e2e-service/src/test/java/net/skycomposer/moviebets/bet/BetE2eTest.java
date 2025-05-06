package net.skycomposer.moviebets.bet;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import feign.FeignException;
import lombok.SneakyThrows;
import net.skycomposer.moviebets.common.E2eTest;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.BetResponse;
import net.skycomposer.moviebets.common.dto.customer.WalletData;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;
import net.skycomposer.moviebets.common.dto.market.MarketResponse;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.customer.CustomerTestHelper;
import net.skycomposer.moviebets.helper.RetryHelper;
import net.skycomposer.moviebets.market.MarketTestHelper;

@SpringBootTest
public class BetE2eTest extends E2eTest {

    @Autowired
    private CustomerTestHelper customerTestHelper;

    @Autowired
    private MarketTestHelper marketTestHelper;

    @Autowired
    private BetTestHelper betTestHelper;

    @Test
    @SneakyThrows
    void test() {
        UUID betId = UUID.randomUUID();
        UUID betId2 = UUID.randomUUID();
        UUID betId3 = UUID.randomUUID();
        String customerId = UUID.randomUUID().toString();
        UUID walletRequestId = UUID.randomUUID();
        int walletBalance = 100;
        UUID marketId = UUID.randomUUID();
        int betStake = 100;
        int betStake2 = 101;
        int betStake3 = 101;
        MarketResult marketResult = MarketResult.ITEM2_WINS;

        WalletResponse walletResponse = customerTestHelper.createWallet(customerId, walletRequestId, walletBalance);
        //Duplicate request with the same request id to make sure that duplicates are handled correctly
        try {
            walletResponse = customerTestHelper.createWallet(customerId, walletRequestId, walletBalance);
        } catch (FeignException.InternalServerError e) {
            //expected
        }
        assertThat(walletResponse.getMessage(), equalTo("Funds successfully increased from 100.0000 to 200.0000"));
        MarketResponse marketResponse = marketTestHelper.createMarket(marketId);
        assertThat(marketResponse.getMarketId(), equalTo(marketId));
        assertThat(marketResponse.getMessage(), equalTo("initialized"));

        BetResponse betResponse = betTestHelper.createBet(betId, marketId, customerId, betStake, marketResult);
        assertThat(betResponse.getBetId(), equalTo(betId));
        assertThat(betResponse.getMessage(), equalTo("initialized"));

        //Duplicate requests to make sure that opening the bet with the same id should be handled idempotently (only one bet open event should be handled, other duplicate events should be ignored)
        betResponse = betTestHelper.createBet(betId, marketId, customerId, betStake, marketResult);
        betResponse = betTestHelper.createBet(betId, marketId, customerId, betStake, marketResult);
        betResponse = betTestHelper.createBet(betId, marketId, customerId, betStake, marketResult);

        BetData betData =  RetryHelper.retry(() ->  betTestHelper.getState(betId));

        assertThat(betData.getCustomerId(), equalTo(customerId));
        assertThat(betData.getMarketId(), equalTo(marketId));
        assertThat(betData.getBetId(), equalTo(betId));
        assertThat(betData.getResult(), equalTo(marketResult));
        assertThat(betData.getStake(), equalTo(betStake));

        assertTimeoutPreemptively(
                Duration.ofSeconds(10)
                , () -> {
                    WalletData walletData = customerTestHelper.findWalletById(customerId);
                    while (walletData.getBalance().doubleValue() != 0) {
                        Thread.sleep(100);
                        walletData = customerTestHelper.findWalletById(customerId);
                    }
                    assertThat(walletData.getBalance(), equalTo(BigDecimal.ZERO));
                }, () -> "Wallet amount is not equal to 0; current amount = " + customerTestHelper.findWalletById(customerId).getBalance()
        );

        BetResponse betResponse2 = betTestHelper.createBet(betId2, marketId, customerId, betStake2, marketResult);
        assertThat(betResponse2.getBetId(), equalTo(betId2));
        assertThat(betResponse2.getMessage(), equalTo("initialized"));

        //Duplicate requests to make sure that opening the bet with the same id should be handled idempotently (only one bet open event should be handled, other duplicate events should be ignored)
        betResponse2 = betTestHelper.createBet(betId2, marketId, customerId, betStake2, marketResult);
        //betResponse2 = betTestHelper.createBet(betId2, marketId, walletId, betStake2, betOdds2, betResult);
        //betResponse2 = betTestHelper.createBet(betId2, marketId, walletId, betStake2, betOdds2, betResult);

        BetData betData2 =  RetryHelper.retry(() ->  betTestHelper.getState(betId2));

        assertThat(betData2.getCustomerId(), equalTo(customerId));
        assertThat(betData2.getMarketId(), equalTo(marketId));
        assertThat(betData2.getBetId(), equalTo(betId2));
        assertThat(betData2.getResult(), equalTo(marketResult));
        assertThat(betData2.getStake(), equalTo(betStake2));


        BetResponse betResponse3 = betTestHelper.createBet(betId3, marketId, customerId, betStake3,marketResult);
        assertThat(betResponse3.getBetId(), equalTo(betId3));
        assertThat(betResponse3.getMessage(), equalTo("initialized"));

        //Duplicate requests to make sure that opening the bet with the same id should be handled idempotently (only one bet open event should be handled, other duplicate events should be ignored)
        betResponse3 = betTestHelper.createBet(betId3, marketId, customerId, betStake3, marketResult);
        //betResponse3 = betTestHelper.createBet(betId3, marketId, walletId, betStake3, betOdds3, betResult);
        //betResponse3 = betTestHelper.createBet(betId3, marketId, walletId, betStake3, betOdds3, betResult);

        BetData betData3 =  RetryHelper.retry(() ->  betTestHelper.getState(betId3));

        assertThat(betData3.getCustomerId(), equalTo(customerId));
        assertThat(betData3.getMarketId(), equalTo(marketId));
        assertThat(betData3.getBetId(), equalTo(betId3));
        assertThat(betData3.getResult(), equalTo(marketResult));
        assertThat(betData3.getStake(), equalTo(betStake3));

        marketResponse = marketTestHelper.closeMarket(marketId, marketResult);
        assertThat(marketResponse.getMarketId(), equalTo(marketId));
        assertThat(marketResponse.getMessage(), equalTo("initialized"));

        assertTimeoutPreemptively(
                Duration.ofSeconds(10)
                , () -> {
                    WalletData walletData = customerTestHelper.findWalletById(customerId);
                    while (walletData.getBalance().doubleValue() != 100) {
                        Thread.sleep(100);
                        walletData = customerTestHelper.findWalletById(customerId);
                    }
                    assertThat(walletData.getBalance(), equalTo(100));
                }, () -> "Wallet amount is not equal to 100; current amount = " + customerTestHelper.findWalletById(customerId).getBalance()
        );

    }


}

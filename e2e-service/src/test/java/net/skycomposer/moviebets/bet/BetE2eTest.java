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
import net.skycomposer.moviebets.common.dto.customer.CustomerData;
import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;
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
        String customerId = UUID.randomUUID().toString();
        String customerId2 = UUID.randomUUID().toString();
        String customerId3 = UUID.randomUUID().toString();
        UUID fundRequestId = UUID.randomUUID();
        int customerBalance = 100;
        int betStake = 100;
        int betStake2 = 101;
        int betStake3 = 101;
        MarketResult marketResult = MarketResult.ITEM2_WINS;

        CustomerResponse customerResponse = customerTestHelper.createCustomer(customerId, fundRequestId, customerBalance);
        Thread.sleep(1200);
        //Duplicate request with the same request id to make sure that duplicates are handled correctly
        try {
            customerResponse = customerTestHelper.createCustomer(customerId, fundRequestId, customerBalance);
        } catch (FeignException.InternalServerError e) {
            //expected
        }
        assertThat(customerResponse.getMessage(), equalTo("Duplicate addFunds request for customer %s, requestId = %s".formatted(customerId, fundRequestId)));
        MarketResponse marketResponse = marketTestHelper.createMarket();

        UUID marketId = marketResponse.getMarketId();
        assertThat(marketResponse.getMarketId(), equalTo(marketId));
        assertThat(marketResponse.getMessage(), equalTo("Market %s opened successfully".formatted(marketId)));

        BetResponse betResponse = betTestHelper.createBet(marketId, customerId, betStake, marketResult);
        UUID betId = betResponse.getBetId();
        assertThat(betResponse.getBetId(), equalTo(betId));
        assertThat(betResponse.getMessage(), equalTo("Bet %s created successfully".formatted(betId)));

        //Duplicate requests to make sure that opening the bet with the same id should be handled idempotently (only one bet open event should be handled, other duplicate events should be ignored)
        betTestHelper.createBet(marketId, customerId, betStake, marketResult);
        betTestHelper.createBet(marketId, customerId, betStake, marketResult);
        betTestHelper.createBet(marketId, customerId, betStake, marketResult);

        BetData betData =  RetryHelper.retry(() ->  betTestHelper.getState(betId));

        assertThat(betData.getCustomerId(), equalTo(customerId));
        assertThat(betData.getMarketId(), equalTo(marketId));
        assertThat(betData.getBetId(), equalTo(betId));
        assertThat(betData.getResult(), equalTo(marketResult));
        assertThat(betData.getStake(), equalTo(betStake));

        assertTimeoutPreemptively(
                Duration.ofSeconds(10)
                , () -> {
                    CustomerData customerData = customerTestHelper.findCustomerById(customerId);
                    while (customerData.getBalance().doubleValue() != 0) {
                        Thread.sleep(100);
                        customerData = customerTestHelper.findCustomerById(customerId);
                    }
                    assertThat(customerData.getBalance().stripTrailingZeros(), equalTo(BigDecimal.ZERO));
                }, () -> "Customer balance is not equal to 0; current amount = " + customerTestHelper.findCustomerById(customerId).getBalance()
        );

        BetResponse betResponse2 = betTestHelper.createBet(marketId, customerId2, betStake2, marketResult);
        UUID betId2 = betResponse2.getBetId();
        assertThat(betResponse2.getMessage(), equalTo("Bet %s created successfully".formatted(betId2)));

        //Duplicate requests to make sure that opening the bet with the same id should be handled idempotently (only one bet open event should be handled, other duplicate events should be ignored)
        betTestHelper.createBet(marketId, customerId2, betStake2, marketResult);
        betTestHelper.createBet(marketId, customerId2, betStake2, marketResult);
        betTestHelper.createBet(marketId, customerId2, betStake2, marketResult);

        BetData betData2 =  RetryHelper.retry(() ->  betTestHelper.getState(betId2));

        assertThat(betData2.getCustomerId(), equalTo(customerId2));
        assertThat(betData2.getMarketId(), equalTo(marketId));
        assertThat(betData2.getBetId(), equalTo(betId2));
        assertThat(betData2.getResult(), equalTo(marketResult));
        assertThat(betData2.getStake(), equalTo(betStake2));


        BetResponse betResponse3 = betTestHelper.createBet(marketId, customerId3, betStake3,marketResult);
        UUID betId3 = betResponse3.getBetId();
        assertThat(betResponse3.getBetId(), equalTo(betId3));
        assertThat(betResponse3.getMessage(), equalTo("Bet %s created successfully".formatted(betId3)));

        //Duplicate requests to make sure that opening the bet with the same request id should be handled idempotently (only one bet open event should be handled, other duplicate events should be ignored)
        betTestHelper.createBet(marketId, customerId3, betStake3, marketResult);
        betTestHelper.createBet(marketId, customerId3, betStake3, marketResult);
        betTestHelper.createBet(marketId, customerId3, betStake3, marketResult);

        BetData betData3 =  RetryHelper.retry(() ->  betTestHelper.getState(betId3));

        assertThat(betData3.getCustomerId(), equalTo(customerId3));
        assertThat(betData3.getMarketId(), equalTo(marketId));
        assertThat(betData3.getBetId(), equalTo(betId3));
        assertThat(betData3.getResult(), equalTo(marketResult));
        assertThat(betData3.getStake(), equalTo(betStake3));

        marketResponse = marketTestHelper.closeMarket(marketId);
        assertThat(marketResponse.getMarketId(), equalTo(marketId));
        assertThat(marketResponse.getMessage(), equalTo("Request to close Market %s has been sent successfully".formatted(marketId)));

        assertTimeoutPreemptively(
                Duration.ofSeconds(10)
                , () -> {
                    CustomerData customerData = customerTestHelper.findCustomerById(customerId);
                    while (customerData.getBalance().doubleValue() != 100) {
                        Thread.sleep(100);
                        customerData = customerTestHelper.findCustomerById(customerId);
                    }
                    assertThat(customerData.getBalance().compareTo(new BigDecimal(100)), equalTo(0));
                }, () -> "Customer balance is not equal to 100; current balance = " + customerTestHelper.findCustomerById(customerId).getBalance()
        );

    }


}

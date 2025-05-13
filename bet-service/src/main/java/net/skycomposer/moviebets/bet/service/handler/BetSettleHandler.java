package net.skycomposer.moviebets.bet.service.handler;

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.BetStatus;
import net.skycomposer.moviebets.common.dto.bet.SumStakeData;
import net.skycomposer.moviebets.common.dto.bet.SumStakesData;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.CancelFundReservationCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundsReservedEvent;
import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCommand;
import net.skycomposer.moviebets.common.dto.market.commands.SettleBetsCommand;
import net.skycomposer.moviebets.common.dto.market.commands.SettleMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketCloseConfirmedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketCloseFailedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketSettledEvent;

@Component
@KafkaListener(topics = "${bet.settle.topic.name}", groupId = "${spring.kafka.consumer.bet-settle.group-id}")
public class BetSettleHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    private final String betCommandsTopicName;

    private final String marketCommandsTopicName;

    private final String customerCommandsTopicName;

    private final Integer betSettleBatchSize;

    public BetSettleHandler(
            BetService betService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${bet.commands.topic.name}") String betCommandsTopicName,
            @Value("${market.commands.topic.name}") String marketCommandsTopicName,
            @Value("${customer.commands.topic.name}") String customerCommandsTopicName,
            @Value("${bet.settle.batch.size}") Integer betSettleBatchSize
    ) {
        this.betService = betService;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
        this.betCommandsTopicName = betCommandsTopicName;
        this.marketCommandsTopicName = marketCommandsTopicName;
        this.customerCommandsTopicName = customerCommandsTopicName;
        this.betSettleBatchSize = betSettleBatchSize;
    }

    @KafkaHandler
    @Transactional
    public void handleEvent(@Payload FundsReservedEvent event) {
        boolean isMarketClosed = betService.isMarketClosed(event.getMarketId());
        if (isMarketClosed) {
            CancelFundReservationCommand cancelFundReservationCommand = new CancelFundReservationCommand(
                    event.getBetId(),
                    event.getCustomerId(),
                    event.getMarketId(),
                    event.getCancelRequestId(),
                    event.getFunds());
            kafkaTemplate.send(customerCommandsTopicName, event.getCustomerId(), cancelFundReservationCommand);
        } else {
            betService.setBetValidated(event.getBetId());
        }
    }

    @KafkaHandler
    @Transactional
    public void handleCloseMarketCommand(@Payload CloseMarketCommand command) {
        boolean isMarketClosed = betService.isMarketClosed(command.getMarketId());
        if (isMarketClosed) {
            return;
        }
        SumStakesData sumStakesData = betService.getBetsByMarket(command.getMarketId());
        if (winnerExists(sumStakesData)) {
            SumStakeData winner = getWinner(sumStakesData);
            MarketResult winResult = winner.getResult();
            BigDecimal winnerEarned = getTotalLost(sumStakesData, winner).divide(new BigDecimal(winner.getVotes()));
            Integer totalCount = betService.countByMarketIdAndStatus(command.getMarketId(), BetStatus.VALIDATED);
            MarketCloseConfirmedEvent marketCloseConfirmedEvent = new MarketCloseConfirmedEvent(command.getMarketId(), winResult);
            kafkaTemplate.send(marketCommandsTopicName, command.getMarketId().toString(), marketCloseConfirmedEvent);

            logger.info("Settle Bets Started for market {}: totalCount: {}", command.getMarketId(), totalCount);
            betService.marketSettleStart(command.getMarketId(), totalCount);
            SettleBetsCommand settleBetsCommand = new SettleBetsCommand(command.getMarketId(), winnerEarned, totalCount, winResult);
            kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), settleBetsCommand);
        } else {
            MarketCloseFailedEvent marketCloseFailedEvent = new MarketCloseFailedEvent(command.getMarketId());
            kafkaTemplate.send(marketCommandsTopicName, command.getMarketId().toString(), marketCloseFailedEvent);
        }
    }

    @KafkaHandler
    public void handleEvent(@Payload MarketSettledEvent event) {
        betService.marketSettleDone(event.getMarketId(), event.getWinResult());
    }

    @KafkaHandler
    @Transactional
    public void handleSettleBetsCommand(@Payload SettleBetsCommand command) {
        logger.info("Settle Bets Command Started for market {}: totalCount: {}", command.getMarketId(), command.getTotalCount());
        BigDecimal winnerEarned = command.getWinnerEarned();
        Integer totalCount = command.getTotalCount();
        MarketResult winResult = command.getWinnerResult();
        logger.info("Settle Bets Command winResult: {}", winResult);
        List<BetData> betsToSettle = betService.findByMarketAndStatus(command.getMarketId(), BetStatus.VALIDATED, betSettleBatchSize);
        if (CollectionUtils.isNotEmpty(betsToSettle)) {
            betService.updateStatus(betsToSettle.stream().map(BetData::getBetId).toList(), BetStatus.SETTLE_STARTED);
        }
        for (BetData betData: betsToSettle) {
            SettleBetCommand settleBetCommand = new SettleBetCommand(
                    betData.getBetId(),
                    betData.getCustomerId(),
                    betData.getMarketId(),
                    betData.getBetId(),
                    betData.getStake(),
                    winnerEarned,
                    isWinner(betData, winResult));
            kafkaTemplate.send(betCommandsTopicName, betData.getBetId().toString(), settleBetCommand);
        }

        Integer totalSettled = betService.countSettledBets(command.getMarketId());
        logger.info("Settle Bets: betsToSettle: {}, totalCount: {}, totalSettled: {}", betsToSettle.size(), totalCount, totalSettled);

        if (CollectionUtils.isNotEmpty(betsToSettle)) {
            settleBets(command, winnerEarned, totalCount, winResult);
        } else {
            if (totalSettled.intValue() != totalCount.intValue()) {
                settleBets(command, winnerEarned, totalCount, winResult);
            } else {
                SettleMarketCommand settleMarketCommand = new SettleMarketCommand(command.getMarketId(), winResult);
                kafkaTemplate.send(marketCommandsTopicName, command.getMarketId().toString(), settleMarketCommand);
            }
        }
    }

    private boolean isWinner(BetData betData, MarketResult winnerResult) {
        return betData.getResult() == winnerResult;
    }

    private void settleBets(SettleBetsCommand command, BigDecimal winnerEarned, Integer totalCount, MarketResult winResult) {
        SettleBetsCommand settleBetsCommand = new SettleBetsCommand(
                command.getMarketId(),
                winnerEarned,
                totalCount,
                winResult);
        kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), settleBetsCommand);
    }

    private boolean winnerExists(SumStakesData sumStakesData) {
        if (sumStakesData.getSumStakes().size() == 0) {
            return false;
        }
        if (sumStakesData.getSumStakes().size() == 1) {
            return sumStakesData.getSumStakes().get(0).getVotes() > 0;
        }
        SumStakeData one = sumStakesData.getSumStakes().get(0);
        SumStakeData two = sumStakesData.getSumStakes().get(0);
        return one.getVotes().longValue() != two.getVotes().longValue();
    }

    private SumStakeData getWinner(SumStakesData sumStakesData) {
        if (sumStakesData.getSumStakes().size() == 0) {
            return null;
        }
        if (sumStakesData.getSumStakes().size() == 1) {
            return sumStakesData.getSumStakes().get(0);
        }
        SumStakeData candidate1 = sumStakesData.getSumStakes().get(0);
        SumStakeData candidate2 = sumStakesData.getSumStakes().get(1);
        if (candidate1.getVotes().longValue() == candidate2.getVotes().longValue()) {
            return null;
        }
        return candidate1.getVotes().longValue() > candidate2.getVotes().longValue() ? candidate1 : candidate2;
    }

    private BigDecimal getTotalLost(SumStakesData sumStakesData, SumStakeData winner) {
        BigDecimal totalLost = BigDecimal.ZERO;
        if (sumStakesData.getSumStakes().size() < 2) {
            return totalLost;
        }
        for (int i = 0; i < sumStakesData.getSumStakes().size(); i++) {
            SumStakeData candidate = sumStakesData.getSumStakes().get(i);
            if (candidate.getResult() != winner.getResult()) {
                totalLost = totalLost.add(new BigDecimal(candidate.getTotal()));
            }
        }
        return totalLost;
    }

}

package net.skycomposer.moviebets.bet.service.handler;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.BetStatus;
import net.skycomposer.moviebets.common.dto.bet.SumStakeData;
import net.skycomposer.moviebets.common.dto.bet.SumStakesData;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetCommand;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.customer.commands.CancelFundReservationCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundsAddedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsReservedEvent;
import net.skycomposer.moviebets.common.dto.market.commands.SettleBetsCommand;
import net.skycomposer.moviebets.common.dto.market.commands.SettleMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketClosedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketSettledEvent;

@Component
@KafkaListener(topics = "${bet.settle.topic.name}", groupId = "${kafka.consumer.settle.group-id}")
@RequiredArgsConstructor
public class BetSettleHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bet.settle.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betSettleTopicName;

    @Value("${bet.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betCommandsTopicName;

    @Value("${market.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String marketCommandsTopicName;

    @Value("${customer.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String customerCommandsTopicName;

    @Value("${bet.settle.batch.size}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final Integer betSettleBatchSize;

    @KafkaHandler
    public void handleEvent(@Payload BetCreatedEvent event) {
        boolean isMarketClosed = betService.isMarketClosed(event.getMarketId());
        if (isMarketClosed) {
            RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(),
                    "Bet %s was rejected, because market %s is already closed".formatted(event.getBetId(), event.getMarketId()));
            kafkaTemplate.send(betCommandsTopicName, event.getBetId().toString(), rejectBetCommand);
            return;
        }
        ReserveFundsCommand reserveFundsCommand = new ReserveFundsCommand(
                event.getBetId(),
                event.getCustomerId(),
                event.getMarketId(),
                event.getRequestId(),
                event.getCancelRequestId(),
                new BigDecimal(event.getStake())
        );
        kafkaTemplate.send(customerCommandsTopicName, event.getCustomerId(), reserveFundsCommand);
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
    public void handleEvent(@Payload FundsAddedEvent event) {
        betService.updateMarketSettleCount(event.getBetId(), event.getMarketId());
    }

    @KafkaHandler
    @Transactional
    public void handleEvent(@Payload MarketClosedEvent event) {
        int expectedCount = betService.countByStatus(BetStatus.VALIDATED);
        betService.marketSettleStart(event.getMarketId(), expectedCount);
        SettleBetsCommand settleBetsCommand = new SettleBetsCommand(event.getMarketId(), UUID.randomUUID().toString(), expectedCount);
        kafkaTemplate.send(betSettleTopicName, event.getMarketId().toString(), settleBetsCommand);
    }

    @KafkaHandler
    @Transactional
    public void handleEvent(@Payload MarketSettledEvent event) {
        betService.marketSettleDone(event.getMarketId());
        betService.updateStatus(event.getMarketId(), BetStatus.VALIDATED, BetStatus.SETTLED);
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload SettleBetsCommand command) {
        BigDecimal winnerEarned = command.getWinnerEarned();
        BigDecimal totalLost = command.getTotalLost();
        Integer totalCount = command.getTotalCount();
        if (winnerEarned == null) {
            SumStakesData sumStakesData = betService.getBetsByMarket(command.getMarketId());
            SumStakeData winner = getWinner(sumStakesData);
            if (winner != null) {
                winnerEarned = getTotalLost(sumStakesData, winner).divide(new BigDecimal(winner.getVotes()));
            }
            if (totalCount == null) {
                totalCount = betService.countByStatus(BetStatus.VALIDATED);
            }
        }
        if (winnerEarned != null) {
            List<BetData> betsToSettle = betService.findByMarketAndStatus(command.getMarketId(), BetStatus.VALIDATED, betSettleBatchSize);
            if (CollectionUtils.isNotEmpty(betsToSettle)) {
                betService.updateStatus(betsToSettle.stream().map(BetData::getBetId).toList(), BetStatus.SETTLE_READY);
            }
            for (BetData betData: betsToSettle) {
                SettleBetCommand settleBetCommand = new SettleBetCommand(
                        betData.getBetId(),
                        betData.getCustomerId(),
                        betData.getMarketId(),
                        betData.getBetId(),
                        betData.getStake(),
                        winnerEarned);
                kafkaTemplate.send(betCommandsTopicName, betData.getBetId().toString(), settleBetCommand);
            }

            if (CollectionUtils.isNotEmpty(betsToSettle)) {
                settleBets(command, winnerEarned, totalLost, totalCount);
            } else {
                Integer totalSettled = betService.countSettledBets(command.getMarketId());
                if (totalSettled != totalCount) {
                    settleBets(command, winnerEarned, totalLost, totalCount);
                } else {
                    SettleMarketCommand settleMarketCommand = new SettleMarketCommand(command.getMarketId());
                    kafkaTemplate.send(marketCommandsTopicName, command.getMarketId().toString(), settleMarketCommand);
                }
            }
        }
    }

    private void settleBets(SettleBetsCommand command, BigDecimal winnerEarned, BigDecimal totalLost, Integer totalCount) {
        SettleBetsCommand settleBetsCommand = new SettleBetsCommand(
                command.getMarketId(),
                command.getRequestId(),
                winnerEarned,
                totalLost,
                totalCount);
        kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), settleBetsCommand);
    }

    private SumStakeData getWinner(SumStakesData sumStakesData) {
        if (sumStakesData.getSumStakes().size() == 0) {
            return null;
        }
        SumStakeData max = sumStakesData.getSumStakes().get(0);
        for (int i = 1; i < sumStakesData.getSumStakes().size(); i++) {
            SumStakeData candidate = sumStakesData.getSumStakes().get(i);
            if (candidate.getTotal().doubleValue() > max.getTotal().doubleValue()) {
                max = candidate;
            }

        }
        return max;
    }

    private BigDecimal getTotalLost(SumStakesData sumStakesData, SumStakeData winner) {
        BigDecimal totalLost = BigDecimal.ZERO;
        for (int i = 0; i < sumStakesData.getSumStakes().size(); i++) {
            SumStakeData candidate = sumStakesData.getSumStakes().get(i);
            if (candidate.getResult() != winner.getResult()) {
                totalLost = totalLost.add(new BigDecimal(candidate.getTotal()));
            }
        }
        return totalLost;
    }

}

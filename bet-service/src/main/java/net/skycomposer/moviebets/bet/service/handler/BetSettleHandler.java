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

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.*;
import net.skycomposer.moviebets.common.dto.bet.commands.ApproveBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetCommand;
import net.skycomposer.moviebets.common.dto.market.commands.SettleBetsCommand;

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

    @Value("${bet.settle.batch.size}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final Integer betSettleBatchSize;

    @KafkaHandler
    public void handleCommand(@Payload RejectBetCommand rejectBetCommand) {
        CancelBetRequest cancelBetRequest = new CancelBetRequest(rejectBetCommand.getBetId(), rejectBetCommand.getReason());
        betService.close(cancelBetRequest);
    }

    @KafkaHandler
    public void handleCommand(@Payload ApproveBetCommand approveBetCommand) {
        betService.updateStatus(List.of(approveBetCommand.getBetId()), BetStatus.VALIDATED);
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload SettleBetsCommand command) {
        BigDecimal winnerEarned = command.getWinnerEarned();
        BigDecimal totalLost = command.getTotalLost();
        Long totalCount = command.getTotalCount();
        if (winnerEarned == null) {
            SumStakesData sumStakesData = betService.getBetsByMarket(command.getMarketId());
            SumStakeData winner = getWinner(sumStakesData);
            if (winner != null) {
                winnerEarned = getTotalLost(sumStakesData, winner).divide(new BigDecimal(winner.getVotes()));
            }
            totalCount = betService.countByStatus(BetStatus.VALIDATED);
        }
        if (winnerEarned != null) {
            List<BetData> betsToCancel = betService.findByMarketAndStatus(command.getMarketId(), BetStatus.PlACED, betSettleBatchSize);
            for (BetData betData: betsToCancel) {
                RejectBetCommand rejectBetCommand = new RejectBetCommand(betData.getBetId(),
                        "Bet %s was rejected, because Market %s is already closed".formatted(betData.getBetId(), betData.getMarketId()));
                kafkaTemplate.send(betCommandsTopicName, command.getMarketId().toString(), rejectBetCommand);
            }

            List<BetData> betsToSettle = betService.findByMarketAndStatus(command.getMarketId(), BetStatus.VALIDATED, betSettleBatchSize);
            betService.updateStatus(betsToSettle.stream().map(BetData::getBetId).toList(), BetStatus.SETTLE_READY);

            List<BetData> betsReady = betService.findByMarketAndStatus(command.getMarketId(), BetStatus.SETTLE_READY, betSettleBatchSize);
            for (BetData betData: betsReady) {
                SettleBetCommand settleBetCommand = new SettleBetCommand(
                        betData.getBetId(),
                        betData.getCustomerId(),
                        betData.getMarketId(),
                        command.getRequestId(),
                        betData.getStake(),
                        winnerEarned);
                kafkaTemplate.send(betCommandsTopicName, betData.getBetId().toString(), settleBetCommand);
            }

            if (CollectionUtils.isNotEmpty(betsToCancel)
                    || CollectionUtils.isNotEmpty(betsToSettle)
                    || CollectionUtils.isNotEmpty(betsReady)) {
                SettleBetsCommand settleBetsCommand = new SettleBetsCommand(
                        command.getMarketId(),
                        command.getRequestId(),
                        winnerEarned,
                        totalLost,
                        totalCount);
                kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), settleBetsCommand);
            } else {
                Long totalSettled = betService.countByStatus(BetStatus.SETTLED);
                if (totalSettled != totalCount) {
                    throw new IllegalArgumentException(
                            "Initial total count of validated bets (%d) is not equal to total count of settled bets (%d): Please, notify developer about this issue"
                                    .formatted(totalCount, totalSettled));
                }
            }
        }
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

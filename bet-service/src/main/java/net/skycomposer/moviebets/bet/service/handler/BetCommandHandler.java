package net.skycomposer.moviebets.bet.service.handler;

import java.math.BigDecimal;
import java.time.Instant;

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
import net.skycomposer.moviebets.common.dto.bet.CancelBetRequest;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetCommand;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.bet.events.BetSettledEvent;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.SettleFundsCommand;

@Component
@KafkaListener(topics = "${bet.commands.topic.name}", groupId = "${spring.kafka.consumer.bet-commands.group-id}")
public class BetCommandHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleJobTopicName;

    private final String customerCommandsTopicName;

    private final String betCommandsTopicName;

    private final Integer customerCommandsRetryCount;

    private final Integer customerCommandsRetryTimeoutSeconds;

    public BetCommandHandler(BetService betService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle-job.topic.name}") String betSettleJobTopicName,
            @Value("${customer.commands.topic.name}") String customerCommandsTopicName,
            @Value("${bet.commands.topic.name}") String betCommandsTopicName,
            @Value("${customer.commands.retry.count}") Integer customerCommandsRetryCount,
            @Value("${customer.commands.retry.timeout-seconds}") Integer customerCommandsRetryTimeoutSeconds

    ) {
        this.betService = betService;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleJobTopicName = betSettleJobTopicName;
        this.customerCommandsTopicName = customerCommandsTopicName;
        this.betCommandsTopicName = betCommandsTopicName;
        this.customerCommandsRetryCount = customerCommandsRetryCount;
        this.customerCommandsRetryTimeoutSeconds = customerCommandsRetryTimeoutSeconds;
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectBetCommand rejectBetCommand) {
        CancelBetRequest cancelBetRequest = new CancelBetRequest(rejectBetCommand.getBetId(), rejectBetCommand.getReason());
        betService.cancel(cancelBetRequest, rejectBetCommand.getCustomerId(), false);
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload SettleBetCommand settleBetCommand) {
        if (settleBetCommand.isWinner()) {
            SettleFundsCommand settleFundsCommand = new SettleFundsCommand(
                    settleBetCommand.getBetId(),
                    settleBetCommand.getCustomerId(),
                    settleBetCommand.getMarketId(),
                    settleBetCommand.getRequestId(),
                    settleBetCommand.getWinnerEarned().add(new BigDecimal(settleBetCommand.getStake())));
            kafkaTemplate.send(customerCommandsTopicName, settleBetCommand.getCustomerId(), settleFundsCommand);
        } else {
            BetSettledEvent betSettledEvent = new BetSettledEvent(settleBetCommand.getBetId(), settleBetCommand.getMarketId());
            kafkaTemplate.send(betSettleJobTopicName, settleBetCommand.getMarketId().toString(), betSettledEvent);
        }
    }

    @KafkaHandler
    @Transactional("kafkaTransactionManager")
    public void handleBetCreatedEvent(@Payload BetCreatedEvent event) {
        boolean isMarketClosed = betService.isMarketClosed(event.getMarketId());
        if (isMarketClosed) {
            RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(), event.getCustomerId(),
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
                new BigDecimal(event.getStake()),
                1,
                customerCommandsRetryCount,
                customerCommandsRetryTimeoutSeconds,
                Instant.now()
        );
        kafkaTemplate.send(customerCommandsTopicName, event.getCustomerId(), reserveFundsCommand);
    }

}

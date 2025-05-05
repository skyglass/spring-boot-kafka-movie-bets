package net.skycomposer.moviebets.bet.service.handler;

import java.math.BigDecimal;

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
import net.skycomposer.moviebets.common.dto.bet.events.BetSettledEvent;
import net.skycomposer.moviebets.common.dto.customer.commands.AddFundsCommand;

@Component
@KafkaListener(topics = "${bet.commands.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
public class BetCommandHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    private final String customerCommandsTopicName;

    public BetCommandHandler(BetService betService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${customer.commands.topic.name}") String customerCommandsTopicName
    ) {
        this.betService = betService;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
        this.customerCommandsTopicName = customerCommandsTopicName;
    }

    @KafkaHandler
    public void handleCommand(@Payload RejectBetCommand rejectBetCommand) {
        CancelBetRequest cancelBetRequest = new CancelBetRequest(rejectBetCommand.getBetId(), rejectBetCommand.getReason());
        betService.close(cancelBetRequest);
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload SettleBetCommand settleBetCommand) {
        if (settleBetCommand.isWinner()) {
            AddFundsCommand addFundsCommand = new AddFundsCommand(
                    settleBetCommand.getBetId(),
                    settleBetCommand.getCustomerId(),
                    settleBetCommand.getMarketId(),
                    settleBetCommand.getRequestId(),
                    settleBetCommand.getWinnerEarned().add(new BigDecimal(settleBetCommand.getStake())));
            kafkaTemplate.send(customerCommandsTopicName, settleBetCommand.getCustomerId(), addFundsCommand);
        } else {
            BetSettledEvent betSettledEvent = new BetSettledEvent(settleBetCommand.getBetId(), settleBetCommand.getMarketId());
            kafkaTemplate.send(betSettleTopicName, settleBetCommand.getMarketId().toString(), betSettledEvent);
        }
    }

}

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

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.CancelBetRequest;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.AddFundsCommand;

@Component
@KafkaListener(topics = "${bet.commands.topic.name}", groupId = "${kafka.consumer.commands.group-id}")
@RequiredArgsConstructor
public class BetCommandHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${customer.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String customerCommandsTopicName;

    @KafkaHandler
    public void handleCommand(@Payload RejectBetCommand rejectBetCommand) {
        CancelBetRequest cancelBetRequest = new CancelBetRequest(rejectBetCommand.getBetId(), rejectBetCommand.getReason());
        betService.close(cancelBetRequest);
    }

    @KafkaHandler
    public void handleCommand(@Payload SettleBetCommand settleBetCommand) {
        AddFundsCommand addFundsCommand = new AddFundsCommand(
                settleBetCommand.getBetId(),
                settleBetCommand.getCustomerId(),
                settleBetCommand.getMarketId(),
                settleBetCommand.getRequestId(),
                settleBetCommand.getWinnerEarned().add(new BigDecimal(settleBetCommand.getStake())));
        kafkaTemplate.send(customerCommandsTopicName, settleBetCommand.getCustomerId(), addFundsCommand);
    }

}

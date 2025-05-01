package net.skycomposer.moviebets.bet.saga;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.commands.ApproveBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.customer.commands.CancelFundReservationCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservedEvent;
import net.skycomposer.moviebets.common.dto.market.commands.ValidateMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketValidatedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketValidationFailedEvent;

@Component
@KafkaListener(topics={
        "${bet.events.topic.name}",
        "${market.events.topic.name}",
        "${customer.events.topic.name}"
})
public class BetSaga {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final BetService betService;
    private final String marketCommandsTopicName;
    private final String customerCommandsTopicName;
    private final String betCommandsTopicName;

    public BetSaga(KafkaTemplate<String, Object> kafkaTemplate,
                     BetService betService,
                     @Value("${market.commands.topic.name}") String marketCommandsTopicName,
                     @Value("${customer.commands.topic.name}") String customerCommandsTopicName,
                     @Value("${bet.commands.topic.name}") String betCommandsTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.betService = betService;
        this.marketCommandsTopicName = marketCommandsTopicName;
        this.customerCommandsTopicName = customerCommandsTopicName;
        this.betCommandsTopicName = betCommandsTopicName;
    }

    @KafkaHandler
    public void handleEvent(@Payload BetCreatedEvent event) {

        ReserveFundsCommand reserveFundsCommand = new ReserveFundsCommand(
                event.getBetId(),
                event.getCustomerId(),
                event.getMarketId(),
                UUID.randomUUID().toString(),
                new BigDecimal(event.getStake())
        );

        kafkaTemplate.send(customerCommandsTopicName, reserveFundsCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload FundReservedEvent event) {
        ValidateMarketCommand validateMarketCommand = new ValidateMarketCommand(event.getBetId(),event.getMarketId());
        kafkaTemplate.send(marketCommandsTopicName, validateMarketCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload MarketValidatedEvent event) {
        ApproveBetCommand approveBetCommand = new ApproveBetCommand(event.getBetId());
        kafkaTemplate.send(betCommandsTopicName, approveBetCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload MarketValidationFailedEvent event) {
        BetData betData = betService.findBetById(event.getBetId());
        CancelFundReservationCommand cancelFundReservationCommand = new CancelFundReservationCommand(
                event.getBetId(),
                betData.getCustomerId(),
                UUID.randomUUID().toString(),
                new BigDecimal(betData.getStake()));
        kafkaTemplate.send(customerCommandsTopicName, cancelFundReservationCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload FundReservationCancelledEvent event) {
        RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId());
        kafkaTemplate.send(betCommandsTopicName, rejectBetCommand);
    }
}

package net.skycomposer.moviebets.bet.saga;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.bet.service.BetService;
import net.skycomposer.moviebets.common.dto.bet.BetData;
import net.skycomposer.moviebets.common.dto.bet.commands.ApproveBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.bet.commands.SettleBetStatusCommand;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.customer.commands.CancelFundReservationCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsAddedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsReservedEvent;
import net.skycomposer.moviebets.common.dto.market.commands.SettleBetsCommand;
import net.skycomposer.moviebets.common.dto.market.commands.ValidateMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketClosedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketValidatedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketValidationFailedEvent;

@Component
@KafkaListener(
    topics = {
            "${bet.events.topic.name}",
            "${market.events.topic.name}",
            "${customer.events.topic.name}"
    },
    groupId = "${kafka.consumer.group-id}"
)
@RequiredArgsConstructor
public class BetSaga {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final BetService betService;

    @Value("${bet.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betCommandsTopicName;

    @Value("${market.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String marketCommandsTopicName;

    @Value("${customer.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String customerCommandsTopicName;

    @Value("${bet.settle.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betSettleTopicName;

    @KafkaHandler
    public void handleEvent(@Payload BetCreatedEvent event) {

        ReserveFundsCommand reserveFundsCommand = new ReserveFundsCommand(
                event.getBetId(),
                event.getCustomerId(),
                event.getMarketId(),
                UUID.randomUUID().toString(),
                new BigDecimal(event.getStake())
        );

        kafkaTemplate.send(customerCommandsTopicName, event.getCustomerId(), reserveFundsCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload FundsReservedEvent event) {
        ValidateMarketCommand validateMarketCommand = new ValidateMarketCommand(event.getBetId(),event.getMarketId());
        kafkaTemplate.send(marketCommandsTopicName, event.getMarketId().toString(), validateMarketCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload FundsAddedEvent event) {
        SettleBetStatusCommand settleBetStatusCommand = new SettleBetStatusCommand(event.getBetId());
        kafkaTemplate.send(betCommandsTopicName, event.getBetId().toString(), settleBetStatusCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload MarketValidatedEvent event) {
        ApproveBetCommand approveBetCommand = new ApproveBetCommand(event.getBetId());
        kafkaTemplate.send(betSettleTopicName, event.getMarketId().toString(), approveBetCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload MarketClosedEvent event) {
        SettleBetsCommand settleBetsCommand = new SettleBetsCommand(event.getMarketId(), UUID.randomUUID().toString());
        kafkaTemplate.send(marketCommandsTopicName, event.getMarketId().toString(), settleBetsCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload MarketValidationFailedEvent event) {
        BetData betData = betService.findBetById(event.getBetId());
        CancelFundReservationCommand cancelFundReservationCommand = new CancelFundReservationCommand(
                event.getBetId(),
                betData.getCustomerId(),
                betData.getMarketId(),
                UUID.randomUUID().toString(),
                new BigDecimal(betData.getStake()));
        kafkaTemplate.send(customerCommandsTopicName, betData.getCustomerId(), cancelFundReservationCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload FundReservationCancelledEvent event) {
        RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(),
                "Bet %s was rejected and fund reservation has been successfully cancelled".formatted(event.getBetId()));
        kafkaTemplate.send(betSettleTopicName, event.getMarketId().toString(), rejectBetCommand);
    }
}

package net.skycomposer.moviebets.customer.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.customer.WalletResponse;
import net.skycomposer.moviebets.common.dto.customer.commands.AddFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.CancelFundReservationCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationFailedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsAddedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsReservedEvent;
import net.skycomposer.moviebets.customer.exception.CustomerInsufficientFundsException;
import net.skycomposer.moviebets.customer.service.CustomerService;

@Component
@KafkaListener(topics = "${customer.commands.topic.name}", groupId = "${kafka.consumer.group-id}")
@RequiredArgsConstructor
public class CustomerCommandHandler {

    private final CustomerService customerService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bet.events.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betEventsTopicName;

    @Value("${bet.settle.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betSettleTopicName;

    @KafkaHandler
    public void handleCommand(@Payload ReserveFundsCommand command) {
        try {
            WalletResponse walletResponse = customerService.removeFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
            FundsReservedEvent fundsReservedEvent = new FundsReservedEvent(
                    command.getBetId(), command.getCustomerId(),
                    command.getCancelRequestId(),
                    command.getMarketId(), command.getFunds(), walletResponse.getCurrentBalance());
            kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), fundsReservedEvent);
        } catch (CustomerInsufficientFundsException e) {
            logger.error(e.getLocalizedMessage(), e);
            FundReservationFailedEvent fundReservationFailedEvent = new FundReservationFailedEvent(
                    command.getBetId(), command.getCustomerId(),
                    e.getRequiredAmount(), e.getAvailableAmount());
            kafkaTemplate.send(betEventsTopicName, command.getBetId().toString(), fundReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelFundReservationCommand command) {
        WalletResponse walletResponse = customerService.addFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
        FundReservationCancelledEvent fundReservationCancelledEvent = new FundReservationCancelledEvent(
                command.getBetId(), command.getCustomerId(),
                command.getMarketId(),
                command.getFunds(), walletResponse.getCurrentBalance());
        kafkaTemplate.send(betEventsTopicName, command.getBetId().toString(), fundReservationCancelledEvent);
    }

    @KafkaHandler
    public void handleCommand(@Payload AddFundsCommand command) {
        WalletResponse walletResponse = customerService.addFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
        FundsAddedEvent fundsAddedEvent = new FundsAddedEvent(
                command.getBetId(), command.getCustomerId(),
                command.getMarketId(),
                command.getFunds(), walletResponse.getCurrentBalance());
        kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), fundsAddedEvent);
    }
}

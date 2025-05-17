package net.skycomposer.moviebets.customer.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.customer.CustomerResponse;
import net.skycomposer.moviebets.common.dto.customer.commands.*;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationFailedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsReservedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundsSettledEvent;
import net.skycomposer.moviebets.customer.exception.CustomerInsufficientFundsException;
import net.skycomposer.moviebets.customer.service.CustomerService;

import java.time.Duration;

import static java.time.Instant.now;

@Component
@KafkaListener(topics = "${customer.commands.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
public class CustomerCommandHandler {

    private final CustomerService customerService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String customerEventsTopicName;

    private final String customerCommandsTopicName;

    private final String betSettleTopicName;

    private final String betSettleJobTopicName;

    private final String customerEventsDlqTopicName;

    public CustomerCommandHandler(
            CustomerService customerService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${customer.events.topic.name}") String customerEventsTopicName,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${bet.settle-job.topic.name}") String betSettleJobTopicName,
            @Value("${customer.commands.topic.name}") String customerCommandsTopicName,
            @Value("${customer.events.dlq.topic.name}") String customerEventsDlqTopicName
    ) {
        this.customerService = customerService;
        this.kafkaTemplate = kafkaTemplate;
        this.customerEventsTopicName = customerEventsTopicName;
        this.betSettleTopicName = betSettleTopicName;
        this.betSettleJobTopicName = betSettleJobTopicName;
        this.customerCommandsTopicName = customerCommandsTopicName;
        this.customerEventsDlqTopicName = customerEventsDlqTopicName;
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload ReserveFundsCommand command) {
        try {
            CustomerResponse customerResponse = customerService.removeFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
            FundsReservedEvent fundsReservedEvent = new FundsReservedEvent(
                    command.getBetId(), command.getCustomerId(),
                    command.getCancelRequestId(),
                    command.getMarketId(), command.getFunds(), customerResponse.getCurrentBalance());
            kafkaTemplate.send(betSettleTopicName, command.getMarketId().toString(), fundsReservedEvent);
        } catch (CustomerInsufficientFundsException e) {
            boolean isRetryTimeout = now().minus(Duration.ofSeconds(command.getRetryTimeoutSeconds())).isAfter(command.getRetryStart());
            if (isRetryTimeout
                    || (command.getTotalRetries().intValue() != -1
                        && (command.getRetryCount().intValue() == command.getTotalRetries().intValue()))) {
                logger.error(e.getLocalizedMessage(), e);
                FundReservationFailedEvent fundReservationFailedEvent = new FundReservationFailedEvent(
                        command.getBetId(), command.getCustomerId(),
                        e.getRequiredAmount(), e.getAvailableAmount());
                kafkaTemplate.send(customerEventsDlqTopicName, command.getCustomerId().toString(), fundReservationFailedEvent);
            } else {
                ReserveFundsCommand reserveFundsCommand = new ReserveFundsCommand(
                        command.getBetId(),
                        command.getCustomerId(),
                        command.getMarketId(),
                        command.getRequestId(),
                        command.getCancelRequestId(),
                        command.getFunds(),
                        command.getRetryCount() + 1,
                        command.getTotalRetries(),
                        command.getRetryTimeoutSeconds(),
                        command.getRetryStart()
                );
                kafkaTemplate.send(customerCommandsTopicName, command.getCustomerId(), reserveFundsCommand);
            }
        }
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload CancelFundReservationCommand command) {
        CustomerResponse customerResponse = customerService.addFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
        FundReservationCancelledEvent fundReservationCancelledEvent = new FundReservationCancelledEvent(
                command.getBetId(), command.getCustomerId(),
                command.getMarketId(),
                command.getFunds(), customerResponse.getCurrentBalance());
        kafkaTemplate.send(customerEventsTopicName, command.getCustomerId().toString(), fundReservationCancelledEvent);
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload SettleFundsCommand command) {
        CustomerResponse customerResponse = customerService.addFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
        FundsSettledEvent fundsSettledEvent = new FundsSettledEvent(
                command.getBetId(), command.getCustomerId(),
                command.getMarketId(),
                command.getFunds(), customerResponse.getCurrentBalance());
        kafkaTemplate.send(betSettleJobTopicName, command.getMarketId().toString(), fundsSettledEvent);
    }

    @KafkaHandler
    public void handleCommand(@Payload AddFundsCommand command) {
        customerService.addFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
    }

    @KafkaHandler
    @Transactional
    public void handleCommand(@Payload RemoveFundsCommand command) {
        try {
            customerService.removeFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
        } catch (CustomerInsufficientFundsException e) {
            logger.error(e.getLocalizedMessage(), e);
            FundReservationFailedEvent fundReservationFailedEvent = new FundReservationFailedEvent(
                    command.getRequestId(), command.getCustomerId(),
                    e.getRequiredAmount(), e.getAvailableAmount());
            kafkaTemplate.send(customerEventsDlqTopicName, command.getCustomerId().toString(), fundReservationFailedEvent);
        }
    }
}

package net.skycomposer.moviebets.customer.service.handler;

import net.skycomposer.moviebets.common.dto.customer.WalletResponse;
import net.skycomposer.moviebets.common.dto.customer.commands.CancelFundReservationCommand;
import net.skycomposer.moviebets.common.dto.customer.commands.ReserveFundsCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationFailedEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationSucceededEvent;
import net.skycomposer.moviebets.customer.exception.CustomerInsufficientFundsException;
import net.skycomposer.moviebets.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${customer.commands.topic.name}")
public class CustomerCommandHandler {

    private final CustomerService customerService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String customerEventsTopicName;

    public CustomerCommandHandler(CustomerService customerService,
                                  KafkaTemplate<String, Object> kafkaTemplate,
                                  @Value("${customer.events.topic.name}") String customerEventsTopicName) {
        this.customerService = customerService;
        this.kafkaTemplate = kafkaTemplate;
        this.customerEventsTopicName = customerEventsTopicName;
    }

    @KafkaHandler
    public void handleCommand(@Payload ReserveFundsCommand command) {
        try {
            WalletResponse walletResponse = customerService.removeFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
            FundReservationSucceededEvent fundReservationSucceededEvent = new FundReservationSucceededEvent(command.getCustomerId(),
                    command.getFunds(), walletResponse.getCurrentBalance());
            kafkaTemplate.send(customerEventsTopicName, fundReservationSucceededEvent);
        } catch (CustomerInsufficientFundsException e) {
            logger.error(e.getLocalizedMessage(), e);
            FundReservationFailedEvent fundReservationFailedEvent = new FundReservationFailedEvent(command.getCustomerId(),
                            e.getRequiredAmount(), e.getAvailableAmount());
            kafkaTemplate.send(customerEventsTopicName, fundReservationFailedEvent);
        }
    }

    @KafkaHandler
    public void handleCommand(@Payload CancelFundReservationCommand command) {
        try {
            WalletResponse walletResponse = customerService.addFunds(command.getCustomerId(), command.getRequestId(), command.getFunds());
            FundReservationCancelledEvent fundReservationCancelledEvent = new FundReservationCancelledEvent(command.getCustomerId(),
                    command.getFunds(), walletResponse.getCurrentBalance());
            kafkaTemplate.send(customerEventsTopicName, fundReservationCancelledEvent);
        } catch (CustomerInsufficientFundsException e) {
            logger.error(e.getLocalizedMessage(), e);
            FundReservationFailedEvent fundReservationFailedEvent = new FundReservationFailedEvent(command.getCustomerId(),
                    e.getRequiredAmount(), e.getAvailableAmount());
            kafkaTemplate.send(customerEventsTopicName, fundReservationFailedEvent);
        }
    }
}

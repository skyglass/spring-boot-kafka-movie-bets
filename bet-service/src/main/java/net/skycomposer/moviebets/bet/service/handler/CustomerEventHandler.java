package net.skycomposer.moviebets.bet.service.handler;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationFailedEvent;

@Component
@KafkaListener(topics = "${customer.events.topic.name}", groupId = "${spring.kafka.consumer.customer-events.group-id}")
public class CustomerEventHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betCommandsTopicName;

    public CustomerEventHandler(KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${bet.commands.topic.name}") String betCommandsTopicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.betCommandsTopicName = betCommandsTopicName;
    }

    @KafkaHandler
    @Transactional
    public void handleEvent(@Payload FundReservationCancelledEvent event) {
        RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(),
                "Bet %s was rejected and fund reservation has been successfully cancelled".formatted(event.getBetId()));
        kafkaTemplate.send(betCommandsTopicName, event.getBetId().toString(), rejectBetCommand);
    }

    @KafkaHandler
    @Transactional
    public void handleEvent(@Payload FundReservationFailedEvent event) {
        RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(),
                "Bet %s was rejected because of insufficient funds for customer %s: required = %.2f, but currentBalance = %.2f".formatted(
                        event.getBetId(), event.getCustomerId(), event.getFunds(), event.getCurrentBalance()));
        kafkaTemplate.send(betCommandsTopicName, event.getBetId().toString(), rejectBetCommand);
    }
}

package net.skycomposer.moviebets.bet.saga;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.bet.commands.RejectBetCommand;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationCancelledEvent;
import net.skycomposer.moviebets.common.dto.customer.events.FundReservationFailedEvent;

@Component
@KafkaListener(topics = "${customer.events.topic.name}", groupId = "${kafka.consumer.group-id}")
@RequiredArgsConstructor
public class CustomerEventHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bet.commands.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betCommandsTopicName;

    @KafkaHandler
    public void handleEvent(@Payload FundReservationCancelledEvent event) {
        RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(),
                "Bet %s was rejected and fund reservation has been successfully cancelled".formatted(event.getBetId()));
        kafkaTemplate.send(betCommandsTopicName, event.getMarketId().toString(), rejectBetCommand);
    }

    @KafkaHandler
    public void handleEvent(@Payload FundReservationFailedEvent event) {
        RejectBetCommand rejectBetCommand = new RejectBetCommand(event.getBetId(),
                "Bet %s was rejected because of insufficient funds for customer %s: required = %.2f, but currentBalance = %.2f".formatted(
                        event.getBetId(), event.getCustomerId(), event.getFunds(), event.getCurrentBalance()));
        kafkaTemplate.send(betCommandsTopicName, event.getBetId().toString(), rejectBetCommand);
    }
}

package net.skycomposer.moviebets.market.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.commands.ValidateMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketValidatedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketValidationFailedEvent;
import net.skycomposer.moviebets.market.service.MarketService;

@Component
@KafkaListener(topics = "${market.commands.topic.name}")
@RequiredArgsConstructor
public class MarketCommandHandler {

    private final MarketService marketService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bet.events.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betEventsTopicName;

    @KafkaHandler
    public void handleCommand(@Payload ValidateMarketCommand command) {
        boolean isValid = marketService.isValid(command.getMarketId());
        if (isValid) {
            MarketValidatedEvent marketValidatedEvent = new MarketValidatedEvent(command.getBetId(), command.getMarketId());
            kafkaTemplate.send(betEventsTopicName, command.getBetId().toString(), marketValidatedEvent);
        } else {
            MarketValidationFailedEvent marketValidationFailedEvent = new MarketValidationFailedEvent(command.getBetId(), command.getMarketId());
            kafkaTemplate.send(betEventsTopicName, command.getBetId().toString(), marketValidationFailedEvent);
        }
    }

}

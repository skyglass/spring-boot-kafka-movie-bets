package net.skycomposer.moviebets.bet.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import net.skycomposer.moviebets.bet.service.BetService;

@Component
@KafkaListener(topics = "${bet.commands.topic.name}")
public class BetCommandHandler {

    private final BetService betService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String betEventsTopicName;

    public BetCommandHandler(BetService betService,
                             KafkaTemplate<String, Object> kafkaTemplate,
                             @Value("${bet.events.topic.name}") String betEventsTopicName) {
        this.betService = betService;
        this.kafkaTemplate = kafkaTemplate;
        this.betEventsTopicName = betEventsTopicName;
    }

}

package net.skycomposer.moviebets.market.service.handler;

import net.skycomposer.moviebets.market.service.MarketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(topics = "${market.commands.topic.name}")
public class MarketCommandHandler {

    private final MarketService marketService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final String marketEventsTopicName;

    public MarketCommandHandler(MarketService marketService,
                                KafkaTemplate<String, Object> kafkaTemplate,
                                @Value("${market.events.topic.name}") String marketEventsTopicName) {
        this.marketService = marketService;
        this.kafkaTemplate = kafkaTemplate;
        this.marketEventsTopicName = marketEventsTopicName;
    }

}

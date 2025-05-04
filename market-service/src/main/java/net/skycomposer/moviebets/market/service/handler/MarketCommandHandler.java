package net.skycomposer.moviebets.market.service.handler;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import net.skycomposer.moviebets.common.dto.market.commands.SettleMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketSettledEvent;
import net.skycomposer.moviebets.market.service.MarketService;

@Component
@KafkaListener(topics = "${market.commands.topic.name}", groupId = "${kafka.consumer.group-id}")
@RequiredArgsConstructor
public class MarketCommandHandler {

    private final MarketService marketService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${bet.events.topic.name}")
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final String betSettleTopicName;

    @KafkaHandler
    public void handleCommand(@Payload SettleMarketCommand settleMarketCommand) {
        UUID marketId = settleMarketCommand.getMarketId();
        marketService.settle(marketId);
        MarketSettledEvent marketSettledEvent = new MarketSettledEvent(marketId);
        kafkaTemplate.send(betSettleTopicName, marketId.toString(), marketSettledEvent);
    }


}

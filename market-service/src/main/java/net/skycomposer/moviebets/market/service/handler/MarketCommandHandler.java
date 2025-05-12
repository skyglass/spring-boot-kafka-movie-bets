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
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.market.MarketResult;
import net.skycomposer.moviebets.common.dto.market.commands.SettleMarketCommand;
import net.skycomposer.moviebets.common.dto.market.events.MarketCloseConfirmedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketCloseFailedEvent;
import net.skycomposer.moviebets.common.dto.market.events.MarketSettledEvent;
import net.skycomposer.moviebets.market.service.MarketService;

@Component
@KafkaListener(topics = "${market.commands.topic.name}", groupId = "${spring.kafka.consumer.group-id}")
public class MarketCommandHandler {

    private final MarketService marketService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    public MarketCommandHandler(
            MarketService marketService,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName
    ) {
        this.marketService = marketService;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
    }

    @KafkaHandler
    @Transactional
    public void handleSettleMarketCommand(@Payload SettleMarketCommand settleMarketCommand) {
        UUID marketId = settleMarketCommand.getMarketId();
        marketService.settle(marketId);
        MarketSettledEvent marketSettledEvent = new MarketSettledEvent(marketId, settleMarketCommand.getWinResult());
        kafkaTemplate.send(betSettleTopicName, marketId.toString(), marketSettledEvent);
    }

    @KafkaHandler
    public void handleMarketCloseConfirmedEvent(@Payload MarketCloseConfirmedEvent marketCloseConfirmedEvent) {
        UUID marketId = marketCloseConfirmedEvent.getMarketId();
        MarketResult marketResult = marketCloseConfirmedEvent.getMarketResult();
        marketService.marketCloseConfirmed(marketId, marketResult);
    }

    @KafkaHandler
    public void handleMarketCloseFailedEvent(@Payload MarketCloseFailedEvent marketCloseFailedEvent) {
        UUID marketId = marketCloseFailedEvent.getMarketId();
        marketService.marketCloseFailed(marketId);
    }


}

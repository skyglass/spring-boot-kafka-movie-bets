package net.skycomposer.moviebets.market.service.handler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.market.MarketStatus;
import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCheckCommand;
import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCommand;
import net.skycomposer.moviebets.market.dao.entity.MarketCloseCheckEntity;
import net.skycomposer.moviebets.market.dao.entity.MarketEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketCloseCheckRepository;
import net.skycomposer.moviebets.market.dao.repository.MarketRepository;
import net.skycomposer.moviebets.market.service.MarketService;

@Component
@KafkaListener(topics = "${market.close.topic.name}", groupId = "${spring.kafka.consumer.market-close.group-id}")
public class MarketCloseCheckHandler {

    private final MarketService marketService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    private final MarketRepository marketRepository;

    private final Long checkTimeThresholdSeconds;

    private final MarketCloseCheckRepository marketCloseCheckRepository;

    public MarketCloseCheckHandler(
            MarketService marketService,
            MarketRepository marketRepository,
            MarketCloseCheckRepository marketCloseCheckRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${market.close.check-time.threshold-seconds}") Long checkTimeThresholdSeconds
    ) {
        this.marketService = marketService;
        this.marketRepository = marketRepository;
        this.marketCloseCheckRepository = marketCloseCheckRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
        this.checkTimeThresholdSeconds = checkTimeThresholdSeconds;
    }

    @KafkaHandler
    @Transactional
    public void handleCloseMarketCheckCommand(@Payload CloseMarketCheckCommand command) {
        MarketCloseCheckEntity marketCloseCheckEntity;
        boolean firstMarketCloseCheck = false;
        boolean marketCloseCheckUpdated = false;
        if (!marketCloseCheckRepository.existsByCheckId(MarketCloseCheckEntity.MARKET_CLOSE_CHECK_ID)) {
            marketCloseCheckEntity = new MarketCloseCheckEntity();
            firstMarketCloseCheck = true;
            marketCloseCheckUpdated = true;
        } else {
            marketCloseCheckEntity = marketCloseCheckRepository.findByCheckId(MarketCloseCheckEntity.MARKET_CLOSE_CHECK_ID).get();
        }

        var now = Instant.now();
        var lastCheckTime = marketCloseCheckEntity.getLastCheckAt();
        if (firstMarketCloseCheck || lastCheckTime.plus(Duration.ofSeconds(checkTimeThresholdSeconds)).isBefore(now)) {
            marketCloseCheckEntity.setLastCheckAt(Instant.now());
            marketCloseCheckUpdated = true;
            List<MarketEntity> openMarkets = marketRepository.findByStatus(MarketStatus.OPENED);

            for (MarketEntity market : openMarkets) {
                if (now.isAfter(market.getClosesAt())) {
                    CloseMarketCommand closeMarketCommand = new CloseMarketCommand(market.getId());
                    kafkaTemplate.send(betSettleTopicName, market.getId().toString(), closeMarketCommand);
                }
            }
        }

        if (marketCloseCheckUpdated) {
            marketCloseCheckRepository.save(marketCloseCheckEntity);
        }

    }
}

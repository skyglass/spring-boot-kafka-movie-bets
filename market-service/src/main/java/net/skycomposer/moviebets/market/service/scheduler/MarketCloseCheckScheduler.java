package net.skycomposer.moviebets.market.service.scheduler;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCheckCommand;
import net.skycomposer.moviebets.market.dao.entity.MarketCloseCheckEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketCloseCheckRepository;

@Component
public class MarketCloseCheckScheduler {

    private final MarketCloseCheckRepository marketCloseCheckRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String marketCloseTopicName;

    private final Long checkTimeThresholdSeconds;

    public MarketCloseCheckScheduler(
            MarketCloseCheckRepository marketCloseCheckRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${market.close.topic.name}") String marketCloseTopicName,
            @Value("${market.close.check-time.threshold-seconds}") Long checkTimeThresholdSeconds
    ) {
        this.marketCloseCheckRepository = marketCloseCheckRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.marketCloseTopicName = marketCloseTopicName;
        this.checkTimeThresholdSeconds = checkTimeThresholdSeconds;
    }

    @Scheduled(fixedRate = 10000) // Every 10 seconds
    @Transactional(readOnly = true)
    public void checkMarkets() {
        MarketCloseCheckEntity marketCloseCheckEntity;
        if (!marketCloseCheckRepository.existsByCheckId(MarketCloseCheckEntity.MARKET_CLOSE_CHECK_ID)) {
            marketCloseCheckEntity = null;
        } else {
            marketCloseCheckEntity = marketCloseCheckRepository.findByCheckId(MarketCloseCheckEntity.MARKET_CLOSE_CHECK_ID).get();
        }

        var now = Instant.now();
        var lastCheckTime = marketCloseCheckEntity == null ? null : marketCloseCheckEntity.getLastCheckAt();
        if (lastCheckTime == null || lastCheckTime.plus(Duration.ofSeconds(checkTimeThresholdSeconds)).isBefore(now)) {
            CloseMarketCheckCommand closeMarketCheckCommand = new CloseMarketCheckCommand(MarketCloseCheckEntity.MARKET_CLOSE_CHECK_ID);
            kafkaTemplate.send(marketCloseTopicName, MarketCloseCheckEntity.MARKET_CLOSE_CHECK_ID.toString(), closeMarketCheckCommand);
        }

    }



}

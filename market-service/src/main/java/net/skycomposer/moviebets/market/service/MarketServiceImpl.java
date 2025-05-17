package net.skycomposer.moviebets.market.service;

import static java.time.Instant.now;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.common.dto.market.*;
import net.skycomposer.moviebets.common.dto.market.commands.CloseMarketCommand;
import net.skycomposer.moviebets.market.dao.entity.MarketEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketRepository;
import net.skycomposer.moviebets.market.exception.MarketNotFoundException;

@Service
public class MarketServiceImpl implements MarketService {

    private static final String FUNDS_ADDED_SUCCESSFULLY = "Funds successfully increased from %.4f to %.4f";

    private static final String FUNDS_REMOVED_SUCCESSFULLY = "Funds successfully decreased from %.4f to %.4f";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MarketRepository marketRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    private final Integer marketCloseTimeExtendSeconds;

    public MarketServiceImpl(
            MarketRepository marketRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName,
            @Value("${market.close.close-time.extend-seconds}") Integer marketCloseTimeExtendSeconds
    ) {
        this.marketRepository = marketRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
        this.marketCloseTimeExtendSeconds = marketCloseTimeExtendSeconds;
    }


    @Override
    @Transactional(readOnly = true)
    public MarketData findMarketById(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId).get();
        if (marketEntity == null) {
            throw new MarketNotFoundException(marketId);
        }
        return MarketData.builder()
                .item1(marketEntity.getItem1())
                .item2(marketEntity.getItem2())
                .status(marketEntity.getStatus())
                .result(marketEntity.getResult())
                .open(marketEntity.getOpen())
                .closesAt(marketEntity.getClosesAt())
                .build();
    }

    @Override
    @Transactional
    public MarketResponse open(MarketData market) {
        MarketEntity marketEntity = new MarketEntity();
        marketEntity.setItem1(market.getItem1());
        marketEntity.setItem2(market.getItem2());
        marketEntity.setStatus(market.getStatus());
        marketEntity.setResult(market.getResult());
        marketEntity.setClosesAt(market.getClosesAt());
        marketEntity.setStatus(MarketStatus.OPENED);
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s opened successfully".formatted(marketEntity.getId()));
    }
    @Override
    @Transactional
    public MarketResponse close(UUID marketId) {
        CloseMarketCommand command = new CloseMarketCommand(marketId);
        kafkaTemplate.send(betSettleTopicName, marketId.toString(), command);
        return new MarketResponse(marketId,
                "Request to close Market %s has been sent successfully".formatted(marketId));
    }

    @Override
    @Transactional
    public MarketResponse marketCloseConfirmed(UUID marketId, MarketResult marketResult) {
        MarketEntity marketEntity = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketNotFoundException(marketId));
        marketEntity.setOpen(false);
        marketEntity.setStatus(MarketStatus.CLOSED);
        marketEntity.setResult(marketResult);
        marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s closed successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse marketCloseFailed(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId)
                .orElseThrow(() -> new MarketNotFoundException(marketId));
        Instant newClosesAt = now().plus(Duration.ofSeconds(marketCloseTimeExtendSeconds));
        marketEntity.setClosesAt(newClosesAt);
        marketRepository.save(marketEntity);
        return new MarketResponse(marketId,
                "Market close for market %s failed, because there is no winner yet: market close time was increased to continue".formatted(marketId));
    }

    @Override
    @Transactional
    public MarketResponse close(CloseMarketRequest request) {
        CloseMarketCommand command = new CloseMarketCommand(request.getMarketId());
        kafkaTemplate.send(betSettleTopicName, request.getMarketId().toString(), command);
        return new MarketResponse(request.getMarketId(),
                "Request to close Market %s has been sent successfully".formatted(request.getMarketId()));
    }

    @Override
    @Transactional
    public MarketResponse settle(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId).orElseThrow(
                () -> new MarketNotFoundException(marketId));
        marketEntity.setStatus(MarketStatus.SETTLED);
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s settled successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse cancel(CancelMarketRequest request) {
        var marketId = request.getMarketId();
        MarketEntity marketEntity = marketRepository.findById(marketId).orElseThrow(
                () -> new MarketNotFoundException(marketId));
        marketEntity.setOpen(false);
        marketEntity.setStatus(MarketStatus.CANCELLED);
        marketRepository.save(marketEntity);
        return new MarketResponse(marketId,
                "Market %s cancelled successfully, reason: %s".formatted(marketId, request.getReason()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketData> findAll() {
        return marketRepository.findAllByOrderByClosesAtDesc().stream()
                .map(entity -> new MarketData(entity.getId(), entity.getItem1(), entity.getItem2(),
                        entity.getStatus(), entity.getResult(), entity.getClosesAt(), entity.getOpen()))
                .collect(Collectors.toList());
    }

}

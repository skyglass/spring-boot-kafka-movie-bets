package net.skycomposer.moviebets.market.service;

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
import net.skycomposer.moviebets.common.dto.market.events.MarketClosedEvent;
import net.skycomposer.moviebets.market.dao.entity.MarketEntity;
import net.skycomposer.moviebets.market.dao.repository.MarketRepository;
import net.skycomposer.moviebets.market.dao.repository.MarketRequestRepository;
import net.skycomposer.moviebets.market.exception.MarketNotFoundException;

@Service
public class MarketServiceImpl implements MarketService {

    private static final String FUNDS_ADDED_SUCCESSFULLY = "Funds successfully increased from %.4f to %.4f";

    private static final String FUNDS_REMOVED_SUCCESSFULLY = "Funds successfully decreased from %.4f to %.4f";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MarketRepository marketRepository;

    private final MarketRequestRepository marketRequestRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;

    public MarketServiceImpl(
            MarketRepository marketRepository,
            MarketRequestRepository marketRequestRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName
    ) {
        this.marketRepository = marketRepository;
        this.marketRequestRepository = marketRequestRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
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
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s opened successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse update(MarketData marketData) {
        MarketEntity marketEntity = marketRepository.findById(marketData.getMarketId()).get();
        if (marketEntity == null) {
            throw new MarketNotFoundException(marketData.getMarketId());
        }
        marketEntity.setItem1(marketData.getItem1());
        marketEntity.setItem2(marketData.getItem2());
        marketEntity.setStatus(marketData.getStatus());
        marketEntity.setResult(marketData.getResult());
        marketEntity.setClosesAt(marketData.getClosesAt());
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s updated successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse close(CloseMarketRequest request) {
        MarketEntity marketEntity = marketRepository.findById(request.getMarketId()).get();
        if (marketEntity == null) {
            throw new MarketNotFoundException(request.getMarketId());
        }
        marketEntity.setOpen(false);
        marketEntity.setStatus(MarketStatus.CLOSED);
        marketEntity = marketRepository.save(marketEntity);
        MarketClosedEvent marketClosedEvent = new MarketClosedEvent(marketEntity.getId());
        kafkaTemplate.send(betSettleTopicName, marketEntity.getId().toString(), marketClosedEvent);
        return new MarketResponse(marketEntity.getId(),
                "Market %s closed successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse settle(UUID marketId) {
        MarketEntity marketEntity = marketRepository.findById(marketId).get();
        if (marketEntity == null) {
            throw new MarketNotFoundException(marketId);
        }
        marketEntity.setStatus(MarketStatus.SETTLED);
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s settled successfully".formatted(marketEntity.getId()));
    }

    @Override
    @Transactional
    public MarketResponse cancel(CancelMarketRequest request) {
        MarketEntity marketEntity = marketRepository.findById(request.getMarketId()).get();
        if (marketEntity == null) {
            throw new MarketNotFoundException(request.getMarketId());
        }
        marketEntity.setOpen(false);
        marketEntity.setStatus(MarketStatus.CANCELLED);
        marketEntity = marketRepository.save(marketEntity);
        return new MarketResponse(marketEntity.getId(),
                "Market %s cancelled successfully, reason: %s".formatted(marketEntity.getId(), request.getReason()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MarketData> findAll() {
        return marketRepository.findAll().stream()
                .map(entity -> new MarketData(entity.getId(), entity.getItem1(), entity.getItem2(),
                        entity.getStatus(), entity.getResult(), entity.getClosesAt(), entity.getOpen()))
                .collect(Collectors.toList());
    }

}

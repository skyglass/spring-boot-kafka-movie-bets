package net.skycomposer.moviebets.bet.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.skycomposer.moviebets.bet.dao.entity.BetEntity;
import net.skycomposer.moviebets.bet.dao.entity.BetSettleRequestEntity;
import net.skycomposer.moviebets.bet.dao.entity.MarketSettleStatusEntity;
import net.skycomposer.moviebets.bet.dao.repository.BetRepository;
import net.skycomposer.moviebets.bet.dao.repository.BetSettleRequestRepository;
import net.skycomposer.moviebets.bet.dao.repository.MarketSettleStatusRepository;
import net.skycomposer.moviebets.bet.exception.BetNotFoundException;
import net.skycomposer.moviebets.common.dto.bet.*;
import net.skycomposer.moviebets.common.dto.bet.events.BetCreatedEvent;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Service
public class BetServiceImpl implements BetService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final BetRepository betRepository;

    private final MarketSettleStatusRepository marketSettleStatusRepository;

    private final BetSettleRequestRepository betSettleRequestRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final String betSettleTopicName;


    public BetServiceImpl(
            BetRepository betRepository,
            MarketSettleStatusRepository marketSettleStatusRepository,
            BetSettleRequestRepository betSettleRequestRepository,
            KafkaTemplate<String, Object> kafkaTemplate,
            @Value("${bet.settle.topic.name}") String betSettleTopicName
    ) {
        this.betRepository = betRepository;
        this.marketSettleStatusRepository = marketSettleStatusRepository;
        this.betSettleRequestRepository = betSettleRequestRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.betSettleTopicName = betSettleTopicName;
    }


    @Override
    @Transactional(readOnly = true)
    public BetData findBetById(UUID betId) {
        BetEntity betEntity = betRepository.findById(betId).get();
        if (betEntity == null) {
            throw new BetNotFoundException(betId);
        }
        return createBetData(betEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetData> findAll() {
        return betRepository.findAll().stream()
                .map(entity -> createBetData(entity))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BetResponse open(BetData betData) {
        BetEntity betEntity = createBetEntity(betData);
        betEntity = betRepository.save(betEntity);
        BetCreatedEvent betCreatedEvent = createBetCreatedEvent(betEntity, betData.getRequestId(), betData.getCancelRequestId());
        kafkaTemplate.send(betSettleTopicName, betEntity.getMarketId().toString(), betCreatedEvent);
        return new BetResponse(betEntity.getId(),
                "Bet %s created successfully".formatted(betEntity.getId()));
    }

    @Override
    @Transactional
    public BetResponse close(CancelBetRequest cancelBetRequest) {
        BetEntity betEntity = betRepository.findById(cancelBetRequest.getBetId()).get();
        if (betEntity == null) {
            throw new BetNotFoundException(cancelBetRequest.getBetId());
        }
        betEntity.setStatus(BetStatus.CANCELLED);
        betEntity = betRepository.save(betEntity);
        return new BetResponse(betEntity.getId(),
                "Bet %s cancelled successfully".formatted(betEntity.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public BetData getState(UUID betId) {
        return findBetById(betId);
    }

    @Override
    @Transactional(readOnly = true)
    public SumStakesData getBetsByMarket(UUID marketId) {
        List<SumStakeData> groupedStakes = betRepository.findStakeSumGroupedByResult(marketId);
        return SumStakesData.builder()
                .sumStakes(groupedStakes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetDataList getBetsForMarket(UUID marketId) {
        List<BetEntity> betEntityList = betRepository.findByMarketId(marketId);
        return BetDataList.builder()
                .betDataList(createBetDataList(betEntityList))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BetDataList getBetsForPlayer(String customerId) {
        List<BetEntity> betEntityList = betRepository.findByCustomerId(customerId);
        return BetDataList.builder()
                .betDataList(createBetDataList(betEntityList))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BetData> findByMarketAndStatus(UUID marketId, BetStatus betStatus, Integer limit) {
        return betRepository.findByMarketIdAndStatus(marketId, betStatus, PageRequest.of(0, limit))
                .stream()
                .map(this::createBetData)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void updateStatus(List<UUID> betUuids, BetStatus betStatus) {
        if (betUuids == null || betUuids.isEmpty()) {
            return;
        }
        betRepository.updateStatus(betUuids, betStatus);
    }

    @Override
    @Transactional
    public void setBetValidated(UUID betId) {
        updateStatus(List.of(betId), BetStatus.VALIDATED);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isMarketClosed(UUID marketId) {
        return marketSettleStatusRepository.existsById(marketId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByStatus(BetStatus betStatus) {
        return betRepository.countByStatus(betStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public int countSettledBets(UUID marketId) {
        MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findById(marketId).get();
        if (marketSettleStatusEntity != null) {
            return marketSettleStatusEntity.getFinishedCount();
        } else {
            throw new IllegalArgumentException("Wrong usage of countSettledBets method");
        }
    }

    @Override
    @Transactional
    public void marketSettleStart(UUID marketId, int expectedCount) {
        if (!marketSettleStatusRepository.existsByMarketId(marketId)) {
            MarketSettleStatusEntity marketSettleStatusEntity = MarketSettleStatusEntity.builder()
                .marketId(marketId)
                .expectedCount(expectedCount)
                .finishedCount(0)
                .build();
            marketSettleStatusRepository.save(marketSettleStatusEntity);
        }
    }

    @Override
    @Transactional
    public void updateMarketSettleCount(UUID betId, UUID marketId) {
        if (betSettleRequestRepository.existsByRequestId(betId)) {
            String message = String.format("Duplicate bet settle request for bet %s, market = %s", betId, marketId);
            logger.warn(message);
        } else {
            MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findById(marketId).get();
            marketSettleStatusEntity.setFinishedCount(marketSettleStatusEntity.getFinishedCount() + 1);
            marketSettleStatusRepository.save(marketSettleStatusEntity);
            betSettleRequestRepository.save(
                    BetSettleRequestEntity.builder()
                    .requestId(betId)
                    .marketId(marketId)
                    .build());
        }
    }

    @Override
    @Transactional
    public void marketSettleDone(UUID marketId, MarketResult winResult) {
        if (marketSettleStatusRepository.existsByMarketId(marketId)) {
            MarketSettleStatusEntity marketSettleStatusEntity = marketSettleStatusRepository.findByMarketId(marketId).get();
            marketSettleStatusRepository.delete(marketSettleStatusEntity);
            betSettleRequestRepository.deleteByMarketId(marketId);
        }
        betRepository.settleBets(marketId, BetStatus.VALIDATED, BetStatus.SETTLED, winResult);
    }

    private BetData createBetData(BetEntity betEntity) {
        return BetData.builder()
                .betId(betEntity.getId())
                .customerId(betEntity.getCustomerId())
                .marketId(betEntity.getMarketId())
                .marketName(betEntity.getMarketName())
                .stake(betEntity.getStake())
                .result(betEntity.getResult())
                .status(betEntity.getStatus())
                .marketConfirmed(betEntity.getMarketConfirmed())
                .fundsConfirmed(betEntity.getFundsConfirmed())
                .betSettled(betEntity.getBetSettled())
                .betWon(betEntity.getBetWon())
                .build();
    }

    private List<BetData> createBetDataList(List<BetEntity> betEntityList) {
        return betEntityList.stream().map(entity -> createBetData(entity)).toList();
    }

    private BetEntity createBetEntity(BetData betData) {
        return BetEntity.builder()
                .customerId(betData.getCustomerId())
                .marketId(betData.getMarketId())
                .marketName(betData.getMarketName())
                .stake(betData.getStake())
                .result(betData.getResult())
                .build();
    }

    private BetCreatedEvent createBetCreatedEvent(BetEntity betEntity, UUID requestId, UUID cancelRequestId) {
        return BetCreatedEvent.builder()
                .betId(betEntity.getId())
                .customerId(betEntity.getCustomerId())
                .requestId(requestId)
                .cancelRequestId(cancelRequestId)
                .marketId(betEntity.getMarketId())
                .marketName(betEntity.getMarketName())
                .stake(betEntity.getStake())
                .result(betEntity.getResult())
                .build();
    }


}

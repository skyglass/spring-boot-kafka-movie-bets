package net.skycomposer.moviebets.bet.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.MarketSettleStatusEntity;

@Repository
public interface MarketSettleStatusRepository extends JpaRepository<MarketSettleStatusEntity, UUID> {

    Optional<MarketSettleStatusEntity> findByMarketId(UUID marketId);

    boolean existsByMarketId(UUID marketId);
}

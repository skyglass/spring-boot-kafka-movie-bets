package net.skycomposer.moviebets.bet.dao.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.MarketSettleStatusEntity;

@Repository
public interface MarketSettleStatusRepository extends JpaRepository<MarketSettleStatusEntity, UUID> {
}

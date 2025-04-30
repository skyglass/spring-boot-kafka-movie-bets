package net.skycomposer.moviebets.market.dao.repository;

import net.skycomposer.moviebets.market.dao.entity.MarketRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MarketRequestRepository extends JpaRepository<MarketRequestEntity, UUID> {
}

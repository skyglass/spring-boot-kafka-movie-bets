package net.skycomposer.moviebets.market.dao.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.common.dto.market.MarketStatus;
import net.skycomposer.moviebets.market.dao.entity.MarketEntity;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

    List<MarketEntity> findByStatus(MarketStatus status);

    List<MarketEntity> findAllByOrderByClosesAtDesc();

}

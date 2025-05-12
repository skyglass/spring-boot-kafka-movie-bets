package net.skycomposer.moviebets.market.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.market.dao.entity.MarketCloseCheckEntity;

@Repository
public interface MarketCloseCheckRepository extends JpaRepository<MarketCloseCheckEntity, UUID> {

    Optional<MarketCloseCheckEntity> findByCheckId(Integer checkId);

    boolean existsByCheckId(Integer checkId);

}

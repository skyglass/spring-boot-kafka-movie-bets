package net.skycomposer.betting.market.dao.repository;

import net.skycomposer.betting.market.dao.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

}

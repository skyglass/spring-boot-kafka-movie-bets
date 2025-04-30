package net.skycomposer.moviebets.market.dao.repository;

import net.skycomposer.moviebets.market.dao.entity.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

}

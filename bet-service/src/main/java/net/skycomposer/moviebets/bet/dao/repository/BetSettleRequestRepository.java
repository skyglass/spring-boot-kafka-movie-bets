package net.skycomposer.moviebets.bet.dao.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.BetSettleRequestEntity;

@Repository
public interface BetSettleRequestRepository extends JpaRepository<BetSettleRequestEntity, UUID> {

    @Modifying
    @Query("DELETE FROM BetSettleRequestEntity b WHERE b.marketId = :marketId")
    void deleteByMarketId(UUID marketId);

    Optional<BetSettleRequestEntity> findByRequestId(UUID requestId);

    boolean existsByRequestId(UUID requestId);
}

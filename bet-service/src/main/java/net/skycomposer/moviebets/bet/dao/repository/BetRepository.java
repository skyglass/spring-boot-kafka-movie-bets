package net.skycomposer.moviebets.bet.dao.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.BetEntity;
import net.skycomposer.moviebets.common.dto.bet.BetStatus;
import net.skycomposer.moviebets.common.dto.bet.SumStakeData;
import net.skycomposer.moviebets.common.dto.market.MarketResult;

@Repository
public interface BetRepository extends JpaRepository<BetEntity, UUID> {

    @Query("SELECT new net.skycomposer.moviebets.common.dto.bet.SumStakeData(SUM(b.stake), COUNT(b.id), b.result) " +
            "FROM BetEntity b WHERE b.marketId = :marketId GROUP BY b.result")
    List<SumStakeData> findStakeSumGroupedByResult(@Param("marketId") UUID marketId);

    List<BetEntity> findByMarketId(UUID marketId);

    List<BetEntity> findByCustomerId(String marketId);

    List<BetEntity> findByMarketIdAndStatus(UUID marketId, BetStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE BetEntity b SET b.status = :status WHERE b.id IN :ids")
    void updateStatus(List<UUID> ids, BetStatus status);

    @Modifying
    @Query("""
        UPDATE BetEntity b
        SET b.status = :settledStatus,
            b.betWon = CASE WHEN b.result = :winResult THEN true ELSE false END
        WHERE b.marketId = :marketId AND b.status = :validatedStatus
    """)
    void settleBets(UUID marketId, BetStatus validatedStatus, BetStatus settledStatus, MarketResult winResult);

    int countByStatus(BetStatus status);

}

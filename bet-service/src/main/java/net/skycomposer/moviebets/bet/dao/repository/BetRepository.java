package net.skycomposer.moviebets.bet.dao.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.BetEntity;
import net.skycomposer.moviebets.common.dto.bet.SumStakeData;

@Repository
public interface BetRepository extends JpaRepository<BetEntity, UUID> {

    @Query("SELECT new net.skycomposer.moviebets.common.dto.bet.SumStakeData(SUM(b.stake), b.result) " +
            "FROM BetEntity b WHERE b.marketId = :marketId GROUP BY b.result")
    List<SumStakeData> findStakeSumGroupedByResult(@Param("marketId") UUID marketId);

    List<BetEntity> findByMarketId(UUID marketId);

    List<BetEntity> findByCustomerId(String marketId);

}

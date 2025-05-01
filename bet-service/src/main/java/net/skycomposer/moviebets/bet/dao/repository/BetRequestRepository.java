package net.skycomposer.moviebets.bet.dao.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import net.skycomposer.moviebets.bet.dao.entity.BetRequestEntity;

@Repository
public interface BetRequestRepository extends JpaRepository<BetRequestEntity, UUID> {
}

package net.skycomposer.moviebets.customer.dao.repository;

import net.skycomposer.moviebets.customer.dao.entity.FundRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FundRequestRepository extends JpaRepository<FundRequestEntity, UUID> {

    Optional<FundRequestEntity> findByRequestId(UUID requestId);

    boolean existsByRequestId(UUID requestId);
}

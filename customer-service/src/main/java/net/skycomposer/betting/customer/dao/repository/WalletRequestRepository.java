package net.skycomposer.betting.customer.dao.repository;

import net.skycomposer.betting.customer.dao.entity.CustomerEntity;
import net.skycomposer.betting.customer.dao.entity.WalletRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WalletRequestRepository extends JpaRepository<WalletRequestEntity, UUID> {
}

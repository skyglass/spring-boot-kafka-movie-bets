package net.skycomposer.betting.customer.service;

import net.skycomposer.betting.common.domain.dto.customer.Customer;
import net.skycomposer.betting.common.domain.dto.customer.WalletData;
import net.skycomposer.betting.common.domain.dto.customer.WalletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CustomerService {

    List<Customer> findAll();

    Customer findCustomerById(String customerId);

    WalletResponse addFunds(String customerId, String requestId, BigDecimal funds);

    WalletResponse removeFunds(String customerId, String requestId, BigDecimal funds);

    WalletData findWalletById(String customerId);

    List<WalletData> findAllWallets();
}
